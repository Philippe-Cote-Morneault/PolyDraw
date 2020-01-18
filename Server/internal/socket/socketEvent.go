package socket

// Quick implementation of enum to represent different socket events.
type socketEvent struct {
	Connection    int
	Disconnection int
}

// SocketEvent represents an event that can occur with sockets.
var SocketEvent = &socketEvent{
	Connection:    0,
	Disconnection: 1,
}
