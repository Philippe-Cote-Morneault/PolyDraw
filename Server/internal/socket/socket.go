package socket

import (
	"fmt"
	"github.com/google/uuid"
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

	server.clientSocketManager = newClientSocketManager()

	// Listen for new socket connections and create client for each new connection
	for {
		connection, err := (*server.listener).Accept()
		if err != nil {
			fmt.Println(err)
		}
		clientSocket := &ClientSocket{socket: connection, id: uuid.New()}
		server.clientSocketManager.registerClient(clientSocket)
		fmt.Println(connection.RemoteAddr())
		go server.clientSocketManager.receive(clientSocket.id)
	}
}




