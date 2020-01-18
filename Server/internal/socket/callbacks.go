package socket

import "time"

// MessageCallback is the subscriber function called when messages are received on the socket
type MessageCallback = func(message SocketMessage, sender string)

// EventCallback is te subscriber function called when socket events occur
type EventCallback = func(client *ClientSocket, timestamp time.Time)
