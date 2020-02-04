package healthcheck

import (
	"log"
	"sync/atomic"
	"time"

	"github.com/google/uuid"
	"gitlab.com/jigsawcorp/log3900/internal/socket"
)

func (h *HealthCheck) handleNewHost(socketID uuid.UUID) {
	time.Sleep(time.Second * 10)

	for {

		h.m.Lock()
		h.connections[socketID] = false
		h.m.Unlock()

		message := socket.RawMessage{}
		message.Length = 0
		message.MessageType = byte(socket.MessageType.HealthCheck)
		err := socket.SendRawMessageToSocketID(message, socketID)
		if err != nil {
			// Error in the socket we can close the connection.
			log.Printf("[Healthcheck] -> Connection %s might be closed %s", socketID, err)
			socket.RemoveClientFromID(socketID)
		}

		time.Sleep(time.Second * 10)

		//Check if the client has phoned back home
		h.m.RLock()
		hasPhoned := h.connections[socketID]
		h.m.RUnlock()

		if !hasPhoned {
			log.Printf("[Healthcheck] -> Connection %s did not respond. Closing the connection", socketID)
			socket.RemoveClientFromID(socketID)
			return
		}

		//Atomic check to make sure that the we can exit the loop
		if atomic.LoadInt32(h.closing) == 1 {
			return
		}
	}
}

func (h *HealthCheck) handleCloseHost(socketID uuid.UUID) {
	h.m.Lock()
	delete(h.connections, socketID)
	h.m.Unlock()
}

func (h *HealthCheck) handleReception(message socket.RawMessageReceived) {
	h.m.Lock()
	h.connections[message.SocketID] = true
	h.m.Unlock()
}
