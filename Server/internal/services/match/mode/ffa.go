package mode

import (
	"github.com/google/uuid"
	"gitlab.com/jigsawcorp/log3900/internal/services/match"
	"gitlab.com/jigsawcorp/log3900/internal/socket"
	"gitlab.com/jigsawcorp/log3900/model"
)

//FFA Free for all game mode
type FFA struct {
	base
}

//Init initialize the game mode
func (f FFA) Init(connections []uuid.UUID, info model.Group) {
	f.init(connections, info)
}

//Start the game mode
func (f FFA) Start() {
	f.waitForPlayers()
	//We can start the game loop
	f.GameLoop()
}

//Ready registering that it is ready
func (f FFA) Ready() {
	f.ready()
}

//GameLoop method should be called with start
func (f FFA) GameLoop() {
	//Choose a user.
	//Make him draw
	//Keep track of the scores of every user
	//Check for guessing
}

//Disconnect endpoint for when a user exits
func (f FFA) Disconnect(socketID uuid.UUID) {
}

//TryWord endpoint for when a user tries to guess a word
func (f FFA) TryWord(socketID uuid.UUID, word string) {
}

//IsDrawing endpoint called by the drawing service when a user is drawing. Usefull to detect if a user is AFK
func (f FFA) IsDrawing(socketID uuid.UUID) {
}

//HintRequested method used by the user when requesting a hint
func (f FFA) HintRequested(socketID uuid.UUID) {
}

//Close forces the game to stop completely. Graceful shutdown
func (f FFA) Close() {
}

//GetConnections returns all the socketID of the match
func (f FFA) GetConnections() []uuid.UUID {
}

//GetWelcome returns a packet to send to all the players. Presents various details about the game
func (f FFA) GetWelcome() socket.RawMessage {
	players := make([]match.PlayersData, 0, len(f.info.Users))
	for i := range f.info.Users {
		players = append(players, match.PlayersData{
			UserID:   f.info.Users[i].ID.String(),
			Username: f.info.Users[i].Username,
			IsCPU:    false,
		})
	}
	//TODO parameters ??
	welcome := match.ResponseGameInfo{
		Players:   players,
		GameType:  0,
		TimeImage: 30000,
		Laps:      3,
		TotalTime: 0,
	}
	message := socket.RawMessage{}
	message.ParseMessagePack(byte(socket.MessageType.GameWelcome), welcome)
	return message

}
