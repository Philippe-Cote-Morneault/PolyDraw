package lobby

import "gitlab.com/jigsawcorp/log3900/pkg/cbroadcast"

//BJoinGroup join a group
const BJoinGroup = "lobby:join"

//BLeaveGroup leave a group
const BLeaveGroup = "lobby:leave"

const BStartMatch = "lobby:start"

//BSize buffer size for the messenger service
const BSize = 5

//Register the broadcast for messenger
func (l *Lobby) Register() {
	cbroadcast.Register(BJoinGroup, BSize)
	cbroadcast.Register(BLeaveGroup, BSize)
	cbroadcast.Register(BStartMatch, BSize)
}
