package messenger

import (
	"github.com/google/uuid"
	"gitlab.com/jigsawcorp/log3900/model"
)

var instance *handler
var channelIDCache map[string]uuid.UUID

func setInstance(handle *handler) {
	instance = handle
	channelIDCache = make(map[string]uuid.UUID)
}

//RegisterGroup create a channel for each group
func RegisterGroup(group *model.Group, connections []uuid.UUID) {
	channelID := instance.createGroupChannel(group, connections)
	channelIDCache[group.Name] = channelID

	//Join all the users to the game
	for i := range connections {
		instance.joinChannel(connections[i], channelID)
	}
}

//HandleJoinGroup join a group chat
func HandleJoinGroup(group *model.Group, socketID uuid.UUID) {
	channelID := channelIDCache[group.Name]
	instance.joinChannel(socketID, channelID)
}

//HandleQuitGroup leave a group chat
func HandleQuitGroup(group *model.Group, socketID uuid.UUID) {
	channelID := channelIDCache[group.Name]
	instance.quitChannel(socketID, channelID)
}

//UnregisterGame remove a channel for each group
func UnRegisterGroup(group *model.Group, connections []uuid.UUID) {
	channelID := channelIDCache[group.Name]
	for i := range connections {
		instance.quitChannel(connections[i], channelID)
	}

	instance.deleteGroupChannel(group)
}
