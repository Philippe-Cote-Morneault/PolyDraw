package socket

import (
	"encoding/binary"
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
		buffer := make([]byte, minPacketLength)
		TLBuffer := make([]byte, minPacketLength)
		length, err := clientSocket.socket.Read(buffer)

		if err != nil || length == 0 {
			// If the connection is closed, unregister client
			manager.unregisterClient(clientSocket.id)
			break
		}

		if length > 0 {
			//We wait until we have all the three bytes needed for TL of TLV
			copy(TLBuffer, buffer)
			closed := false
			TLLength := length
			for TLLength < 3 {
				buffer := make([]byte, 1) //We get one by one the remaining bytes
				length, err := clientSocket.socket.Read(buffer)

				if err != nil || length == 0 {
					// If the connection is closed, unregister client
					manager.unregisterClient(clientSocket.id)
					closed = true
					break
				}
				copy(TLBuffer[TLLength:], buffer)
				TLLength++
			}
			if closed {
				break
			}

			// We have all the bytes needed to read the rest of the packet
			size := binary.BigEndian.Uint16(TLBuffer[1:3])
			totalLength := int(size) + 3
			typeMessage := TLBuffer[0]

			remainingBytesPacket := totalLength - 3
			messageBytes := make([]byte, totalLength) // Make size for the message
			copy(messageBytes, TLBuffer)

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
					closed = true
					break
				}
				copy(messageBytes[(totalLength-remainingBytesPacket):], buffer[:length])
				remainingBytesPacket -= length
			}
			if closed {
				break
			}

			//Message is complete we can send the broadcast
			message := RawMessage{}
			message.ParseMessage(typeMessage, size, messageBytes)

			cbroadcast.Broadcast(BSocketReceive, message)
		}
	}
}

func (manager *ClientSocketManager) close(clientSocket *ClientSocket) {
	clientSocket.socket.Close()
	cbroadcast.Broadcast(BSocketCloseClient, clientSocket.id) //Broadcast to all the services that the connection is closed
}
