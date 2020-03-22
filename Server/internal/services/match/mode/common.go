package mode

import (
	"log"
	"sync"

	"gitlab.com/jigsawcorp/log3900/internal/services/virtualplayer"

	"github.com/google/uuid"
	match2 "gitlab.com/jigsawcorp/log3900/internal/match"
	"gitlab.com/jigsawcorp/log3900/internal/services/auth"
	"gitlab.com/jigsawcorp/log3900/internal/socket"
	"gitlab.com/jigsawcorp/log3900/model"
)

type players struct {
	socketID uuid.UUID
	userID   uuid.UUID
	Username string
	Order    int
	IsCPU    bool
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
				IsCPU:    false,
			}
			b.connections[socketID] = &b.players[i]
		}
	}

	bots := virtualplayer.GetVirtualPlayersInfo(info.ID)

	if bots != nil {
		if len(bots) == info.VirtualPlayers {
			for _, bot := range bots {
				b.players = append(b.players, players{
					socketID: uuid.Nil,
					userID:   bot.BotID,
					Username: bot.Username,
					IsCPU:    true,
				})
			}
		}
	}

	b.info = info
	b.readyMatch.Add(len(b.connections))

	b.readyOnce = make(map[uuid.UUID]bool)
	for i := range b.connections {
		b.readyOnce[b.connections[i].socketID] = false
	}
	log.Printf("[Match] -> Init match %s", b.info.ID)
}

//broadcast messages to all users not in parallel
func (b *base) broadcast(message *socket.RawMessage) {
	for i := range b.connections {
		socket.SendRawMessageToSocketID(*message, b.connections[i].socketID)
	}
	log.Printf("[Match] -> Message %d broadcasted, Match: %s", message.MessageType, b.info.ID)
}

//pbroadcast use to broadcast to all users in parallel
func (b *base) pbroadcast(message *socket.RawMessage) {
	for i := range b.connections {
		go socket.SendRawMessageToSocketID(*message, b.connections[i].socketID)
	}
	log.Printf("[Match] -> Message %d broadcasted in parallel, Match: %s", message.MessageType, b.info.ID)
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

//getPlayers used to return players needs to be exported by the implementing struct
func (b *base) getPlayers() []match2.Player {
	players := make([]match2.Player, len(b.players))
	for i := range b.players {
		players[i].ID = b.players[i].userID
		players[i].Username = b.players[i].Username
		players[i].IsCPU = b.players[i].IsCPU
	}
	return players
}
