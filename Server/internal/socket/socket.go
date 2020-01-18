package socket

import (
	"fmt"
	"net"
)

type Server struct {
	listener *net.Listener
	clientSocketManager *ClientSocketManager
}
// Starts listening to incoming socket connections
func (server *Server)StartListening(host string) {
	listener, err := net.Listen("tcp", host)
	if err != nil {
		fmt.Println(err)
	}
	server.listener = &listener

	server.clientSocketManager = &ClientSocketManager{
		clients: make(map[*ClientSocket]bool),
		subscribers: make(map[int][]SocketCallback),
	}

	for {
		connection, err := (*server.listener).Accept()
		if err != nil {
			fmt.Println(err)
		}
		clientSocket := &ClientSocket{socket: connection}
		server.clientSocketManager.RegisterClient(clientSocket)
		fmt.Println(connection.RemoteAddr())
		go server.clientSocketManager.receive(clientSocket)
	}
}


