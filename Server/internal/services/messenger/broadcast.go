package messenger

import "gitlab.com/jigsawcorp/log3900/pkg/cbroadcast"

//BMessageSent broadcast message when the client sends a message to the server
const BMessageSent = "messenger:sent"

//BCreateChannel broadcast message when the client wants to create a channel
const BCreateChannel = "messenger:chancreate"

//BJoinChannel broadcast message when the client wants to join a channel
const BJoinChannel = "messenger:chanjoin"

//BLeaveChannel broadcast message when the client wants to leave a channel
const BLeaveChannel = "messenger:chanleave"

//BDestroyChannel broadcast message when the client wants to destroy a channel
const BDestroyChannel = "messenger:chandestroy"

//BBotMessage broadcast message when a bot wants to speak
const BBotMessage = "messenger:botmessage"

//BSize buffer size for the messenger service
const BSize = 5

//Register the broadcast for messenger
func (m *Messenger) Register() {
	cbroadcast.Register(BMessageSent, BSize)
	cbroadcast.Register(BCreateChannel, BSize)
	cbroadcast.Register(BJoinChannel, BSize)
	cbroadcast.Register(BLeaveChannel, BSize)
	cbroadcast.Register(BDestroyChannel, BSize)
	cbroadcast.Register(BBotMessage, BSize)
}
