package socket

import "fmt"

type ClientSocketManager struct {
	clients map[*ClientSocket]bool
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
		fmt.Println("Waiting for socket inbound message!")
		message := make([]byte, 4096)
		length, err := client.socket.Read(message)
		if err != nil {
			manager.UnregisterClient(client)
			client.socket.Close()
			break
		}
		if length > 0 {
			fmt.Println("Received message from socket:")
			fmt.Println(length)
			fmt.Println(string(message))
		}
	}
}