package socket

import (
	"github.com/google/uuid"
	"github.com/vmihailenco/msgpack/v4"
	"time"
)

// ClientSocketManager manages all client sockets connected and subscribers
type ClientSocketManager struct {
	clients            map[uuid.UUID]*ClientSocket
	messageSubscribers map[int]map[uuid.UUID]MessageCallback
	eventSubscribers   map[int]map[uuid.UUID]EventCallback
}

func newClientSocketManager() *ClientSocketManager {
	manager := new(ClientSocketManager)
	manager.clients = make(map[uuid.UUID]*ClientSocket)
	manager.messageSubscribers = make(map[int]map[uuid.UUID]MessageCallback)
	manager.eventSubscribers = make(map[int]map[uuid.UUID]EventCallback)

	return manager
}

// Registers a new client to the manager. Will listen to messages from this client.
func (manager *ClientSocketManager) registerClient(client *ClientSocket) {
	manager.clients[client.id] = client
}

func (manager *ClientSocketManager) unregisterClient(clientID uuid.UUID) {
	if clientConnection, ok := manager.clients[clientID]; ok {
		clientConnection.socket.Close()
		delete(manager.clients, clientID)
	}
}

func (manager *ClientSocketManager) receive(clientID uuid.UUID) {
	for {
		if clientConnection, ok := manager.clients[clientID]; ok {
			message := make([]byte, 4096)
			length, err := clientConnection.socket.Read(message)
			if err != nil {
				// If the connection is closed, unregister client
				manager.unregisterClient(clientID)
				clientConnection.socket.Close()
				manager.notifyEventSubscribers(SocketEvent.Disconnection, clientConnection)
				break
			}
			if length > 0 {
				var socketMessage SerializableMessage
				err := msgpack.Unmarshal(message, &socketMessage)
				if err != nil {
					// TODO Handle this error
					break
				}
				manager.notifyMessageSubscribers(socketMessage)
			}
		} else {
			// Client connection does not exist anymore
			// TODO: Handle trying to read from connection when it doesn't exist anymore
			break
		}
	}
}

func (manager *ClientSocketManager) notifyMessageSubscribers(message SerializableMessage) {
	if callbacks, ok := manager.messageSubscribers[message.Type]; ok {
		for _, callback := range callbacks {
			// TODO: Figure out if sender will be username or id
			callback(message, "")
		}
	}
}

func (manager *ClientSocketManager) notifyEventSubscribers(eventType int, client *ClientSocket) {
	if callbacks, ok := manager.eventSubscribers[eventType]; ok {
		for _, callback := range callbacks {
			// TODO: Figure out if sender will be username or id
			callback(client, time.Now())
		}
	}
}
