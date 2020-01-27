package messenger

//MessageSent represent a message that was sent by the client
type MessageSent struct {
	Message string
	CanalID string
}

//MessageReceived represent a message that was relayed by the server
type MessageReceived struct {
	CanalID    string
	Timestamp  int
	SenderID   string
	SenderName string
	Message    string
}
