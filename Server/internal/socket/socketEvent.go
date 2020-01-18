package socket

// Quick implementation of enum to represent different socket events.
type socketEvent struct {
	Connection int
	Disconnection int
}

// Enum for public use
var SocketEvent = &socketEvent{
	Connection: 0,
	Disconnection: 1,
}
