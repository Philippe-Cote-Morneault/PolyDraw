package socket

import "gitlab.com/jigsawcorp/log3900/pkg/cbroadcast"

//BSocketReady broadcasted when the socket is ready
const BSocketReady = "socket:ready"

//BSocketConnected broadcasted when the socket is connected for a client
//Sends the client id a uuid
const BSocketConnected = "socket:connect"

//BSocketCloseClient broadcasted when the socket is closed for a client
//Sends the client id a uuid
const BSocketCloseClient = "socket:closeclient"

//BSocketAuthConnected broadcasted when the socket is connected for a client, only fires if the socket is authenthicated
//Sends the client id a uuid
const BSocketAuthConnected = "socket:authconnect"

//BSocketAuthCloseClient broadcasted when the socket is closed for a client, only fires if the socket is authenthicated
//Sends the client id a uuid
const BSocketAuthCloseClient = "socket:authcloseclient"

//BSocketClose broadcasted when the socket is closed for all client
//Nil is closed by itself or the error if it is a server error
const BSocketClose = "socket:close"

//BSocketReceive broadcasted when the socket received data for a client
//Sends the RawMessageReceived struct contains a message and a client id
const BSocketReceive = "socket:receive"

//BSize default buffer size for broadcast
const BSize = 20

//RegisterBroadcast Register all the broadcast for the socket
func RegisterBroadcast() {
	cbroadcast.Register(BSocketReady, BSize)
	cbroadcast.Register(BSocketConnected, BSize)
	cbroadcast.Register(BSocketCloseClient, BSize)
	cbroadcast.Register(BSocketAuthConnected, BSize)
	cbroadcast.Register(BSocketAuthCloseClient, BSize)
	cbroadcast.Register(BSocketClose, BSize)
	cbroadcast.Register(BSocketReceive, BSize)
}
