package lobby

import "gitlab.com/jigsawcorp/log3900/pkg/cbroadcast"

//BJoinGroup join a group
const BJoinGroup = "lobby:join"

//BLeaveGroup leave a group
const BLeaveGroup = "lobby:leave"

//BStartMatch promote the group to a match
const BStartMatch = "lobby:start"

//BKickUser kick a user from a group
const BKickUser = "lobby:kick"

//BSize buffer size for the messenger service
const BSize = 5

//Register the broadcast for messenger
func (l *Lobby) Register() {
	cbroadcast.Register(BJoinGroup, BSize)
	cbroadcast.Register(BLeaveGroup, BSize)
	cbroadcast.Register(BStartMatch, BSize)
	cbroadcast.Register(BKickUser, BSize)
}
