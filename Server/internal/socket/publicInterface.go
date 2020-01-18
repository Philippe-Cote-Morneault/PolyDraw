package socket

import (
	"github.com/google/uuid"
	"github.com/vmihailenco/msgpack/v4"
)

// SubscribeToMessage associates a callback to a message type. When a message is received on a socket with the specified message type, the callback
// will be called. Returns a uuid to identify the callback. The uuuid is used to unsubscribe.
func (manager *ClientSocketManager) SubscribeToMessage(messageType int, callback MessageCallback) uuid.UUID {
	if _, ok := manager.messageSubscribers[messageType]; !ok {
		manager.messageSubscribers[messageType] = make(map[uuid.UUID]MessageCallback)
	}

	callbackID := uuid.New()
	manager.messageSubscribers[messageType][callbackID] = callback

	return callbackID
}

// UnsubscribeFromMessage removes a message callback from the subscriber list.
func (manager *ClientSocketManager) UnsubscribeFromMessage(messageType int, callbackID uuid.UUID) {
	if _, ok := manager.messageSubscribers[messageType]; ok {
		callbacks := manager.messageSubscribers[messageType]
		delete(callbacks, callbackID)
	}
}

// SubscribeToEvent associates a callback to a socket event type. When a socket event occurs, the callback will be called.
// Returns a uuid to identify the callback. The uuuid is used to unsubscribe.
func (manager *ClientSocketManager) SubscribeToEvent(eventType int, callback EventCallback) uuid.UUID {
	if _, ok := manager.eventSubscribers[eventType]; !ok {
		manager.eventSubscribers[eventType] = make(map[uuid.UUID]EventCallback)
	}

	callbackID := uuid.New()
	manager.eventSubscribers[eventType][callbackID] = callback

	return callbackID
}

// UnsubscribeFromEvent removes an event callback from the subscriber list.
func (manager *ClientSocketManager) UnsubscribeFromEvent(eventType int, callbackID uuid.UUID) {
	if _, ok := manager.eventSubscribers[eventType]; ok {
		callbacks := manager.eventSubscribers[eventType]
		delete(callbacks, callbackID)
	}
}

// SendMessageToSocketID sends a SocketMessage to the socketID
func (manager *ClientSocketManager) SendMessageToSocketID(message SocketMessage, socketID uuid.UUID) error {
	if clientConnection, ok := manager.clients[socketID]; ok {
		serializedMessage, err := msgpack.Marshal(message)
		if err != nil {
			return err
		}
		_, err = clientConnection.socket.Write(serializedMessage)

		return err
	}

	return nil
}

// SendRawMessageToSocketID sends a message to a socket with the specified id with raw bytes.
func (manager *ClientSocketManager) SendRawMessageToSocketID(message RawMessage, id uuid.UUID) error {
	if clientConnection, ok := manager.clients[id]; ok {
		_, err := clientConnection.socket.Write(message.ToBytesSlice())
		if err != nil {
			// TODO: Handle error when writing
			return err
		}
	}

	return nil
}

// SendMessageToUsername sends a message to a client socket from username associated to the user of the socket.
func (manager *ClientSocketManager) SendMessageToUsername(message SocketMessage, username string) {
	// TODO: Implement when we have user service
}

// RemoveClientFromID removes a client socket from the ClientID.
func (manager *ClientSocketManager) RemoveClientFromID(clientID uuid.UUID) error {
	if clientConnection, ok := manager.clients[clientID]; ok {
		err := clientConnection.socket.Close()

		// Remove from client map if socket is successfully closed
		if err == nil {
			delete(manager.clients, clientID)
		}

		return err
	}

	return nil
}
