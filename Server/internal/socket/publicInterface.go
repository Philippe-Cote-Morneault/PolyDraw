package socket

import (
	"github.com/google/uuid"
	"github.com/vmihailenco/msgpack/v4"
)

func (manager *ClientSocketManager) Subscribe(messageType int, callback SocketCallback) {
	if _, ok := manager.subscribers[messageType]; !ok {
		manager.subscribers[messageType] = []SocketCallback{}
	}
	manager.subscribers[messageType] = append(manager.subscribers[messageType], callback)
}

// Sends a SocketMessage to the socketId
func (manager *ClientSocketManager) SendMessageToSocketId(message SocketMessage, id uuid.UUID) error {
	if clientConnection, ok := manager.clients[id]; ok {
		serializedMessage, err := msgpack.Marshal(message)
		if err != nil {
			return err
		}
		_, err = clientConnection.socket.Write(serializedMessage)

		return err
	}

	return nil
}

func (manager *ClientSocketManager) SendRawMessageToSocketId(message RawMessage,id uuid.UUID) error {
	if clientConnection, ok := manager.clients[id]; ok {
		_, err := clientConnection.socket.Write(message.ToBytesSlice())
		if err != nil {
			// TODO: Handle error when writing
			return err
		}
	}

	return nil
}

func (manager *ClientSocketManager) SendMessageToUsername(message SocketMessage, username string) {

}