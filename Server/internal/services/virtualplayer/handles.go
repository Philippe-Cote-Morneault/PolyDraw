package virtualplayer

import (
	"log"
	"math/rand"
	"sync"

	"gitlab.com/jigsawcorp/log3900/internal/services/auth"
	"gitlab.com/jigsawcorp/log3900/internal/services/drawing"
	"gitlab.com/jigsawcorp/log3900/model"

	"github.com/google/uuid"
	match2 "gitlab.com/jigsawcorp/log3900/internal/match"
)

var managerInstance Manager

// Manager represents a struct that manage all virtual players
type Manager struct {
	mutex    sync.Mutex
	Bots     map[uuid.UUID]*virtualPlayerInfos // playerID -> virtualPlayerInfos
	Channels map[uuid.UUID]uuid.UUID           // groupID -> channelID
	Groups   map[uuid.UUID]map[uuid.UUID]bool  //groupID -> []botID
	Games    map[uuid.UUID]*match2.IMatch      //groupID -> IMatch

}

func (m *Manager) init() {
	m.Bots = make(map[uuid.UUID]*virtualPlayerInfos)
	m.Channels = make(map[uuid.UUID]uuid.UUID)
	m.Groups = make(map[uuid.UUID]map[uuid.UUID]bool)
	m.Games = make(map[uuid.UUID]*match2.IMatch)
}

//AddGroup [Current Thread] adds the group to cache (lobby)
func AddGroup(groupID uuid.UUID) {
	managerInstance.mutex.Lock()
	managerInstance.Groups[groupID] = make(map[uuid.UUID]bool)
	managerInstance.mutex.Unlock()
	printManager()
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
		log.Printf("[Virtual Player] -> [Error] Can't find channelID of groupID : %v. Aborting handleEndGame...", groupID)
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
	printManager()
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
	printManager()

	return playerInfos.Username
}

//KickVirtualPlayer [Current Thread] kicks virtualPlayer from cache. Returns playerID, username (lobby)
func KickVirtualPlayer(userID uuid.UUID) (uuid.UUID, string) {
	managerInstance.mutex.Lock()
	bot, botOk := managerInstance.Bots[userID]
	if !botOk {
		managerInstance.mutex.Unlock()
		log.Printf("[Virtual Player] -> [Error] Can't find userID : %v. Aborting KickVirtualPlayer...", userID)
		return uuid.Nil, ""
	}

	groupID := bot.GroupID
	group, ok := managerInstance.Groups[groupID]
	if !ok {
		managerInstance.mutex.Unlock()
		log.Printf("[Virtual Player] -> [Error] Can't find group with id : %v of user : %v. Aborting KickVirtualPlayer...", groupID, userID)
		return uuid.Nil, ""
	}

	if _, ok := group[userID]; !ok {
		managerInstance.mutex.Unlock()
		log.Printf("[Virtual Player] -> [Error] Can't find user with id : %v in group : %v. Aborting KickVirtualPlayer...", userID, groupID)
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

	log.Printf("[Lobby] -> deleting bot in DB: %v", user)
	printManager()

	return groupID, bot.Username

}

// handleStartGame [New Threads] does the startGame routine for a bot in game (match ->)
func handleStartGame(game match2.IMatch) {
	groupID := game.GetGroupID()

	managerInstance.mutex.Lock()
	managerInstance.Games[groupID] = &game
	channelID, ok := managerInstance.Channels[groupID]
	if !ok {
		managerInstance.mutex.Unlock()
		log.Printf("[Virtual Player] -> [Error] Can't find channelID of groupID : %v. Aborting startGame...", groupID)
		return
	}
	group := managerInstance.Groups[groupID]
	var wg sync.WaitGroup
	wg.Add(len(group))
	for botID := range group {
		go func(id uuid.UUID) {
			defer wg.Done()
			managerInstance.Bots[id].speak(channelID, "startGame")
		}(botID)
	}
	managerInstance.mutex.Unlock()
	wg.Wait()
}

// startDrawing [New Threads] bot draws for all player in games (-> match)
func startDrawing(round *match2.RoundStart) {
	managerInstance.mutex.Lock()
	if !round.Drawer.IsCPU {
		return
	}
	bot, ok := managerInstance.Bots[round.Drawer.ID]

	if !ok {
		managerInstance.mutex.Unlock()
		log.Printf("[Virtual Player] -> [Error] Can't find bot's id : %v. Aborting drawing...", round.Drawer.ID)
		return
	}

	game, groupOk := managerInstance.Games[round.MatchID]
	if !groupOk {
		managerInstance.mutex.Unlock()
		log.Printf("[Virtual Player] -> [Error] Can't find group's id : %v. Aborting drawing...", round.MatchID)
		return
	}

	for _, playerID := range (*game).GetConnections() {
		socketID, err := auth.GetSocketID(playerID)

		if err != nil {
			managerInstance.mutex.Unlock()
			log.Printf("[Virtual Player] -> [Error] Can't find user's socketid from userID: %v. Aborting drawing...", playerID)
			return
		}

		uuidBytes, _ := round.Game.ID.MarshalBinary()
		go drawing.StartDrawing(socketID, uuidBytes, round.Game.Image.SVGFile, bot.DrawingTimeFactor)
	}
	managerInstance.mutex.Unlock()
}

// handleRoundEnds [New Threads] does the roundEnd routine for a bot in game (match ->)
func handleRoundEnds(groupID uuid.UUID) {
	managerInstance.mutex.Lock()
	channelID, ok := managerInstance.Channels[groupID]
	if !ok {
		managerInstance.mutex.Unlock()
		log.Printf("[Virtual Player] -> [Error] Can't find channelID of groupID : %v. Aborting handleRoundEnds...", groupID)
		return
	}

	group, groupOk := managerInstance.Groups[groupID]
	if !groupOk {
		managerInstance.mutex.Unlock()
		log.Printf("[Virtual Player] -> [Error] Can't find groupId : %v. Aborting handleRoundEnds...", groupID)
		return
	}

	var wg sync.WaitGroup
	wg.Add(len(group))
	for botID := range group {
		go func(id uuid.UUID) {
			defer wg.Done()
			managerInstance.Bots[id].speak(channelID, "endRound")
		}(botID)
	}
	managerInstance.mutex.Unlock()
	wg.Wait()
}

// handleEndGame [New Threads] does the endGame routine for a bot in game (match ->)
func handleEndGame(groupID uuid.UUID) {
	managerInstance.mutex.Lock()

	if _, ok := managerInstance.Games[groupID]; !ok {
		managerInstance.mutex.Unlock()
		log.Printf("[Virtual Player] -> [Error] Can't find game of groupID : %v. Aborting handleEndGame...", groupID)
		return
	}

	delete(managerInstance.Games, groupID)
	managerInstance.mutex.Unlock()
	RemoveGroup(groupID)
}

//TODO temporary waiting for real stats
// randomUsername [Current Thread] return random username among players in game (virtualplayer)
func randomUsername(groupID uuid.UUID) string {
	managerInstance.mutex.Lock()
	game, ok := managerInstance.Games[groupID]
	managerInstance.mutex.Unlock()

	if !ok {
		return ""
	}

	players := (*game).GetPlayers()

	return players[rand.Intn(len(players))].Username
}
