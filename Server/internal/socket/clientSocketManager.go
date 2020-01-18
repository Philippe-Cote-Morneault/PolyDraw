package socket

import (
	"github.com/google/uuid"
	"github.com/vmihailenco/msgpack/v4"
)

type ClientSocketManager struct {
	clients map[uuid.UUID]*ClientSocket
	subscribers map[int][]SocketCallback
}

// Registers a new client to the manager. Will listen to messages from this client.
func (manager *ClientSocketManager) registerClient(client *ClientSocket)  {
	manager.clients[client.id] = client
}

func (manager *ClientSocketManager) unregisterClient(clientId uuid.UUID)  {
	if clientConnection, ok := manager.clients[clientId]; ok {
		clientConnection.socket.Close()
		delete(manager.clients, clientId)
	}
}

func (manager *ClientSocketManager) receive(clientId uuid.UUID) {
	for {
		if clientConnection, ok := manager.clients[clientId]; ok {
			message := make([]byte, 4096)
			length, err := clientConnection.socket.Read(message)
			if err != nil {
				// If the connection is closed, unregister client
				manager.unregisterClient(clientId)
				clientConnection.socket.Close()
				break
			}
			if length > 0 {
				var socketMessage SocketMessage
				err := msgpack.Unmarshal(message, &socketMessage)
				if err != nil {
					// TODO Handle this error
					break
				}
				manager.notifySubscribers(socketMessage)
			}
		} else {
			// Client connection does not exist anymore
			// TODO: Handle trying to read from connection when it doesn't exist anymore
			break
		}
	}
}

func (manager *ClientSocketManager) notifySubscribers(message SocketMessage) {
	if callbacks, ok := manager.subscribers[message.Type]; ok {
		for _, callback := range callbacks {
			// TODO: Figure out if sender will be username or id
			callback(message, "")
		}
	}
}

