package messenger

import (
	"github.com/google/uuid"
	"gitlab.com/jigsawcorp/log3900/internal/socket"
	"gitlab.com/jigsawcorp/log3900/model"
)

var instance *handler
var channelCache map[string]channelGroup

type channelGroup struct {
	channelID      uuid.UUID
	createResponse socket.RawMessage
}

func setInstance(handle *handler) {
	instance = handle
	channelCache = make(map[string]channelGroup)
}

//RegisterGroup create a channel for each group
func RegisterGroup(group *model.Group) {
	channelID, response := instance.createGroupChannel(group)
	channelCache[group.Name] = channelGroup{
		channelID:      channelID,
		createResponse: response,
	}
}

//HandleJoinGroup join a group chat
func HandleJoinGroup(group *model.Group, socketID uuid.UUID) {
	channel := channelCache[group.Name]
	socket.SendRawMessageToSocketID(channel.createResponse, socketID)
	instance.joinChannel(socketID, channel.channelID)
}

//HandleQuitGroup leave a group chat
func HandleQuitGroup(group *model.Group, socketID uuid.UUID) {
	channelID := channelCache[group.Name].channelID
	instance.quitChannel(socketID, channelID)
}

//UnRegisterGroup remove a channel for each group
func UnRegisterGroup(group *model.Group, connections []uuid.UUID) {
	channelID := channelCache[group.Name].channelID
	for i := range connections {
		instance.quitChannel(connections[i], channelID)
	}
	instance.deleteGroupChannel(group)
}
