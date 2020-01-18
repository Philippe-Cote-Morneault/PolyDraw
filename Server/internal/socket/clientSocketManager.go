package socket

import (
	"fmt"
	"github.com/vmihailenco/msgpack/v4"
)

type ClientSocketManager struct {
	clients map[*ClientSocket]bool
	subscribers map[int][]SocketCallback
}

// Registers a new client to the manager. Will listen to messages from this client.
func (manager *ClientSocketManager) RegisterClient(client *ClientSocket)  {
	manager.clients[client] = true
}

func (manager *ClientSocketManager) UnregisterClient(client *ClientSocket)  {
	if connectionActive := manager.clients[client]; connectionActive {
		client.socket.Close()
		delete(manager.clients, client)
	}
}

func (manager *ClientSocketManager) receive(client *ClientSocket) {
	for {
		message := make([]byte, 4096)
		length, err := client.socket.Read(message)
		if err != nil {
			manager.UnregisterClient(client)
			client.socket.Close()
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
	}
}

func (manager *ClientSocketManager) Subscribe(messageType int, callback SocketCallback) {
	if _, ok := manager.subscribers[messageType]; !ok {
		manager.subscribers[messageType] = []SocketCallback{}
	}
	manager.subscribers[messageType] = append(manager.subscribers[messageType], callback)
	fmt.Println(len(manager.subscribers[messageType]))
}

func (manager *ClientSocketManager) notifySubscribers(message SocketMessage) {
	if callbacks, ok := manager.subscribers[message.Type]; ok {
		for _, callback := range callbacks {
			// TODO: Figure out if sender will be username or id
			callback(message, "")
		}
	}
}