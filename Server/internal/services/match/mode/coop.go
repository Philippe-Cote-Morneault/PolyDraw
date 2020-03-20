package mode

import (
	"github.com/google/uuid"
	"gitlab.com/jigsawcorp/log3900/internal/socket"
	"gitlab.com/jigsawcorp/log3900/model"
)

//Coop represent a cooperative game mode
type Coop struct {
	base
}

//Init creates the coop game mode
func (c *Coop) Init(connections []uuid.UUID, info model.Group) {
	c.init(connections, info)
}

//Ready client register to make sure they are ready to start the game
func (c *Coop) Ready(socketID uuid.UUID) {
	panic("implement me")
}

//Start the game and the game loop
func (c *Coop) Start() {
	panic("implement me")
}

//Disconnect handle disconnect for the coop
func (c *Coop) Disconnect(socketID uuid.UUID) {
	panic("implement me")
}

//TryWord handle when a client wants to try a word
func (c *Coop) TryWord(socketID uuid.UUID, word string) {
	panic("implement me")
}

//IsDrawing method not used by coop
func (c *Coop) IsDrawing(socketID uuid.UUID) {
}

//HintRequested for the current virtual player drawing
func (c *Coop) HintRequested(socketID uuid.UUID) {
	panic("implement me")
}

//Close method used to force close the current game
func (c *Coop) Close() {
	panic("implement me")
}

//GetConnections method used to return all the connections of the players
func (c *Coop) GetConnections() []uuid.UUID {
	panic("implement me")
}

//GetWelcome message used for the broadcast of the type of game
func (c *Coop) GetWelcome() socket.RawMessage {
	panic("implement me")
}
