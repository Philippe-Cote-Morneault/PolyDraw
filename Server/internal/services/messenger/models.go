package messenger

//MessageSent represent a message that was sent by the client
type MessageSent struct {
	Message   string
	ChannelID string
}

//MessageReceived represent a message that was relayed by the server
type MessageReceived struct {
	ChannelID  string
	Timestamp  int
	SenderID   string
	SenderName string
	Message    string
}

//ChannelJoin represent a message that was sent by the server to inform the client that a new user has join
type ChannelJoin struct {
	UserID    string
	Username  string
	ChannelID string
	Timestamp int
}
