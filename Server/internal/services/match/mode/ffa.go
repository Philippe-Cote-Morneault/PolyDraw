package mode

import (
	"github.com/google/uuid"
	"gitlab.com/jigsawcorp/log3900/internal/socket"
	"gitlab.com/jigsawcorp/log3900/model"
	"gitlab.com/jigsawcorp/log3900/pkg/sliceutils"
	"math/rand"
	"time"
)

//FFA Free for all game mode
type FFA struct {
	base
	order     []int
	orderPos  int
	curLap    int
	lapsTotal int
	rand      *rand.Rand
	timeImage int
	isRunning bool
}

//Init initialize the game mode
func (f *FFA) Init(connections []uuid.UUID, info model.Group) {

	f.init(connections, info)
	f.rand = rand.New(rand.NewSource(time.Now().UnixNano()))
	f.isRunning = true

}

//Start the game mode
func (f *FFA) Start() {
	f.waitForPlayers()

	//Generate players positions
	f.setOrder()

	//We can start the game loop
	for f.isRunning {
		f.GameLoop()
	}
}

//Ready registering that it is ready
func (f *FFA) Ready(socketID uuid.UUID) {
	f.ready(socketID)
}

//GameLoop method should be called with start
func (f *FFA) GameLoop() {
	//Choose a user.
	curDrawer := f.connections[f.order[f.orderPos]]
	drawingID := uuid.New()
	word := ""
	message := socket.RawMessage{}
	//TODO find a word
	message.ParseMessagePack(byte(socket.MessageType.PlayerDrawThis), PlayerDrawThis{
		Word:      word,
		Time:      f.timeImage,
		DrawingID: drawingID.String(),
	})
	socket.SendRawMessageToSocketID(message, curDrawer.socketID)

	message.ParseMessagePack(byte(socket.MessageType.PlayerDrawingTurn), PlayerTurnDraw{
		UserID:    curDrawer.userID.String(),
		Username:  curDrawer.Username,
		Time:      f.timeImage,
		DrawingID: drawingID.String(),
		Length:    len(word),
	})
	f.broadcast(&message)
	//Make him draw
	//TODO register with the drawing service the drawing ID to route to the correct users
	time.Sleep(time.Millisecond * time.Duration(f.timeImage))

	f.orderPos++
	if f.orderPos > len(f.connections)-1 {
		f.orderPos = 0
		f.curLap++

		//Is the game finished ?
		if f.curLap > f.lapsTotal-1 {
			f.isRunning = false
			return
		}
	}
}

//Disconnect endpoint for when a user exits
func (f *FFA) Disconnect(socketID uuid.UUID) {
}

//TryWord endpoint for when a user tries to guess a word
func (f *FFA) TryWord(socketID uuid.UUID, word string) {
}

//IsDrawing endpoint called by the drawing service when a user is drawing. Usefull to detect if a user is AFK
func (f *FFA) IsDrawing(socketID uuid.UUID) {
}

//HintRequested method used by the user when requesting a hint
func (f *FFA) HintRequested(socketID uuid.UUID) {
}

//Close forces the game to stop completely. Graceful shutdown
func (f *FFA) Close() {
}

//GetConnections returns all the socketID of the match
func (f *FFA) GetConnections() []uuid.UUID {
	connections := make([]uuid.UUID, 0, len(f.connections))
	for i := range connections {
		connections = append(connections, f.connections[i].socketID)
	}
	return connections
}

//GetWelcome returns a packet to send to all the players. Presents various details about the game
func (f *FFA) GetWelcome() socket.RawMessage {
	players := make([]PlayersData, 0, len(f.info.Users))
	for i := range f.info.Users {
		players = append(players, PlayersData{
			UserID:   f.info.Users[i].ID.String(),
			Username: f.info.Users[i].Username,
			IsCPU:    false,
		})
	}
	//TODO parameters ??
	f.timeImage = 30000
	f.lapsTotal = 3
	welcome := ResponseGameInfo{
		Players:   players,
		GameType:  0,
		TimeImage: f.timeImage,
		Laps:      f.lapsTotal,
		TotalTime: 0,
	}
	message := socket.RawMessage{}
	message.ParseMessagePack(byte(socket.MessageType.GameWelcome), welcome)
	return message

}

func (f *FFA) setOrder() {
	choices := make([]int, 0, len(f.connections))
	f.order = make([]int, 0, len(f.connections))
	for i := range f.connections {
		choices[i] = i
	}

	for i := len(choices) - 1; i <= 0; i-- {
		choicePos := f.rand.Intn(i)
		f.order = append(f.order, sliceutils.PopInt(&choices, choicePos))
	}

	f.orderPos = 0

}

//finish when the match terminates announce winner
func (f *FFA) finish() {
	//Send a message to all the players to give them the details of the game and who is the winner
}
