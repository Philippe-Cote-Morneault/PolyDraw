package messenger

import (
	"github.com/google/uuid"
	"gitlab.com/jigsawcorp/log3900/internal/socket"
	"gitlab.com/jigsawcorp/log3900/model"
	"sync"
)

var instance *handler
var channelCache map[uuid.UUID]channelGroup
var mutex sync.RWMutex

type channelGroup struct {
	channelID      uuid.UUID
	createResponse socket.RawMessage
}

func setInstance(handle *handler) {
	instance = handle
	channelCache = make(map[uuid.UUID]channelGroup)
}

//RegisterGroup create a channel for each group
func RegisterGroup(group *model.Group) {
	channelID, response := instance.createGroupChannel(group)

	mutex.Lock()
	channelCache[group.ID] = channelGroup{
		channelID:      channelID,
		createResponse: response,
	}
	mutex.Unlock()
}

//HandleJoinGroup join a group chat
func HandleJoinGroup(group *model.Group, socketID uuid.UUID) {
	mutex.RLock()
	channel := channelCache[group.ID]
	mutex.RUnlock()

	socket.SendQueueMessageSocketID(channel.createResponse, socketID)
	instance.joinChannel(socketID, channel.channelID)
}

//HandleQuitGroup leave a group chat
func HandleQuitGroup(group *model.Group, socketID uuid.UUID) {
	mutex.RLock()
	channelID := channelCache[group.ID].channelID
	mutex.RUnlock()

	if channelID != uuid.Nil {
		instance.quitChannel(socketID, channelID)
	}
}

//UnRegisterGroup remove a channel for each group
func UnRegisterGroup(group *model.Group, connections []uuid.UUID) {
	mutex.RLock()
	channelID := channelCache[group.ID].channelID
	mutex.RUnlock()

	if channelID != uuid.Nil {
		for i := range connections {
			instance.quitChannel(connections[i], channelID)
		}
		instance.deleteGroupChannel(group)

		mutex.Lock()
		delete(channelCache, group.ID)
		mutex.Unlock()
	}
}

//BroadcastAll sends a message to all the connections
func BroadcastAll(message socket.RawMessage) {
	instance.broadcast(uuid.Nil, message)
}
