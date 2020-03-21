package virtualplayer

import (
	"log"
	"sync"

	"gitlab.com/jigsawcorp/log3900/internal/services/auth"
	"gitlab.com/jigsawcorp/log3900/internal/services/drawing"

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

//AddGroup adds the group to cache (lobby)
func AddGroup(groupID uuid.UUID) {
	managerInstance.mutex.Lock()
	managerInstance.Groups[groupID] = make(map[uuid.UUID]bool)
	managerInstance.mutex.Unlock()
}

//AddGroup adds the group to cache (lobby)
func registerChannelGroup(groupID, channelID uuid.UUID) {
	managerInstance.mutex.Lock()
	managerInstance.Channels[groupID] = channelID
	managerInstance.mutex.Unlock()
}

//RemoveGroup adds the group to cache
func RemoveGroup(groupID uuid.UUID) {
	managerInstance.mutex.Lock()
	if _, ok := managerInstance.Groups[groupID]; ok {
		delete(managerInstance.Groups, groupID)
		managerInstance.mutex.Unlock()
	} else {
		managerInstance.mutex.Unlock()
		log.Printf("[Virtual Player] -> [Error] Can't find groupId : %v. Aborting RemoveGroup...", groupID)
	}
}

//AddVirtualPlayer adds virtualPlayer to cache. Returns playerID, username
func AddVirtualPlayer(groupID, botID uuid.UUID) string {

	playerInfos := generateVirtualPlayer()
	playerInfos.BotID = botID
	playerInfos.GroupID = groupID
	managerInstance.mutex.Lock()

	if group, groupOk := managerInstance.Groups[groupID]; groupOk {
		group[botID] = true
	} else {
		log.Printf("[Virtual Player] -> [Error] Can't find groupId : %v. Aborting AddVirtualPlayer...", groupID)
		return ""
	}

	managerInstance.Bots[botID] = playerInfos
	managerInstance.mutex.Unlock()

	return playerInfos.Username
}

//KickVirtualPlayer kicks virtualPlayer from cache. Returns playerID, username
func KickVirtualPlayer(userID uuid.UUID) (uuid.UUID, string) {
	managerInstance.mutex.Lock()
	if bot, ok := managerInstance.Bots[userID]; ok {
		groupID := bot.GroupID

		if group, ok := managerInstance.Groups[groupID]; ok {

			if _, ok := group[userID]; ok {
				delete(group, userID)
				delete(managerInstance.Bots, userID)
				managerInstance.mutex.Unlock()

				return groupID, bot.Username
			}
			managerInstance.mutex.Unlock()
			log.Printf("[Virtual Player] -> [Error] Can't find user with id : %v in group : %v. Aborting KickVirtualPlayer...", userID, groupID)
			return uuid.Nil, ""
		}
		managerInstance.mutex.Unlock()
		log.Printf("[Virtual Player] -> [Error] Can't find group with id : %v of user : %v. Aborting KickVirtualPlayer...", groupID, userID)
		return uuid.Nil, ""
	}
	managerInstance.mutex.Unlock()
	log.Printf("[Virtual Player] -> [Error] Can't find userID : %v. Aborting KickVirtualPlayer...", userID)
	return uuid.Nil, ""
}

// handleStartGame does the startGame routine for a bot in game
func handleStartGame(game match2.IMatch) {
	groupID := game.GetGroupID()

	managerInstance.mutex.Lock()
	managerInstance.Games[groupID] = &game
	if channelID, ok := managerInstance.Channels[groupID]; ok {
		for botID := range managerInstance.Groups[groupID] {
			managerInstance.Bots[botID].speak(channelID, "startGame")
		}
		managerInstance.mutex.Unlock()
	} else {
		managerInstance.mutex.Unlock()
		log.Printf("[Virtual Player] -> [Error] Can't find channelID of groupID : %v. Aborting startGame...", groupID)
	}
}

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

func handleRoundEnds(groupID uuid.UUID) {
	managerInstance.mutex.Lock()
	group, groupOk := managerInstance.Groups[groupID]
	if !groupOk {
		managerInstance.mutex.Unlock()
		log.Printf("[Virtual Player] -> [Error] Can't find groupId : %v. Aborting handleRoundEnds...", groupID)
		return
	}

	if channelID, ok := managerInstance.Channels[groupID]; ok {
		for botID := range group {
			managerInstance.Bots[botID].speak(channelID, "roundStart")
		}
		managerInstance.mutex.Unlock()
	} else {
		managerInstance.mutex.Unlock()
		log.Printf("[Virtual Player] -> [Error] Can't find channelID of groupID : %v. Aborting handleRoundEnds...", groupID)
	}
}

// handleStartGame does the startGame routine for a bot
func handleEndGame(groupID uuid.UUID) {
	managerInstance.mutex.Lock()

	if _, ok := managerInstance.Games[groupID]; ok {
		delete(managerInstance.Games, groupID)
	} else {
		managerInstance.mutex.Unlock()
		log.Printf("[Virtual Player] -> [Error] Can't find game of groupID : %v. Aborting handleEndGame...", groupID)
		return
	}

	if _, ok := managerInstance.Channels[groupID]; ok {
		delete(managerInstance.Channels, groupID)
	} else {
		managerInstance.mutex.Unlock()
		log.Printf("[Virtual Player] -> [Error] Can't find channelID of groupID : %v. Aborting handleEndGame...", groupID)
		return
	}

	if group, ok := managerInstance.Groups[groupID]; ok {
		for botID := range group {
			delete(group, botID)
		}
		delete(managerInstance.Groups, groupID)
		managerInstance.mutex.Unlock()
	} else {
		managerInstance.mutex.Unlock()
		log.Printf("[Virtual Player] -> [Error] Can't find bots of groupID : %v. Aborting handleEndGame...", groupID)
	}
}
