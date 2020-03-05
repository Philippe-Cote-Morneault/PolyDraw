package mode

import (
	"github.com/google/uuid"
	"github.com/tevino/abool"
	"gitlab.com/jigsawcorp/log3900/internal/socket"
	"gitlab.com/jigsawcorp/log3900/model"
	"gitlab.com/jigsawcorp/log3900/pkg/sliceutils"
	"math/rand"
	"strings"
	"sync"
	"time"
)

//FFA Free for all game mode
type FFA struct {
	base
	order  []int
	scores []int //Scores data are in the order of the match so the first user to draw is the first one in the score board
	//We can get the position with the field order
	orderPos    int
	curLap      int
	lapsTotal   int
	rand        *rand.Rand
	timeImage   int
	isRunning   bool
	time        int
	currentWord string

	receiving        sync.Mutex
	receivingGuesses *abool.AtomicBool
	hasFoundit       map[uuid.UUID]bool
	waitingResponse  sync.WaitGroup
}

//Init initialize the game mode
func (f *FFA) Init(connections []uuid.UUID, info model.Group) {
	f.init(connections, info)
	f.rand = rand.New(rand.NewSource(time.Now().UnixNano()))
	f.isRunning = true
	f.hasFoundit = make(map[uuid.UUID]bool, len(connections))
	f.receivingGuesses = abool.New()
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
	f.finish()
}

//Ready registering that it is ready
func (f *FFA) Ready(socketID uuid.UUID) {
	f.ready(socketID)
}

//GameLoop method should be called with start
func (f *FFA) GameLoop() {
	//Choose a user.
	curDrawer := f.players[f.order[f.orderPos]]
	drawingID := uuid.New()
	f.currentWord = f.findWord()
	message := socket.RawMessage{}
	message.ParseMessagePack(byte(socket.MessageType.PlayerDrawThis), PlayerDrawThis{
		Word:      f.currentWord,
		Time:      f.timeImage,
		DrawingID: drawingID.String(),
	})
	socket.SendRawMessageToSocketID(message, curDrawer.socketID)

	message.ParseMessagePack(byte(socket.MessageType.PlayerDrawingTurn), PlayerTurnDraw{
		UserID:    curDrawer.userID.String(),
		Username:  curDrawer.Username,
		Time:      f.timeImage,
		DrawingID: drawingID.String(),
		Length:    len(f.currentWord),
	})
	f.broadcast(&message)
	f.receivingGuesses.Set()

	//Make him draw
	//TODO register with the drawing service the drawing ID to route to the correct users drawing
	f.waitTimeout()

	//Send message that the current word have expired
	timeUpMessage := socket.RawMessage{}
	timeUpMessage.ParseMessagePack(byte(socket.MessageType.TimeUp), TimeUp{
		Type: 1,
		Word: f.currentWord,
	})
	f.broadcast(&timeUpMessage)

	f.orderPos++
	if f.orderPos > len(f.players)-1 {
		f.orderPos = 0
		f.curLap++

		//Is the game finished ?
		if f.curLap > f.lapsTotal-1 {
			f.isRunning = false
			timeUpMessage := socket.RawMessage{}
			timeUpMessage.ParseMessagePack(byte(socket.MessageType.TimeUp), TimeUp{
				Type: 2,
				Word: f.currentWord,
			})
			f.broadcast(&timeUpMessage)
			return
		}
	}
	f.currentWord = ""
}

//Disconnect endpoint for when a user exits
func (f *FFA) Disconnect(socketID uuid.UUID) {
}

//TryWord endpoint for when a user tries to guess a word
func (f *FFA) TryWord(socketID uuid.UUID, word string) {
	f.receiving.Lock()
	if strings.ToLower(strings.TrimSpace(word)) == f.currentWord && f.currentWord != "" {

		//The word was found
		if f.receivingGuesses.IsSet() && !f.hasFoundit[socketID] {
			f.hasFoundit[socketID] = true
			f.waitingResponse.Done()
			f.receiving.Unlock()

			players := f.connections[socketID]
			pointsForWord := 100 //TODO change the point system based with time
			f.scores[players.Order] += pointsForWord

			response := socket.RawMessage{}
			response.ParseMessagePack(byte(socket.MessageType.ResponseGuess), GuessResponse{
				Valid:       true,
				Point:       pointsForWord,
				PointsTotal: f.scores[players.Order],
			})
			socket.SendRawMessageToSocketID(response, socketID)

			//Broadcast to all the other players that the word was found
			broadcast := socket.RawMessage{}
			response.ParseMessagePack(byte(socket.MessageType.WordFound), WordFound{
				Username:    players.Username,
				UserID:      players.userID.String(),
				Point:       pointsForWord,
				PointsTotal: f.scores[players.Order],
			})
			f.broadcast(&broadcast)
		} else {
			f.receiving.Unlock()
		}
	} else {
		f.receiving.Unlock()

		players := f.connections[socketID]
		response := socket.RawMessage{}
		response.ParseMessagePack(byte(socket.MessageType.ResponseGuess), GuessResponse{
			Valid:       false,
			Point:       0,
			PointsTotal: f.scores[players.Order],
		})
		socket.SendRawMessageToSocketID(response, socketID)
	}
}

//IsDrawing endpoint called by the drawing service when a user is drawing. Usefull to detect if a user is AFK
func (f *FFA) IsDrawing(socketID uuid.UUID) {
}

//HintRequested method used by the user when requesting a hint
func (f *FFA) HintRequested(socketID uuid.UUID) {
	//Hint is not available in ffa
	if !f.players[f.order[f.orderPos]].IsCPU {
		message := socket.RawMessage{}
		message.ParseMessagePack(byte(socket.MessageType.ResponseHintMatch), HintResponse{
			Hint:  "",
			Error: "Hints are not available for this player. The drawing player needs to be a virtual player.",
		})
		socket.SendRawMessageToSocketID(message, socketID)
	} else {
		message := socket.RawMessage{}
		message.ParseMessagePack(byte(socket.MessageType.ResponseHintMatch), HintResponse{
			Hint:  "Not implemented", //TODO replace with the real hint from the virtual player
			Error: "",
		})
		socket.SendRawMessageToSocketID(message, socketID)
	}
}

//Close forces the game to stop completely. Graceful shutdown
func (f *FFA) Close() {
}

//GetConnections returns all the socketID of the match
func (f *FFA) GetConnections() []uuid.UUID {
	connections := make([]uuid.UUID, 0, len(f.players))
	for i := range connections {
		connections = append(connections, f.players[i].socketID)
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

func (f *FFA) findWord() string {
	//TODO language
	word, err := model.Redis().SRandMember("dict_words_en").Result()
	if err != nil {
		f.Close()
	}
	return word
}

func (f *FFA) setOrder() {
	choices := make([]int, 0, len(f.players))
	f.order = make([]int, 0, len(f.players))
	for i := range f.players {
		choices[i] = i
	}

	for i := len(choices) - 1; i <= 0; i-- {
		choicePos := f.rand.Intn(i)
		userPos := sliceutils.PopInt(&choices, choicePos)

		f.players[userPos].Order = i
		f.order = append(f.order, userPos)
	}

	f.orderPos = 0

}

func (f *FFA) resetGuess() {
	for i := range f.players {
		f.hasFoundit[f.players[i].socketID] = false
	}

}

func (f *FFA) waitTimeout() bool {
	defer f.receiving.Unlock() //Mutex prevents from having a negative semaphore upon cleanup

	c := make(chan struct{})
	go func() {
		defer close(c)
		f.waitingResponse.Wait()
	}()

	select {
	case <-c:
		f.receiving.Lock()
		f.receivingGuesses.UnSet()
		return false // completed normally
	case <-time.After(time.Duration(f.timeImage)):
		f.receiving.Lock()
		f.receivingGuesses.UnSet()
		//Make sure to clear the semaphore to avoid goroutine leakage
		for k := range f.hasFoundit {
			if !f.hasFoundit[k] {
				f.waitingResponse.Done()
			}
		}
		return true // timed out
	}
}

//finish when the match terminates announce winner
func (f *FFA) finish() {

	//Identify the winner
	bestPlayerOrder := -1
	bestScore := -1
	players := make([]PlayersDataPoint, len(f.scores))

	for i := range f.scores {
		if bestScore < f.scores[i] {
			bestPlayerOrder = i
			bestScore = f.scores[i]
		}
		players[i] = PlayersDataPoint{
			Username: f.players[f.order[i]].Username,
			UserID:   f.players[f.order[i]].userID.String(),
			Point:    f.scores[i],
		}
	}
	winner := f.players[f.order[bestPlayerOrder]]

	//Send a message to all the players to give them the details of the game and who is the winner
	message := socket.RawMessage{}
	message.ParseMessagePack(byte(socket.MessageType.GameEnded), GameEnded{
		Players:    players,
		Winner:     winner.userID.String(),
		WinnerName: winner.Username,
		Time:       f.time,
	})

	f.broadcast(&message)
}
