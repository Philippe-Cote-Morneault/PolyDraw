package socket

import (
	"log"
	"net"
	"sync"

	"github.com/google/uuid"
	"github.com/vmihailenco/msgpack/v4"
)

// Server represents a Socket server
type Server struct {
	running             bool
	mutex               sync.Mutex
	closingChannel      chan bool
	listener            *net.Listener
	clientSocketManager *ClientSocketManager
}

var wg sync.WaitGroup //Wait group used for shutdown

// StartListening starts listening to incoming socket connections
func (server *Server) StartListening(host string) {
	server.mutex.Lock()

	server.running = true
	server.closingChannel = make(chan bool)
	log.Printf("[SOCKET] -> Server is started on %s", host)

	listener, err := net.Listen("tcp", host)
	if err != nil {
		log.Fatal(err)
	}
	server.listener = &listener

	server.mutex.Unlock()
	server.clientSocketManager = newClientSocketManager()

	// Listen for new socket connections and create client for each new connection
	for {
		connection, err := (*server.listener).Accept()
		if err != nil {

			server.mutex.Lock()
			if server.running {
				log.Fatal("[SOCKET] -> ", err)
			}
			server.mutex.Unlock()
		}
		clientSocket := &ClientSocket{socket: connection, id: uuid.New()}
		server.clientSocketManager.registerClient(clientSocket)

		wg.Add(1)
		go server.clientSocketManager.receive(clientSocket.id, server.closingChannel)

		server.clientSocketManager.notifyEventSubscribers(SocketEvent.Connection, clientSocket)
	}
}

//Shutdown close the socket properly
func (server *Server) Shutdown() {
	server.mutex.Lock()
	server.running = false
	server.mutex.Unlock()

	close(server.closingChannel)

	wg.Wait() //Wait for all the receivers to end

	log.Println("[SOCKET] -> Shutting down the socket server...")
	//TODO send a to all the games to close them

	server.mutex.Lock()
	if server.listener != nil {
		(*server.listener).Close()
	}
	server.mutex.Unlock()
}

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

// SendMessageToSocketID sends a SerializableMessage to the socketID
func (manager *ClientSocketManager) SendMessageToSocketID(message SerializableMessage, socketID uuid.UUID) error {
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
func (manager *ClientSocketManager) SendMessageToUsername(message SerializableMessage, username string) {
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
