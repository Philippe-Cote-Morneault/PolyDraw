package drawing

import (
	"github.com/google/uuid"
	match2 "gitlab.com/jigsawcorp/log3900/internal/match"
	"gitlab.com/jigsawcorp/log3900/internal/socket"
	"log"
	"sync"
)

var instanceRouter *Router

//RegisterGame to the drawing service. This way the messages can be routed
func RegisterGame(ptr match2.IMatch) {
	connections := ptr.GetConnections()

	instanceRouter.mutex.Lock()
	instanceRouter.addSocket(connections, &ptr)
	instanceRouter.mutex.Unlock()
	log.Printf("[Drawing] Registered a match with the following connections %v", connections)
}

//UnRegisterSession removes the session from the cache
func UnRegisterSession(socketID uuid.UUID) {
	instanceRouter.mutex.Lock()
	game, ok := instanceRouter.connections[socketID]
	if ok {
		for i := range game.connections {
			if game.connections[i] == socketID {
				max := len(game.connections) - 1

				game.connections[i] = game.connections[max]
				game.connections[max] = uuid.Nil
				game.connections = game.connections[:max]
				break
			}
		}
		delete(instanceRouter.connections, socketID)
	}
	instanceRouter.mutex.Unlock()
}

//UnRegisterGame to the drawing service. This way the program can save memory.
func UnRegisterGame(ptr match2.IMatch) {
	connections := ptr.GetConnections()

	instanceRouter.mutex.Lock()
	for i := range connections {
		instanceRouter.removeSocket(connections[i])
	}
	instanceRouter.mutex.Unlock()
}

type game struct {
	match       *match2.IMatch
	connections []uuid.UUID
}

//Router used to routes the various messages of the drawing
type Router struct {
	mutex       sync.RWMutex
	connections map[uuid.UUID]*game //socketUUID
}

//Init initialize the router and it's pointer
func (r *Router) Init() {
	instanceRouter = r
	r.connections = make(map[uuid.UUID]*game)
}

//addSocket to add all the connections to the local cache
func (r *Router) addSocket(connections []uuid.UUID, ptr *match2.IMatch) {
	game := game{
		match:       ptr,
		connections: connections,
	}
	for i := range connections {
		r.connections[connections[i]] = &game
	}
}

//RemoveSocket to remove a socket id to the router
func (r *Router) removeSocket(socketID uuid.UUID) {
	delete(r.connections, socketID)
}

//Route the message and broadcast it to every other clients
func (r *Router) Route(message *socket.RawMessageReceived) {
	var newMessageType byte
	switch message.Payload.MessageType {
	case 30:
		newMessageType = 31
	case 32:
		newMessageType = 33
	case 34:
		newMessageType = 35
	case 38:
		newMessageType = 39
	default:
		newMessageType = 0
	}

	if newMessageType == 0 {
		panic("Drawing: Wrong message type passed to the drawing router")
	}
	newMessage := socket.RawMessage{
		MessageType: newMessageType,
		Length:      message.Payload.Length,
		Bytes:       message.Payload.Bytes,
	}

	r.mutex.RLock()
	game, ok := r.connections[message.SocketID]
	if ok {
		for i := range game.connections {
			if game.connections[i] != message.SocketID {
				socket.SendQueueMessageSocketID(newMessage, game.connections[i])
			}
		}
	} else {
		log.Printf("[Drawing] cannot find match associated with socket id %s", message.SocketID)
	}
	r.mutex.RUnlock()

}
