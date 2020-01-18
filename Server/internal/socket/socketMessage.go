package socket

import "time"

// Represents a message sent over socket
type SocketMessage struct {
	Type int
	Data interface{}
	Time time.Time
}

// Quick implementation of enum to represent different message types
type messageType struct {
	ServerConnection int
	ServerDisconnection int
	UserDisconnected int
	MessageSent int
	MessageReceived int
	JoinChannel int
	UserJoinedChannel int
	LeaveChannel int
	UserLeftChannel int
	CreateChannel int
}

// Enum for public use
var MessageType = &messageType{
	ServerConnection: 0,
	ServerDisconnection: 1,
	UserDisconnected: 2,
	MessageSent: 20,
	MessageReceived: 21,
	JoinChannel: 22,
	UserJoinedChannel: 23,
	LeaveChannel: 24,
	UserLeftChannel: 25,
	CreateChannel: 26,
}

