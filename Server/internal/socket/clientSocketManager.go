package socket

import (
	"fmt"
	"net"
	"sync"

	"github.com/google/uuid"
	"gitlab.com/jigsawcorp/log3900/pkg/cbroadcast"
)

// ClientSocket is a client connected to the socket
type ClientSocket struct {
	socket net.Conn
	id     uuid.UUID
}

// ClientSocketManager manages all client sockets connected and subscribers
type ClientSocketManager struct {
	mutexMap sync.Mutex
	clients  map[uuid.UUID]*ClientSocket
}

func newClientSocketManager() *ClientSocketManager {
	manager := new(ClientSocketManager)
	manager.mutexMap.Lock()

	manager.clients = make(map[uuid.UUID]*ClientSocket)

	manager.mutexMap.Unlock()

	return manager
}

// Registers a new client to the manager. Will listen to messages from this client.
func (manager *ClientSocketManager) registerClient(client *ClientSocket) {
	defer manager.mutexMap.Unlock()
	manager.mutexMap.Lock()

	manager.clients[client.id] = client
}

func (manager *ClientSocketManager) unregisterClient(clientID uuid.UUID) {
	defer manager.mutexMap.Unlock()

	manager.mutexMap.Lock()
	if clientConnection, ok := manager.clients[clientID]; ok {
		manager.close(clientConnection)

		delete(manager.clients, clientID)
	}
}

func (manager *ClientSocketManager) receive(clientSocket *ClientSocket, closing chan bool) {
	defer wg.Done() //Defer so once everything is completed we can return
	cancel := make(chan struct{})
	defer close(cancel) //Defer cancel so we end the thread if no signal is called

	go func() {
		select {
		case <-closing:
			manager.close(clientSocket)
		case <-cancel:
			return
		}
	}()

	cbroadcast.Broadcast(BSocketConnected, clientSocket.id) //Broadcast to all the services that a new connection is made
	for {
		message := make([]byte, 4096)
		length, err := clientSocket.socket.Read(message)
		if err != nil {
			// If the connection is closed, unregister client
			manager.unregisterClient(clientSocket.id)
			fmt.Println("Err inside Read")
			break
		}
		if length > 0 {
			cbroadcast.Broadcast(BSocketReceive, message)
			//TODO parse message or count on a service to do it
		}
	}

}

func (manager *ClientSocketManager) close(clientSocket *ClientSocket) {
	clientSocket.socket.Close()
	cbroadcast.Broadcast(BSocketCloseClient, clientSocket.id) //Broadcast to all the services that the connection is closed
}
