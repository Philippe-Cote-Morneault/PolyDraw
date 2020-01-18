package socket

import (
	"github.com/google/uuid"
	"github.com/vmihailenco/msgpack/v4"
)

// The passed callback will be called when messageType event is received. Returns a uuid used to unsubscribe
func (manager *ClientSocketManager) Subscribe(messageType int, callback SocketCallback) uuid.UUID {
	if _, ok := manager.subscribers[messageType]; !ok {
		manager.subscribers[messageType] = make(map[uuid.UUID]SocketCallback)
	}

	callbackId := uuid.New()
	manager.subscribers[messageType][callbackId] = callback

	return callbackId
}

func (manager *ClientSocketManager) Unsubscribe(messageType int, callbackId uuid.UUID) {
	if _, ok := manager.subscribers[messageType]; ok {
		callbacks := manager.subscribers[messageType]
		delete(callbacks, callbackId)
	}
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

// TODO: Implement when we have user service
func (manager *ClientSocketManager) SendMessageToUsername(message SocketMessage, username string) {

}