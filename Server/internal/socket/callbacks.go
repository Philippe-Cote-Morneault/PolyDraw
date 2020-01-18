package socket

import "time"

// Callback signature when socket messages are transmitted
type MessageCallback = func(message SocketMessage, sender string)

// Callback signature when socket events occur
type EventCallback = func(client ClientSocket, timestamp time.Time)
