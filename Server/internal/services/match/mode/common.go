package mode

import (
	"github.com/google/uuid"
	"gitlab.com/jigsawcorp/log3900/internal/socket"
	"gitlab.com/jigsawcorp/log3900/model"
	"sync"
)

type base struct {
	readyMatch  sync.WaitGroup
	readyOnce   map[uuid.UUID]bool
	connections []uuid.UUID
	info        model.Group
}

func (b *base) init(connections []uuid.UUID, info model.Group) {
	b.connections = make([]uuid.UUID, len(connections))
	copy(b.connections, connections)
	b.info = info
	b.readyMatch.Add(len(b.connections))

	b.readyOnce = make(map[uuid.UUID]bool)
	for i := range b.connections {
		b.readyOnce[b.connections[i]] = false
	}

}

func (b *base) broadcast(message *socket.RawMessage) {
	for i := range b.connections {
		socket.SendRawMessageToSocketID(*message, b.connections[i])
	}
}

//Wait for all the clients to be ready
func (b *base) waitForPlayers() {
	//TODO include a timeout in case a client drops the connection to avoid a deadlock
	b.readyMatch.Wait()

	//Send a message to all the clients to advise them that the game is starting
	message := socket.RawMessage{}
	message.MessageType = byte(socket.MessageType.GameStarting)
	b.broadcast(&message)
}

func (b *base) ready(socketID uuid.UUID) {
	if !b.readyOnce[socketID] {
		b.readyMatch.Done()
		b.readyOnce[socketID] = true
	}
}
