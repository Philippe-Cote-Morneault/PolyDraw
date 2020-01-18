package socket

// Callback signature wen socket messages are transmitted
type SocketCallback = func(message SocketMessage, sender string)
