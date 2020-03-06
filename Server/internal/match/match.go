package match

import (
	"github.com/google/uuid"
	"gitlab.com/jigsawcorp/log3900/internal/socket"
	"gitlab.com/jigsawcorp/log3900/model"
)

//IMatch implement the logic of a game match
type IMatch interface {
	//Initialisation needed for the type of match, It passes data about the initial game and all the connections that
	//are waiting for the game
	Init(connections []uuid.UUID, info model.Group)

	//Ready called by every clients to tell the server that they are ready to start the game
	Ready(socketID uuid.UUID)

	//Start is the method used to start the game loop. There should be the waiting for all the clients
	Start()

	//Disconnect used to handle the client disconnection in the match
	//this call can either be a hard disconnect (socket closing) or one where the socket remains alive
	Disconnect(socketID uuid.UUID)

	//TryWord used to handle the requests from the client when they try a word
	TryWord(socketID uuid.UUID, word string)

	//IsDrawing is called every time one of the player is drawing on the screen. In case of a virtual player id null is used
	IsDrawing(socketID uuid.UUID)

	//HintRequested is called when a player is requesting a hint
	HintRequested(socketID uuid.UUID)

	//Close method used to close the game in progress. For example the server is shutting down
	Close()

	//GetConnections returns all the connections that are current in the game.
	GetConnections() []uuid.UUID

	//GetWelcome returns the message that the game manager needs to send before all the users can register
	GetWelcome() socket.RawMessage
}
