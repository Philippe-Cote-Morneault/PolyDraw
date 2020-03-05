package drawing

import (
	"github.com/google/uuid"
	match2 "gitlab.com/jigsawcorp/log3900/internal/match"
	"gitlab.com/jigsawcorp/log3900/internal/socket"
	"sync"
)

var instanceRouter *Router

//RegisterGame to the drawing service. This way the messages can be routed
func RegisterGame(ptr match2.IMatch) {
	connections := ptr.GetConnections()

	instanceRouter.mutex.Lock()
	for i := range connections {
		instanceRouter.connections[connections[i]] = &ptr
	}
	instanceRouter.mutex.Unlock()
}

//UnRegisterGame to the drawing service. This way the program can save memory.
func UnRegisterGame(ptr match2.IMatch) {
	connections := ptr.GetConnections()

	instanceRouter.mutex.Lock()
	for i := range connections {
		delete(instanceRouter.connections, connections[i])
	}
	instanceRouter.mutex.Unlock()
}

//Router used to routes the various messages of the drawing
type Router struct {
	mutex       sync.RWMutex
	connections map[uuid.UUID]*match2.IMatch //socketUUID
}

//Init initialize the router and it's pointer
func (r *Router) Init() {
	instanceRouter = r
	r.connections = make(map[uuid.UUID]*match2.IMatch)
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
	matchPtr := r.connections[message.SocketID]
	r.mutex.RUnlock()

	connections := (*matchPtr).GetConnections()
	for i := range connections {
		if connections[i] != message.SocketID {
			go socket.SendRawMessageToSocketID(newMessage, connections[i])
		}
	}

}
