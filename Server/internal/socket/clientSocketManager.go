package socket

import (
	"encoding/binary"
	"log"
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
	const buffSize = 4096
	const minPacketLength = 3

	for {
		buffer := make([]byte, buffSize)
		length, err := clientSocket.socket.Read(buffer)
		if err != nil {
			// If the connection is closed, unregister client
			manager.unregisterClient(clientSocket.id)
			break
		}
		if length < buffSize && length > minPacketLength {
			offset := 0
			remainingBytes := length

			for remainingBytes > minPacketLength {
				size := int(binary.BigEndian.Uint16(buffer[1+offset:3+offset])) + 3
				message := make([]byte, size)

				copy(message, buffer[offset:size+offset])
				cbroadcast.Broadcast(BSocketReceive, message)

				offset += size
				remainingBytes -= size

				if remainingBytes < 0 {
					//TODO missing bytes
					log.Printf("Bad formatting missing bytes according to size")
				}

			}
			if remainingBytes > 0 {
				//TODO remaining bytes
				log.Printf("Bad formatting residuals bytes")
			}
		} else if length == buffSize {
			//The message is not complete we must get the size and the remaining bytes
			size := int(binary.BigEndian.Uint16(buffer[1:3])) + 3
			remainingBytesPacket := size - length
			message := make([]byte, size) // Make size for the message
			copy(message, buffer)

			for remainingBytesPacket > 0 {

				if remainingBytesPacket < buffSize {
					buffer = make([]byte, remainingBytesPacket)
				} else {
					buffer = make([]byte, buffSize)
				}

				length, err := clientSocket.socket.Read(buffer)
				if err != nil {
					// If the connection is closed, unregister client
					manager.unregisterClient(clientSocket.id)
					break
				}
				copy(message[(size-remainingBytesPacket):], buffer[:length])
				remainingBytesPacket -= length
			}
			//Message is complete we can send the broadcast
			cbroadcast.Broadcast(BSocketReceive, message)
		}
	}

}

func (manager *ClientSocketManager) close(clientSocket *ClientSocket) {
	clientSocket.socket.Close()
	cbroadcast.Broadcast(BSocketCloseClient, clientSocket.id) //Broadcast to all the services that the connection is closed
}
