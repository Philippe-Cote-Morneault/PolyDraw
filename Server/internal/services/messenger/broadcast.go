package messenger

import "gitlab.com/jigsawcorp/log3900/pkg/cbroadcast"

//BMessageSent broadcast message when the client sends a message to the server
const BMessageSent = "messenger:sent"

//BSize buffer size for the messenger service
const BSize = 5

//Register the broadcast for messenger
func (m *Messenger) Register() {
	cbroadcast.Register(BMessageSent, BSize)
}
