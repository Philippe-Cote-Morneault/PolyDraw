package mode

import (
	"github.com/google/uuid"
	"gitlab.com/jigsawcorp/log3900/internal/socket"
	"gitlab.com/jigsawcorp/log3900/model"
)

type FFA struct {
	base
}

func (f FFA) Init(connections []uuid.UUID, info model.Group) {
	f.init(connections, info)
}

func (f FFA) Start() {
	//Send a message for the players that the game is about to start

	f.waitForPlayers()
	//We can start the game loop
	f.GameLoop()
}

func (f FFA) Ready() {
	f.ready()
}

func (f FFA) GameLoop() {
	//Choose a user.
	//Make him draw
	//Keep track of the scores of every user
	//Check for guessing
	panic("implement me")
}

func (f FFA) Disconnect(socketID uuid.UUID) {
	panic("implement me")
}

func (f FFA) TryWord(socketID uuid.UUID, word string) {
	panic("implement me")
}

func (f FFA) IsDrawing(socketID uuid.UUID) {
	panic("implement me")
}

func (f FFA) HintRequested(socketID uuid.UUID) {
	panic("implement me")
}

func (f FFA) Close() {
	panic("implement me")
}

func (f FFA) GetConnections() []uuid.UUID {
	panic("implement me")
}

func (f FFA) GetWelcome() socket.RawMessage {
	panic("implement me")
}
