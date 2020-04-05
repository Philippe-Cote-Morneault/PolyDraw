package virtualplayer

import (
	"log"
	"math/rand"
	"sync"
	"time"

	"gitlab.com/jigsawcorp/log3900/internal/language"
	"gitlab.com/jigsawcorp/log3900/internal/socket"

	"gitlab.com/jigsawcorp/log3900/internal/services/auth"
	"gitlab.com/jigsawcorp/log3900/internal/services/drawing"
	"gitlab.com/jigsawcorp/log3900/model"

	"github.com/google/uuid"
	"github.com/tevino/abool"
	match2 "gitlab.com/jigsawcorp/log3900/internal/match"
)

var managerInstance Manager

const drawingTimeBot = 10 //in Seconds

type responseHint struct {
	UserID    string
	HintsLeft int
	Hint      string
	Error     string
	BotID     string
}

type gameHints struct {
	GameID uuid.UUID
	Hints  map[string]bool
}

// Manager represents a struct that manage all virtual players
type Manager struct {
	mutex           sync.Mutex
	Bots            map[uuid.UUID]*virtualPlayerInfos //botID -> virtualPlayerInfos
	Channels        map[uuid.UUID]uuid.UUID           //groupID -> channelID
	HintsInGames    map[uuid.UUID]*gameHints          //groupID -> gameHints
	Groups          map[uuid.UUID]map[uuid.UUID]bool  //groupID -> []botID
	Matches         map[uuid.UUID]*match2.IMatch      //groupID -> IMatch
	HintsPerPlayers map[uuid.UUID]map[string]bool     //playerID -> []indices
	Drawing         map[uuid.UUID]*abool.AtomicBool   //socketID -> stopDrawing

}

func (m *Manager) init() {
	m.Bots = make(map[uuid.UUID]*virtualPlayerInfos)
	m.Channels = make(map[uuid.UUID]uuid.UUID)
	m.Groups = make(map[uuid.UUID]map[uuid.UUID]bool)
	m.Matches = make(map[uuid.UUID]*match2.IMatch)
	m.HintsInGames = make(map[uuid.UUID]*gameHints)
	m.HintsPerPlayers = make(map[uuid.UUID]map[string]bool)
	m.Drawing = make(map[uuid.UUID]*abool.AtomicBool)
}

//AddGroup [Current Thread] adds the group to cache (lobby)
func AddGroup(groupID uuid.UUID) {
	managerInstance.mutex.Lock()
	managerInstance.Groups[groupID] = make(map[uuid.UUID]bool)
	managerInstance.mutex.Unlock()
}

//registerChannelGroup [New Thread] saves in cache the groupID corresponding to channelID (messenger->)
func registerChannelGroup(groupID, channelID uuid.UUID) {
	managerInstance.mutex.Lock()
	managerInstance.Channels[groupID] = channelID
	managerInstance.mutex.Unlock()
}

//RemoveGroup [Current Thread] removes the group from cache (lobby)
func RemoveGroup(groupID uuid.UUID) {
	managerInstance.mutex.Lock()

	if _, ok := managerInstance.Channels[groupID]; !ok {
		managerInstance.mutex.Unlock()
		log.Printf("[VirtualPlayer] -> [Error] Can't find channelID with groupID : %v. Aborting RemoveGroup...", groupID)
		return
	}

	delete(managerInstance.Channels, groupID)

	group, ok := managerInstance.Groups[groupID]
	managerInstance.mutex.Unlock()

	if !ok {
		log.Printf("[VirtualPlayer] -> [Error] Can't find groupId : %v. Aborting RemoveGroup...", groupID)
		return
	}

	for botID := range group {
		KickVirtualPlayer(botID)
	}

	managerInstance.mutex.Lock()
	delete(managerInstance.Groups, groupID)
	managerInstance.mutex.Unlock()
}

//AddVirtualPlayer [Current Thread] adds virtualPlayer to cache. Returns playerID, username (lobby)
func AddVirtualPlayer(groupID, botID uuid.UUID, lang int) string {
	playerInfos := generateVirtualPlayer(lang)
	playerInfos.BotID = botID
	playerInfos.GroupID = groupID
	managerInstance.mutex.Lock()
	group, ok := managerInstance.Groups[groupID]

	if !ok {
		managerInstance.mutex.Unlock()
		log.Printf("[VirtualPlayer] -> [Error] Can't find groupId : %v. Aborting AddVirtualPlayer...", groupID)
		return ""
	}

	group[botID] = true
	managerInstance.Bots[botID] = playerInfos
	managerInstance.mutex.Unlock()

	log.Println("[VirtualPlayer] -> AddVirtualPlayer")
	return playerInfos.Username
}

//KickVirtualPlayer [Current Thread] kicks virtualPlayer from cache. Returns playerID, username (lobby)
func KickVirtualPlayer(userID uuid.UUID) (uuid.UUID, string) {
	managerInstance.mutex.Lock()
	bot, botOk := managerInstance.Bots[userID]
	if !botOk {
		managerInstance.mutex.Unlock()
		log.Printf("[VirtualPlayer] -> [Error] Can't find botID : %v. Aborting KickVirtualPlayer...", userID)
		return uuid.Nil, ""
	}

	groupID := bot.GroupID
	group, ok := managerInstance.Groups[groupID]
	if !ok {
		managerInstance.mutex.Unlock()
		log.Printf("[VirtualPlayer] -> [Error] Can't find groupId : %v. Aborting KickVirtualPlayer...", groupID)
		return uuid.Nil, ""
	}

	if _, ok := group[userID]; !ok {
		managerInstance.mutex.Unlock()
		log.Printf("[VirtualPlayer] -> [Error] Can't find bot with id : %v in group : %v. Aborting KickVirtualPlayer...", userID, groupID)
		return uuid.Nil, ""
	}

	delete(group, userID)
	delete(managerInstance.Bots, userID)
	managerInstance.mutex.Unlock()

	var groupDB model.Group
	var user model.User
	model.DB().Where("id = ?", groupID).First(&groupDB)

	if groupDB.ID == uuid.Nil {
		log.Printf("[VirtualPlayer] -> [Error] Can't find in DB group with id : %v. Aborting KickVirtualPlayer...", groupID)
		return uuid.Nil, ""
	}

	model.DB().Where("id = ?", userID).First(&user)

	if user.ID == uuid.Nil {
		log.Printf("[VirtualPlayer] -> [Error] Can't find in DB user with id : %v. Aborting KickVirtualPlayer...", userID)
		return uuid.Nil, ""
	}

	model.DB().Model(&groupDB).Association("Users").Delete(&user)
	model.DB().Unscoped().Delete(&user)
	groupDB.VirtualPlayers--
	model.DB().Save(&groupDB)

	log.Printf("[VirtualPlayer] -> deleting bot in DB: %v", user)
	return groupID, bot.Username

}

// handleStartGame [New Threads] does the startGame routine for a bot in match (match ->)
func handleStartGame(match match2.IMatch) {
	groupID := match.GetGroupID()
	managerInstance.mutex.Lock()
	managerInstance.Matches[groupID] = &match
	managerInstance.mutex.Unlock()

	makeBotsSpeak("startGame", groupID, uuid.Nil)
}

// startDrawing [New Threads] bot draws for all player in games (match ->)
func startDrawing(round *match2.RoundStart) {
	log.Printf("[VirtualPlayer] Round start begin of startDrawing round:%v", *round.Game.Image)
	managerInstance.mutex.Lock()
	//Save All Hints from game
	g := gameHints{GameID: (*round).Game.ID, Hints: make(map[string]bool)}
	for _, h := range (*round).Game.Hints {
		g.Hints[h.Hint] = true
	}
	managerInstance.HintsInGames[round.MatchID] = &g

	bot, ok := managerInstance.Bots[(*round).Drawer.ID]
	if !ok {
		managerInstance.mutex.Unlock()
		log.Printf("[VirtualPlayer] -> [Error] Can't find botID : %v. Aborting drawing...", (*round).Drawer.ID)
		return
	}

	match, groupOk := managerInstance.Matches[(*round).MatchID]
	if !groupOk {
		managerInstance.mutex.Unlock()
		log.Printf("[VirtualPlayer] -> [Error] Can't find match with groupID : %v. Aborting drawing...", (*round).MatchID)
		return
	}
	managerInstance.mutex.Unlock()

	time.Sleep(1700 * time.Millisecond)

	uuidBytes, _ := (*round).Game.ID.MarshalBinary()
	var wg sync.WaitGroup
	connections := (*match).GetConnections()
	stopAllDrawingProcedures(connections)
	stopDrawings := initializeDrawingStates(connections)

	wg.Add(1)
	go func() {
		defer wg.Done()
		drawing.StartDrawing(connections, uuidBytes, &drawing.Draw{SVGFile: round.Game.Image.SVGFile, DrawingTime: bot.DrawingTimeFactor * drawingTimeBot, Mode: round.Game.Image.Mode}, stopDrawings)
	}()
	wg.Wait()
}

// handleRoundEnds [New Threads] does the roundEnd routine for a bot in match (match ->)
func handleRoundEnds(groupID uuid.UUID, makeBotSpeak bool) {
	managerInstance.mutex.Lock()
	match, groupOk := managerInstance.Matches[groupID]
	if !groupOk {
		managerInstance.mutex.Unlock()
		log.Printf("[VirtualPlayer] -> [Error] Can't find match with groupID : %v. Aborting handleRoundEnds...", groupID)
		return
	}
	connections := (*match).GetConnections()
	managerInstance.mutex.Unlock()

	stopAllDrawingProcedures(connections)

	if makeBotSpeak {
		makeBotsSpeak("endRound", groupID, uuid.Nil)
	}
}

// handleEndGame [New Threads] does the endGame routine for a bot in match (match ->)
func handleEndGame(groupID uuid.UUID) {
	handleRoundEnds(groupID, false)
	managerInstance.mutex.Lock()

	if _, ok := managerInstance.HintsInGames[groupID]; !ok {
		managerInstance.mutex.Unlock()
		log.Printf("[VirtualPlayer] -> [Error] Can't find game with groupID : %v. Aborting handleEndGame...", groupID)
		return
	}
	delete(managerInstance.HintsInGames, groupID)

	if _, ok := managerInstance.Matches[groupID]; !ok {
		managerInstance.mutex.Unlock()
		log.Printf("[VirtualPlayer] -> [Error] Can't find match with groupID : %v. Aborting handleEndGame...", groupID)
		return
	}

	for _, socketID := range (*managerInstance.Matches[groupID]).GetConnections() {
		playerID, err := auth.GetUserID(socketID)
		if err != nil {
			managerInstance.mutex.Unlock()
			log.Printf("[VirtualPlayer] -> [Error] Can't find userID from socketID: %v. Aborting handleEndGame...", socketID)
			return
		}

		if _, ok := managerInstance.HintsPerPlayers[playerID]; !ok {
			continue
		}
		delete(managerInstance.HintsPerPlayers, playerID)
	}

	delete(managerInstance.Matches, groupID)
	managerInstance.mutex.Unlock()
	RemoveGroup(groupID)
}

//GetVirtualPlayersInfo [Current Thread] returns botInfos from cache (match)
func GetVirtualPlayersInfo(groupID uuid.UUID) []match2.BotInfos {
	var botsInfos []match2.BotInfos
	defer managerInstance.mutex.Unlock()
	managerInstance.mutex.Lock()
	bots, ok := managerInstance.Groups[groupID]

	if !ok {
		log.Printf("[VirtualPlayer] -> [Error] Can't find groupId : %v. Aborting getVirtualPlayersInfo...", groupID)
		return nil
	}

	for botID := range bots {
		botInfos, infoOk := managerInstance.Bots[botID]
		if !infoOk {
			log.Printf("[VirtualPlayer] -> [Error] Can't find botID : %v. Aborting getVirtualPlayersInfo...", botID)
			return nil
		}
		botsInfos = append(botsInfos, match2.BotInfos{BotID: botInfos.BotID, Username: botInfos.Username})
	}
	log.Printf("[VirtualPlayer] GetVirtualPlayersInfos returns %v", botsInfos)
	return botsInfos
}

//GetHintByBot returns a boolean if hint is given to user or not
func GetHintByBot(hintRequest *match2.HintRequested) bool {
	log.Printf("[VirtualPlayer] In GetHintByBot with hintRequest : %v", *hintRequest)
	playerID := hintRequest.Player.ID
	managerInstance.mutex.Lock()

	hintsInGame, ok := managerInstance.HintsInGames[hintRequest.MatchID]
	if !ok {
		managerInstance.mutex.Unlock()
		log.Printf("[VirtualPlayer] -> [Error] Can't find hint with groupID : %v. Aborting GetHintByBot...", hintRequest.MatchID)
		respHintRequest(false, hintRequest, "Group Id incorrect", hintRequest.GameType)
		return false
	}

	_, hasHint := managerInstance.HintsPerPlayers[playerID]
	if !hasHint || hintRequest.GameType != 0 {
		//Will iterate once and take the first hint in game
		for hint := range hintsInGame.Hints {
			if hintRequest.GameType != 0 {
				delete(hintsInGame.Hints, hint)
			} else {
				managerInstance.HintsPerPlayers[playerID] = make(map[string]bool)
				managerInstance.HintsPerPlayers[playerID][hint] = true
			}
			managerInstance.mutex.Unlock()
			respHintRequest(true, hintRequest, hint, hintRequest.GameType)
			return true
		}
	} else {
		//Will look for an hint not asked yet
		for hint := range hintsInGame.Hints {
			if _, hintExists := managerInstance.HintsPerPlayers[playerID][hint]; !hintExists {
				if hintRequest.GameType != 0 {
					delete(hintsInGame.Hints, hint)
				} else {
					managerInstance.HintsPerPlayers[playerID][hint] = true
				}
				managerInstance.mutex.Unlock()
				respHintRequest(true, hintRequest, hint, hintRequest.GameType)
				return true
			}
		}
	}

	managerInstance.mutex.Unlock()
	respHintRequest(false, hintRequest, "", hintRequest.GameType)
	return false
}

// respHintRequest [Current Thread] sends to client hint response (virtualplayer)
func respHintRequest(hintOk bool, hintRequest *match2.HintRequested, hint string, gameType int) {
	var hintRes responseHint
	bot, botOk := managerInstance.Bots[hintRequest.DrawerID]
	if !botOk {
		log.Printf("[VirtualPlayer] -> [Error] Can't find botID : %v. Aborting respHintRequest", hintRequest.DrawerID)
		return
	}
	hintRes.BotID = bot.BotID.String()
	if hintOk {
		lineHint := " Mon indice est : "
		if bot.Language == 0 {
			lineHint = " My hint is : "
		}
		hintRes.Hint = bot.getInteraction("hintRequest") + lineHint + hint
		hintRes.Error = ""
	} else {
		if hint == "" {
			hint = language.MustGet("botlines.noHint"+bot.Personality, bot.Language)
		}
		hintRes.Hint = ""
		hintRes.Error = hint
	}
	managerInstance.mutex.Lock()
	group, ok := managerInstance.Matches[hintRequest.MatchID]
	if !ok {
		managerInstance.mutex.Unlock()
		log.Printf("[VirtualPlayer] -> [Error] Can't find groupId : %v. Aborting respHintRequest...", hintRequest.MatchID)
		return
	}

	if gameType == 0 {
		hintRes.HintsLeft = getHintsLeft(hintRequest.MatchID, hintRequest.Player.ID) // pas de lock
		hintRes.UserID = hintRequest.Player.ID.String()

		message := socket.RawMessage{}
		message.ParseMessagePack(byte(socket.MessageType.ResponseHintMatch), hintRes)
		socket.SendQueueMessageSocketID(message, hintRequest.SocketID)
	} else {

		for _, socketID := range (*group).GetConnections() {
			playerID, err := auth.GetUserID(socketID)
			if err != nil {
				log.Printf("[VirtualPlayer] -> [Error] Can't send hint Respond to userID :%v ", playerID)
			}

			hintRes.HintsLeft = getHintsLeft(hintRequest.MatchID, playerID) // pas de lock
			hintRes.UserID = hintRequest.Player.ID.String()

			message := socket.RawMessage{}
			message.ParseMessagePack(byte(socket.MessageType.ResponseHintMatch), hintRes)
			socket.SendQueueMessageSocketID(message, socketID)
		}
	}

	managerInstance.mutex.Unlock()
}

// randomUsername [Current Thread] return random username among players in match (virtualplayer)
func randomUsername(groupID uuid.UUID) string {
	//TODO temporary hack waiting for real stats
	managerInstance.mutex.Lock()
	match, ok := managerInstance.Matches[groupID]
	managerInstance.mutex.Unlock()

	if !ok {
		log.Printf("[VirtualPlayer] -> [Error] Can't find match with groupID : %v. Aborting randomUsername...", groupID)
		return ""
	}

	players := (*match).GetPlayers()

	return players[rand.Intn(len(players))].Username
}

//makeBotsSpeak [New Threads] sends bot interaction to all connected users (virtualplayer)
func makeBotsSpeak(interactionType string, groupID, speakingBotID uuid.UUID) {
	managerInstance.mutex.Lock()

	channelID, ok := managerInstance.Channels[groupID]
	if !ok {
		managerInstance.mutex.Unlock()
		log.Printf("[VirtualPlayer] -> [Error] Can't find channelID with groupID : %v. Aborting makeBotsSpeak...", groupID)
		return
	}

	group, groupOk := managerInstance.Groups[groupID]

	if !groupOk {
		managerInstance.mutex.Unlock()
		log.Printf("[VirtualPlayer] -> [Error] Can't find groupId : %v. Aborting makeBotsSpeak...", groupID)
		return
	}

	var wg sync.WaitGroup
	wg.Add(len(group))
	if speakingBotID != uuid.Nil {
		bot, botOk := managerInstance.Bots[speakingBotID]
		if !botOk {
			managerInstance.mutex.Unlock()
			log.Printf("[VirtualPlayer] -> [Error] Can't find botID : %v.", speakingBotID)
			return
		}
		go func(chanID uuid.UUID) {
			defer wg.Done()
			bot.speak(chanID, interactionType)
		}(channelID)
	} else {
		for botID := range group {
			bot, botOk := managerInstance.Bots[botID]
			if !botOk {
				managerInstance.mutex.Unlock()
				log.Printf("[VirtualPlayer] -> [Error] Can't find botID : %v.", botID)
				return
			}
			go func(chanID uuid.UUID) {
				defer wg.Done()
				bot.speak(chanID, interactionType)
			}(channelID)
		}
	}
	managerInstance.mutex.Unlock()
	wg.Wait()
}

// Only called in hitRequest
func getHintsLeft(groupID, playerID uuid.UUID) int {
	hintsInGame, ok := managerInstance.HintsInGames[groupID]
	if !ok {
		log.Printf("[VirtualPlayer] -> [Error] Can't find game with groupID : %v. Aborting getHintsLeft...", groupID)
		return -1
	}
	hintsPlayers, ok := managerInstance.HintsPerPlayers[playerID]

	hintAsked := 0
	if ok {
		hintAsked = len(hintsPlayers)
	}

	return len(hintsInGame.Hints) - hintAsked
}
func initializeDrawingStates(connections []uuid.UUID) []*abool.AtomicBool {
	var stopDrawings []*abool.AtomicBool

	managerInstance.mutex.Lock()

	for _, connection := range connections {
		a := abool.New()
		managerInstance.Drawing[connection] = a
		stopDrawings = append(stopDrawings, a)
	}
	managerInstance.mutex.Unlock()
	return stopDrawings
}

func stopDrawingOfSocket(socketID uuid.UUID) {
	managerInstance.mutex.Lock()
	stopDrawing, ok := managerInstance.Drawing[socketID]
	managerInstance.mutex.Unlock()

	if !ok {
		log.Printf("[VirtualPlayer] -> [Error] Can't find socketID : %v. Can't stop drawing procedure...", socketID)
		return
	}
	stopDrawing.Set()
}

func stopAllDrawingProcedures(connections []uuid.UUID) {
	managerInstance.mutex.Lock()
	for _, connection := range connections {
		if stopDrawing, ok := managerInstance.Drawing[connection]; ok {
			stopDrawing.Set()
		} else {
			log.Printf("[VirtualPlayer] -> [Error] Can't find socketID : %v. Can't stop drawing procdeure...", connection)
		}
	}
	managerInstance.mutex.Unlock()
}
