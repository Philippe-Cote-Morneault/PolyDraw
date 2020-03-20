package virtualplayer

import (
	"log"
	"sync"

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
	Games    map[uuid.UUID]*match2.IMatch      //groupID -> ffa

}

func (m *Manager) init() {
	m.Bots = make(map[uuid.UUID]*virtualPlayerInfos)
	m.Channels = make(map[uuid.UUID]uuid.UUID)
	m.Groups = make(map[uuid.UUID]map[uuid.UUID]bool)
	m.Games = make(map[uuid.UUID]*match2.IMatch)
}

//AddGroup adds the group to cache
func AddGroup(groupID uuid.UUID) {
	managerInstance.mutex.Lock()
	managerInstance.Groups[groupID] = make(map[uuid.UUID]bool)
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
			} else {
				managerInstance.mutex.Unlock()
				log.Printf("[Virtual Player] -> [Error] Can't find user with id : %v in group : %v. Aborting KickVirtualPlayer...", userID, groupID)
				return uuid.Nil, ""
			}
		} else {
			managerInstance.mutex.Unlock()
			log.Printf("[Virtual Player] -> [Error] Can't find group with id : %v of user : %v. Aborting KickVirtualPlayer...", groupID, userID)
			return uuid.Nil, ""
		}

	} else {
		managerInstance.mutex.Unlock()
		log.Printf("[Virtual Player] -> [Error] Can't find userID : %v. Aborting KickVirtualPlayer...", userID)
		return uuid.Nil, ""
	}
}

// startGame does the startGame routine for a bot
func startGame(game match2.IMatch) {
	groupID := game.GetGroupID()

	managerInstance.mutex.Lock()
	managerInstance.Games[groupID] = &game
	if channelID, ok := managerInstance.Channels[groupID]; ok {
		for botID := range managerInstance.Groups[groupID] {
			managerInstance.Bots[botID].speak(channelID, "start")
		}
		managerInstance.mutex.Unlock()
	} else {
		managerInstance.mutex.Unlock()
		log.Printf("[Virtual Player] -> [Error] Can't find channelID of groupID : %v. Aborting startGame...", groupID)
	}

}

func startDrawing(round *match2.RoundStart) {
	if !round.Drawer.IsCPU {
		return
	}
	_, ok := managerInstance.Bots[round.Drawer.ID]

	if !ok {
		log.Printf("[Virtual Player] -> [Error] Can't find bot's id : %v. Aborting drawing...", round.Drawer.ID)
		return
	}
	// for _, playerID := managerInstance.Games[round.MatchID].playerUsernames{

	// 	drawing.StartDrawing()
	// }
}
