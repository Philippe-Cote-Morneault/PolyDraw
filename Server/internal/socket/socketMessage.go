package socket

type SocketMessage struct {

}

// Quick implementation of enum to represent different message types
type messageList struct {
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
var MessageType = &messageList{
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

