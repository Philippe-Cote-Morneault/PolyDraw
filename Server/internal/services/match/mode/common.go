package mode

import (
	"github.com/google/uuid"
	"gitlab.com/jigsawcorp/log3900/internal/socket"
	"gitlab.com/jigsawcorp/log3900/model"
	"sync"
)

type base struct {
	readyMatch  sync.WaitGroup
	connections []uuid.UUID
	info        model.Group
}

func (b *base) init(connections []uuid.UUID, info model.Group) {
	b.info = info
	b.connections = connections
}

func (b *base) broadcast(message *socket.RawMessage) {
	for i := range b.connections {
		socket.SendRawMessageToSocketID(*message, b.connections[i])
	}
}

//queueWait make sure that the client connections are added to the semaphore
func (b *base) queueWait() {
	b.readyMatch.Add(len(b.connections))
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

func (b *base) ready() {
	b.readyMatch.Done()
}
