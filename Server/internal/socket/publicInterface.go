package socket

import (
	"github.com/google/uuid"
	"github.com/vmihailenco/msgpack/v4"
)

// The passed callback will be called when messageType event is received. Returns a uuid used to unsubscribe
func (manager *ClientSocketManager) SubscribeToMessage(messageType int, callback MessageCallback) uuid.UUID {
	if _, ok := manager.messageSubscribers[messageType]; !ok {
		manager.messageSubscribers[messageType] = make(map[uuid.UUID]MessageCallback)
	}

	callbackId := uuid.New()
	manager.messageSubscribers[messageType][callbackId] = callback

	return callbackId
}

func (manager *ClientSocketManager) UnsubscribeFromMessage(messageType int, callbackId uuid.UUID) {
	if _, ok := manager.messageSubscribers[messageType]; ok {
		callbacks := manager.messageSubscribers[messageType]
		delete(callbacks, callbackId)
	}
}

// The passed callback will be called when eventType event is received. Returns a uuid used to unsubscribe
func (manager *ClientSocketManager) SubscribeToEvent(eventType int, callback EventCallback) uuid.UUID {
	if _, ok := manager.eventSubscribers[eventType]; !ok {
		manager.eventSubscribers[eventType] = make(map[uuid.UUID]EventCallback)
	}

	callbackId := uuid.New()
	manager.eventSubscribers[eventType][callbackId] = callback

	return callbackId
}

func (manager *ClientSocketManager) UnsubscribeFromEvent(eventType int, callbackId uuid.UUID) {
	if _, ok := manager.eventSubscribers[eventType]; ok {
		callbacks := manager.eventSubscribers[eventType]
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

func (manager *ClientSocketManager) SendRawMessageToSocketId(message RawMessage, id uuid.UUID) error {
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

func (manager *ClientSocketManager) RemoveClientFromId(clientId uuid.UUID) error {
	if clientConnection, ok := manager.clients[clientId]; ok {
		err := clientConnection.socket.Close()

		// Remove from client map if socket is successfully closed
		if err == nil {
			delete(manager.clients, clientId)
		}

		return err
	}

	return nil
}
