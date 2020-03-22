package virtualplayer

import (
	"log"
	"math/rand"
	"sync"

	"gitlab.com/jigsawcorp/log3900/internal/socket"

	"gitlab.com/jigsawcorp/log3900/internal/services/auth"
	"gitlab.com/jigsawcorp/log3900/internal/services/drawing"
	"gitlab.com/jigsawcorp/log3900/model"

	"github.com/google/uuid"
	match2 "gitlab.com/jigsawcorp/log3900/internal/match"
)

var managerInstance Manager

type responseHint struct {
	Hint  string
	Error string
}

type gameHints struct {
	GameID uuid.UUID
	Hints  map[string]bool
}

// Manager represents a struct that manage all virtual players
type Manager struct {
	mutex    sync.Mutex
	Bots     map[uuid.UUID]*virtualPlayerInfos //botID -> virtualPlayerInfos
	Channels map[uuid.UUID]uuid.UUID           //groupID -> channelID
	Games    map[uuid.UUID]*gameHints          //groupID -> gameHints
	Groups   map[uuid.UUID]map[uuid.UUID]bool  //groupID -> []botID
	Matches  map[uuid.UUID]*match2.IMatch      //groupID -> IMatch
	Hints    map[uuid.UUID]map[string]bool     //playerID -> []indices

}

func (m *Manager) init() {
	m.Bots = make(map[uuid.UUID]*virtualPlayerInfos)
	m.Channels = make(map[uuid.UUID]uuid.UUID)
	m.Groups = make(map[uuid.UUID]map[uuid.UUID]bool)
	m.Matches = make(map[uuid.UUID]*match2.IMatch)
	m.Games = make(map[uuid.UUID]*gameHints)
	m.Hints = make(map[uuid.UUID]map[string]bool)

}

//AddGroup [Current Thread] adds the group to cache (lobby)
func AddGroup(groupID uuid.UUID) {
	managerInstance.mutex.Lock()
	managerInstance.Groups[groupID] = make(map[uuid.UUID]bool)
	managerInstance.mutex.Unlock()
	printManager("AddGroup")
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
		log.Printf("[Virtual Player] -> [Error] Can't find channelID with groupID : %v. Aborting RemoveGroup...", groupID)
		return
	}

	delete(managerInstance.Channels, groupID)

	group, ok := managerInstance.Groups[groupID]
	managerInstance.mutex.Unlock()

	if !ok {
		log.Printf("[Virtual Player] -> [Error] Can't find groupId : %v. Aborting RemoveGroup...", groupID)
		return
	}

	for botID := range group {
		KickVirtualPlayer(botID)
	}

	managerInstance.mutex.Lock()
	delete(managerInstance.Groups, groupID)
	managerInstance.mutex.Unlock()
	printManager("RemoveGroup")
}

//AddVirtualPlayer [Current Thread] adds virtualPlayer to cache. Returns playerID, username (lobby)
func AddVirtualPlayer(groupID, botID uuid.UUID) string {
	playerInfos := generateVirtualPlayer()
	playerInfos.BotID = botID
	playerInfos.GroupID = groupID
	managerInstance.mutex.Lock()
	group, ok := managerInstance.Groups[groupID]

	if !ok {
		managerInstance.mutex.Unlock()
		log.Printf("[Virtual Player] -> [Error] Can't find groupId : %v. Aborting AddVirtualPlayer...", groupID)
		return ""
	}

	group[botID] = true
	managerInstance.Bots[botID] = playerInfos
	managerInstance.mutex.Unlock()

	log.Println("[Virtual Player] -> AddVirtualPlayer")
	printManager("AddVirtualPlayer")

	return playerInfos.Username
}

//KickVirtualPlayer [Current Thread] kicks virtualPlayer from cache. Returns playerID, username (lobby)
func KickVirtualPlayer(userID uuid.UUID) (uuid.UUID, string) {
	managerInstance.mutex.Lock()
	bot, botOk := managerInstance.Bots[userID]
	if !botOk {
		managerInstance.mutex.Unlock()
		log.Printf("[Virtual Player] -> [Error] Can't find botID : %v. Aborting KickVirtualPlayer...", userID)
		return uuid.Nil, ""
	}

	groupID := bot.GroupID
	group, ok := managerInstance.Groups[groupID]
	if !ok {
		managerInstance.mutex.Unlock()
		log.Printf("[Virtual Player] -> [Error] Can't find groupId : %v. Aborting KickVirtualPlayer...", groupID)
		return uuid.Nil, ""
	}

	if _, ok := group[userID]; !ok {
		managerInstance.mutex.Unlock()
		log.Printf("[Virtual Player] -> [Error] Can't find bot with id : %v in group : %v. Aborting KickVirtualPlayer...", userID, groupID)
		return uuid.Nil, ""
	}

	delete(group, userID)
	delete(managerInstance.Bots, userID)
	managerInstance.mutex.Unlock()

	var groupDB model.Group
	var user model.User
	model.DB().Where("id = ?", groupID).First(&groupDB)

	if groupDB.ID == uuid.Nil {
		log.Printf("[Virtual Player] -> [Error] Can't find in DB group with id : %v. Aborting KickVirtualPlayer...", groupID)
		return uuid.Nil, ""
	}

	model.DB().Where("id = ?", userID).First(&user)

	if user.ID == uuid.Nil {
		log.Printf("[Virtual Player] -> [Error] Can't find in DB user with id : %v. Aborting KickVirtualPlayer...", userID)
		return uuid.Nil, ""
	}

	model.DB().Model(&groupDB).Association("Users").Delete(&user)
	model.DB().Unscoped().Delete(&user)
	groupDB.VirtualPlayers--
	model.DB().Save(&groupDB)

	log.Printf("[Virtual Player] -> deleting bot in DB: %v", user)
	printManager("KickVirtualPlayer")

	return groupID, bot.Username

}

// handleStartGame [New Threads] does the startGame routine for a bot in match (match ->)
func handleStartGame(match match2.IMatch) {
	groupID := match.GetGroupID()

	managerInstance.mutex.Lock()
	managerInstance.Matches[groupID] = &match
	channelID, ok := managerInstance.Channels[groupID]
	if !ok {
		managerInstance.mutex.Unlock()
		log.Printf("[Virtual Player] -> [Error] Can't find channelID with groupID : %v. Aborting handleStartGame...", groupID)
		return
	}
	group, groupOk := managerInstance.Groups[groupID]

	if !groupOk {
		managerInstance.mutex.Unlock()
		log.Printf("[Virtual Player] -> [Error] Can't find groupId : %v. Aborting handleStartGame...", groupID)
		return
	}
	//Unlocks mutex at the end
	makeBotsSpeak("startGame", group, channelID)
	printManager("handleStartGame")

}

// startDrawing [New Threads] bot draws for all player in games (match ->)
func startDrawing(round *match2.RoundStart) {
	log.Printf("[Virtual Player] Round start begin of startDrawing round:%v", *round.Game.Image)
	managerInstance.mutex.Lock()
	//Save All Hints from game
	g := gameHints{GameID: (*round).Game.ID, Hints: make(map[string]bool)}
	for _, h := range (*round).Game.Hints {
		g.Hints[h.Hint] = true
	}
	managerInstance.Games[round.MatchID] = &g

	bot, ok := managerInstance.Bots[(*round).Drawer.ID]
	if !ok {
		managerInstance.mutex.Unlock()
		log.Printf("[Virtual Player] -> [Error] Can't find botID : %v. Aborting drawing...", (*round).Drawer.ID)
		return
	}

	match, groupOk := managerInstance.Matches[(*round).MatchID]
	if !groupOk {
		managerInstance.mutex.Unlock()
		log.Printf("[Virtual Player] -> [Error] Can't find match with groupID : %v. Aborting drawing...", (*round).MatchID)
		return
	}
	managerInstance.mutex.Unlock()

	uuidBytes, _ := (*round).Game.ID.MarshalBinary()
	var wg sync.WaitGroup
	connections := (*match).GetConnections()
	wg.Add(len(connections))
	for _, id := range connections {
		go func(socketID uuid.UUID) {
			defer wg.Done()
			drawing.StartDrawing(socketID, uuidBytes, round.Game.Image.SVGFile, bot.DrawingTimeFactor)
		}(id)
	}
	wg.Wait()
	printManager("startDrawing")
}

// handleRoundEnds [New Threads] does the roundEnd routine for a bot in match (match ->)
func handleRoundEnds(groupID uuid.UUID) {
	managerInstance.mutex.Lock()
	channelID, ok := managerInstance.Channels[groupID]
	if !ok {
		managerInstance.mutex.Unlock()
		log.Printf("[Virtual Player] -> [Error] Can't find channelID with groupID : %v. Aborting handleRoundEnds...", groupID)
		return
	}

	group, groupOk := managerInstance.Groups[groupID]
	if !groupOk {
		managerInstance.mutex.Unlock()
		log.Printf("[Virtual Player] -> [Error] Can't find groupId : %v. Aborting handleRoundEnds...", groupID)
		return
	}
	//Unlocks mutex at the end
	makeBotsSpeak("endRound", group, channelID)
	printManager("handleRoundEnds")

}

// handleEndGame [New Threads] does the endGame routine for a bot in match (match ->)
func handleEndGame(groupID uuid.UUID) {
	managerInstance.mutex.Lock()

	if _, ok := managerInstance.Games[groupID]; !ok {
		managerInstance.mutex.Unlock()
		log.Printf("[Virtual Player] -> [Error] Can't find game with groupID : %v. Aborting handleEndGame...", groupID)
		return
	}
	delete(managerInstance.Games, groupID)

	if _, ok := managerInstance.Matches[groupID]; !ok {
		managerInstance.mutex.Unlock()
		log.Printf("[Virtual Player] -> [Error] Can't find match with groupID : %v. Aborting handleEndGame...", groupID)
		return
	}

	for _, socketID := range (*managerInstance.Matches[groupID]).GetConnections() {
		playerID, err := auth.GetUserID(socketID)
		if err != nil {
			log.Printf("[Virtual Player] -> [Error] Can't find userID from socketID: %v. Aborting handleEndGame...", socketID)
			return
		}

		if _, ok := managerInstance.Hints[playerID]; !ok {
			log.Printf("[Virtual Player] -> [Error] Can't find player in game with groupID : %v. Aborting handleEndGame...", groupID)
			return
		}
		delete(managerInstance.Hints, playerID)
	}

	delete(managerInstance.Matches, groupID)
	managerInstance.mutex.Unlock()
	RemoveGroup(groupID)
	printManager("handleEndGame")
}

//GetVirtualPlayersInfo [Current Thread] returns botInfos from cache (match)
func GetVirtualPlayersInfo(groupID uuid.UUID) []match2.BotInfos {
	var botsInfos []match2.BotInfos
	managerInstance.mutex.Lock()
	bots, ok := managerInstance.Groups[groupID]

	if !ok {
		managerInstance.mutex.Unlock()
		log.Printf("[Virtual Player] -> [Error] Can't find groupId : %v. Aborting getVirtualPlayersInfo...", groupID)
		return nil
	}

	for botID := range bots {
		botInfos, infoOk := managerInstance.Bots[botID]
		if !infoOk {
			log.Printf("[Virtual Player] -> [Error] Can't find botID : %v. Aborting getVirtualPlayersInfo...", botID)
			return nil
		}
		botsInfos = append(botsInfos, match2.BotInfos{BotID: botInfos.BotID, Username: botInfos.Username})
	}
	managerInstance.mutex.Unlock()
	log.Printf("[Virtual Player] GetVirtualPlayersInfos returns %v", botsInfos)
	return botsInfos
}

//GetHintByBot returns a boolean if hint is given to user or not
func GetHintByBot(hintRequest match2.HintRequested) bool {
	playerID := hintRequest.Player.ID
	managerInstance.mutex.Lock()

	game, ok := managerInstance.Games[hintRequest.MatchID]
	if !ok {
		managerInstance.mutex.Unlock()
		log.Printf("[Virtual Player] -> [Error] Can't find game with groupID : %v. Aborting GetHintByBot...", hintRequest.MatchID)
		respHintRequest(false, hintRequest.SocketID, "Group Id inccorect, game doesn't exists")
		return false
	}

	_, hasHint := managerInstance.Hints[playerID]
	managerInstance.mutex.Unlock()

	if !hasHint {
		//Will iterate once and take the first hint in game
		for hint := range game.Hints {
			managerInstance.Hints[playerID] = make(map[string]bool)
			managerInstance.Hints[playerID][hint] = true
			if hintRequest.GameType == 0 {
				delete(game.Hints, hint)
			}
			managerInstance.mutex.Unlock()
			respHintRequest(true, hintRequest.SocketID, hint)
			return true
		}
	} else {
		//Will look for an hint not asked yet
		for hint := range game.Hints {
			if _, hintExists := managerInstance.Hints[playerID][hint]; !hintExists {
				managerInstance.Hints[playerID][hint] = true
				if hintRequest.GameType == 0 {
					delete(game.Hints, hint)
				}
				managerInstance.mutex.Unlock()
				respHintRequest(true, hintRequest.SocketID, hint)
				return true
			}
		}
	}

	managerInstance.mutex.Unlock()
	respHintRequest(false, hintRequest.SocketID, "No more hints remaining.")
	return false
}

// respHintRequest [Current Thread] sends to client hint response (virtualplayer)
func respHintRequest(hintOk bool, socketID uuid.UUID, hint string) {
	var hintRes responseHint
	if hintOk {
		hintRes.Hint = hint
		hintRes.Error = ""
	} else {
		hintRes.Hint = ""
		hintRes.Error = hint
	}
	message := socket.RawMessage{}
	message.ParseMessagePack(byte(socket.MessageType.ResponseHintMatch), hintRes)
	socket.SendRawMessageToSocketID(message, socketID)
}

// randomUsername [Current Thread] return random username among players in match (virtualplayer)
func randomUsername(groupID uuid.UUID) string {
	//TODO temporary hack waiting for real stats
	managerInstance.mutex.Lock()
	match, ok := managerInstance.Matches[groupID]
	managerInstance.mutex.Unlock()

	if !ok {
		log.Printf("[Virtual Player] -> [Error] Can't find match with groupID : %v. Aborting randomUsername...", groupID)
		return ""
	}

	players := (*match).GetPlayers()

	return players[rand.Intn(len(players))].Username
}

//makeBotsSpeak [New Threads] sends bot interaction to all connected users (virtualplayer)
func makeBotsSpeak(interactionType string, group map[uuid.UUID]bool, channelID uuid.UUID) {
	var wg sync.WaitGroup
	wg.Add(len(group))
	for botID := range group {
		bot, botOk := managerInstance.Bots[botID]
		if !botOk {
			log.Printf("[Virtual Player] -> [Error] Can't find botID : %v.", botID)
			return
		}
		go func(chanID uuid.UUID) {
			defer wg.Done()
			bot.speak(chanID, interactionType)
		}(channelID)
	}
	managerInstance.mutex.Unlock()
	wg.Wait()
}
