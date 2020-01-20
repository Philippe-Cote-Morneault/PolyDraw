package socket

import (
	"log"
	"net"
	"sync"

	"github.com/google/uuid"
)

// Server represents a Socket server
type Server struct {
	closing             bool
	closingMutex        sync.Mutex
	listener            *net.Listener
	clientSocketManager *ClientSocketManager
}

// StartListening starts listening to incoming socket connections
func (server *Server) StartListening(host string) {
	server.closing = false
	log.Printf("[SOCKET] -> Server is started on %s", host)

	listener, err := net.Listen("tcp", host)
	if err != nil {
		log.Fatal(err)
	}
	server.listener = &listener

	server.clientSocketManager = newClientSocketManager()

	// Listen for new socket connections and create client for each new connection
	for {
		connection, err := (*server.listener).Accept()
		if err != nil {
			server.closingMutex.Lock()
			if !server.closing {
				log.Fatal("[SOCKET] -> ", err)
			}
			server.closingMutex.Unlock()
		}
		clientSocket := &ClientSocket{socket: connection, id: uuid.New()}
		server.clientSocketManager.registerClient(clientSocket)
		go server.clientSocketManager.receive(clientSocket.id)
		server.clientSocketManager.notifyEventSubscribers(SocketEvent.Connection, clientSocket)
	}
}

//Shutdown close the socket properly
func (server *Server) Shutdown() {
	server.closingMutex.Lock()
	server.closing = true
	server.closingMutex.Unlock()

	log.Println("[SOCKET] -> Shutting down the socket server...")
	//TODO send a to all the games to close them
	(*server.listener).Close()
}
