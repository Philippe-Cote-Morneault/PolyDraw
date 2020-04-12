package broadcast

import "gitlab.com/jigsawcorp/log3900/pkg/cbroadcast"

//BUpdateMatch broadcasted to update game stats of user
const BUpdateMatch = "stats:updatematch"

//BSetDeconnection broadcasted to update connections of user
const BSetDeconnection = "stats:setdeconnection"

//BCreateConnection broadcasted when the socket is closed for a client
const BCreateConnection = "stats:createconnection"

//BSize default buffer size for broadcast
const BSize = 5

//RegisterBroadcast Register all the broadcast for the socket
func RegisterBroadcast() {
	cbroadcast.Register(BUpdateMatch, BSize)
	cbroadcast.Register(BSetDeconnection, BSize)
	cbroadcast.Register(BCreateConnection, BSize)
}
