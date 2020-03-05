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
	Order    int
}

type base struct {
	readyMatch  sync.WaitGroup
	readyOnce   map[uuid.UUID]bool
	players     []players
	connections map[uuid.UUID]*players
	info        model.Group
}

func (b *base) init(connections []uuid.UUID, info model.Group) {
	b.players = make([]players, len(connections))
	b.connections = make(map[uuid.UUID]*players, len(connections))
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
			b.players[i] = players{
				socketID: socketID,
				userID:   userID,
				Username: user.Username,
			}
			b.connections[socketID] = &b.players[i]
		}
	}

	b.info = info
	b.readyMatch.Add(len(b.players))

	b.readyOnce = make(map[uuid.UUID]bool)
	for i := range b.players {
		b.readyOnce[b.players[i].socketID] = false
	}

}

func (b *base) broadcast(message *socket.RawMessage) {
	for i := range b.players {
		socket.SendRawMessageToSocketID(*message, b.players[i].socketID)
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
