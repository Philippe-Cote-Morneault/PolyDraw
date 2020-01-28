package socket

import (
	"fmt"
	"log"
	"net"
	"sync"

	"github.com/google/uuid"
	"github.com/vmihailenco/msgpack/v4"
	"gitlab.com/jigsawcorp/log3900/pkg/cbroadcast"
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
	cbroadcast.Broadcast(BSocketReady, nil)

	// Listen for new socket connections and create client for each new connection
	for {
		connection, err := (*server.listener).Accept()
		if err != nil {

			server.mutex.Lock()
			if server.running {
				log.Fatal("[SOCKET] -> ", err)
			} else {
				//We exit the function
				return
			}
			server.mutex.Unlock()
		}
		clientSocket := &ClientSocket{socket: connection, id: uuid.New()}

		server.clientSocketManager.registerClient(clientSocket)
		wg.Add(1)
		go server.clientSocketManager.receive(clientSocket, server.closingChannel)
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

	server.mutex.Lock()
	if server.listener != nil {
		(*server.listener).Close()
		cbroadcast.Broadcast(BSocketClose, nil)
	}
	server.mutex.Unlock()
}

// SendMessageToSocketID sends a SerializableMessage to the socketID
//TODO remove possibly
func SendMessageToSocketID(message SerializableMessage, socketID uuid.UUID) error {
	m := clientSocketManagerInstance

	if m == nil {
		return fmt.Errorf("The clientSocketManger was not instanced")
	}

	defer m.mutexMap.Unlock()
	m.mutexMap.Lock()

	if clientConnection, ok := m.clients[socketID]; ok {
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
func SendRawMessageToSocketID(message RawMessage, id uuid.UUID) error {
	m := clientSocketManagerInstance
	if m == nil {
		return fmt.Errorf("The clientSocketManger was not instanced")
	}
	defer m.mutexMap.Unlock()
	m.mutexMap.Lock()
	if clientConnection, ok := m.clients[id]; ok {
		_, err := clientConnection.socket.Write(message.ToBytesSlice())
		if err != nil {
			// TODO: Handle error when writing
			return err
		}
	}

	return nil
}

// RemoveClientFromID removes a client socket from the ClientID.
func RemoveClientFromID(clientID uuid.UUID) error {
	m := clientSocketManagerInstance
	if m == nil {
		return fmt.Errorf("The clientSocketManger was not instanced")
	}
	m.unregisterClient(clientID)
	//TODO error management
	return nil
}
