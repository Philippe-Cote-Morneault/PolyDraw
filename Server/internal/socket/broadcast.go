package socket

import "gitlab.com/jigsawcorp/log3900/pkg/cbroadcast"

//BSocketReady broadcasted when the socket is ready
const BSocketReady = "socket:ready"

//BSocketDisconnect broadcasted when the socket is disconnected for a client with the disconnect message
//Sends the client id a uuid
const BSocketDisconnect = "socket:disconnect"

//BSocketConnected broadcasted when the socket is connected for a client
//Sends the client id a uuid
const BSocketConnected = "socket:connect"

//BSocketCloseClient broadcasted when the socket is closed for a client
//Sends the client id a uuid
const BSocketCloseClient = "socket:closeclient"

//BSocketClose broadcasted when the socket is closed for all client
//Nil is closed by itself or the error if it is a server error
const BSocketClose = "socket:close"

//BSocketReceive broadcasted when the socket received data for a client
const BSocketReceive = "socket:receive"

//BSize default buffer size for broadcast
const BSize = 20

//RegisterBroadcast Register all the broadcast for the socket
func RegisterBroadcast() {
	cbroadcast.Register(BSocketReady, BSize)
	cbroadcast.Register(BSocketDisconnect, BSize)
	cbroadcast.Register(BSocketConnected, BSize)
	cbroadcast.Register(BSocketCloseClient, BSize)
	cbroadcast.Register(BSocketClose, BSize)
	cbroadcast.Register(BSocketReceive, BSize)
}
