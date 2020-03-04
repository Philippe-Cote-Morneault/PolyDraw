package mode

import (
	"github.com/google/uuid"
	"gitlab.com/jigsawcorp/log3900/internal/services/auth"
	"gitlab.com/jigsawcorp/log3900/internal/socket"
	"gitlab.com/jigsawcorp/log3900/model"
	"sync"
)

type players struct {
	socketID uuid.UUID
	userID   uuid.UUID
	Username string
}

type base struct {
	readyMatch  sync.WaitGroup
	readyOnce   map[uuid.UUID]bool
	connections []players
	info        model.Group
}

func (b *base) init(connections []uuid.UUID, info model.Group) {
	b.connections = make([]players, len(connections))
	for i := range connections {
		socketID := connections[i]
		userID, _ := auth.GetUserID(socketID)
		//Find the user data in the game info
		var user *model.User
		for j := range info.Users {
			if info.Users[j].ID == userID {
				user = info.Users[j]
			}
		}
		if user != nil && userID != uuid.Nil {
			b.connections[i] = players{
				socketID: socketID,
				userID:   userID,
				Username: user.Username,
			}
		}
	}

	b.info = info
	b.readyMatch.Add(len(b.connections))

	b.readyOnce = make(map[uuid.UUID]bool)
	for i := range b.connections {
		b.readyOnce[b.connections[i].socketID] = false
	}

}

func (b *base) broadcast(message *socket.RawMessage) {
	for i := range b.connections {
		socket.SendRawMessageToSocketID(*message, b.connections[i].socketID)
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
