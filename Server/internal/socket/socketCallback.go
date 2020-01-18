package socket

type SocketCallback = func(message SocketMessage, sender string)
