package mode

import (
	"context"
	"log"
	"math/rand"
	"sort"
	"strings"
	"sync"
	"time"

	"github.com/google/uuid"
	"github.com/tevino/abool"
	"gitlab.com/jigsawcorp/log3900/internal/services/drawing"
	"gitlab.com/jigsawcorp/log3900/internal/socket"
	"gitlab.com/jigsawcorp/log3900/model"
	"gitlab.com/jigsawcorp/log3900/pkg/sliceutils"
	"golang.org/x/sync/semaphore"
)

const numberOfTurns = 3
const imageDuration = 60000

//FFA Free for all game mode
type FFA struct {
	base
	order  []int
	scores []int //Scores data are in the order of the match so the first user to draw is the first one in the score board
	//We can get the position with the field order
	orderPos       int
	curLap         int
	curDrawer      *players
	lapsTotal      int
	realPlayers    int
	rand           *rand.Rand
	timeImage      int64
	isRunning      bool
	currentWord    string
	timeStart      time.Time
	timeStartImage time.Time

	receiving          sync.Mutex
	receivingGuesses   *abool.AtomicBool
	hasFoundit         map[uuid.UUID]bool
	clientGuess        int
	waitingResponse    *semaphore.Weighted
	cancelWait         func()
	nbWaitingResponses int64
}

//Init initialize the game mode
func (f *FFA) Init(connections []uuid.UUID, info model.Group) {
	f.init(connections, info)
	f.rand = rand.New(rand.NewSource(time.Now().UnixNano()))
	f.isRunning = true
	f.hasFoundit = make(map[uuid.UUID]bool, len(connections))
	f.receivingGuesses = abool.New()
	f.scores = make([]int, len(f.players))

	f.curLap = 1
	f.timeImage = imageDuration
	f.lapsTotal = len(f.players) * numberOfTurns

	f.realPlayers = 0
	for i := range f.players {
		if !f.players[i].IsCPU {
			f.realPlayers++
		}
	}
	drawing.RegisterGame(f)
}

//Start the game mode
func (f *FFA) Start() {

	f.waitForPlayers()

	//Generate players positions
	f.SetOrder()

	//We can start the game loop
	log.Printf("[Match] [FFA] -> Starting gameloop Match: %s", f.info.ID)
	f.timeStart = time.Now()
	for f.isRunning {
		f.GameLoop()
	}
	f.finish()
}

//Ready registering that it is ready
func (f *FFA) Ready(socketID uuid.UUID) {
	defer f.receiving.Unlock()
	f.receiving.Lock()

	f.ready(socketID)
}

//GameLoop method should be called with start
func (f *FFA) GameLoop() {
	//Choose a user.
	f.curDrawer = &f.players[f.order[f.orderPos]]
	drawingID := uuid.New()

	if f.curDrawer.IsCPU {
		f.nbWaitingResponses = int64(f.realPlayers)
	} else {
		f.nbWaitingResponses = int64(f.realPlayers - 1)
		f.hasFoundit[f.curDrawer.socketID] = true
	}
	f.waitingResponse = semaphore.NewWeighted(f.nbWaitingResponses)
	f.waitingResponse.TryAcquire(f.nbWaitingResponses)
	f.currentWord = f.findWord()

	message := socket.RawMessage{}
	message.ParseMessagePack(byte(socket.MessageType.PlayerDrawThis), PlayerDrawThis{
		Word:      f.currentWord,
		Time:      f.timeImage,
		DrawingID: drawingID.String(),
	})
	socket.SendRawMessageToSocketID(message, f.curDrawer.socketID)

	message.ParseMessagePack(byte(socket.MessageType.PlayerDrawingTurn), PlayerTurnDraw{
		UserID:    f.curDrawer.userID.String(),
		Username:  f.curDrawer.Username,
		Time:      f.timeImage,
		DrawingID: drawingID.String(),
		Length:    len(f.currentWord),
	})
	f.pbroadcast(&message)
	f.timeStartImage = time.Now()
	log.Printf("[Match] [FFA] -> Word sent waiting for guesses, Match: %s", f.info.ID)
	f.receivingGuesses.Set()

	//Make him draw
	if f.waitTimeout() {
		log.Printf("[Match] [FFA] -> Time's up. Not all the players could guess the word, Match: %s", f.info.ID)
	} else {
		log.Printf("[Match] [FFA] -> All players could guess the word, Match: %s", f.info.ID)
	}

	//Send message that the current word have expired
	timeUpMessage := socket.RawMessage{}
	timeUpMessage.ParseMessagePack(byte(socket.MessageType.TimeUp), TimeUp{
		Type: 1,
		Word: f.currentWord,
	})
	f.pbroadcast(&timeUpMessage)

	f.receiving.Lock()
	f.orderPos++
	f.curLap++
	if f.orderPos > len(f.players)-1 {
		f.orderPos = 0
	}

	//Is the game finished ?
	if f.curLap > f.lapsTotal {
		f.isRunning = false
		timeUpMessage := socket.RawMessage{}
		timeUpMessage.ParseMessagePack(byte(socket.MessageType.TimeUp), TimeUp{
			Type: 2,
			Word: f.currentWord,
		})
		f.pbroadcast(&timeUpMessage)
		f.receiving.Unlock()
		return
	}

	f.currentWord = ""
	f.resetGuess()
	f.receiving.Unlock()

	time.Sleep(time.Second * 5)
}

//Disconnect endpoint for when a user exits
func (f *FFA) Disconnect(socketID uuid.UUID) {

	f.receiving.Lock()
	leaveMessage := socket.RawMessage{}
	leaveMessage.ParseMessagePack(byte(socket.MessageType.PlayerHasLeftGame), PlayerHasLeft{
		UserID:   f.connections[socketID].userID.String(),
		Username: f.connections[socketID].Username,
	})
	f.pbroadcast(&leaveMessage)

	//Check if drawing
	if f.curDrawer.socketID == socketID {
		f.cancelWait() //We cancel the wait and finish the drawing for the client to see

		//Finish the end of the drawing for the clients
		bytes, _ := uuid.Nil.MarshalBinary()
		endDrawing := socket.RawMessage{
			MessageType: byte(socket.MessageType.EndDrawingServer),
			Length:      uint16(len(bytes)),
			Bytes:       bytes,
		}
		f.pbroadcast(&endDrawing)
	}
	//Check the state of the game if there are enough players to finish the game
	if f.realPlayers-1 <= 0 {
		f.receiving.Unlock()
		f.Close()
		return
	}

	f.removePlayer(f.connections[socketID], socketID)
	f.lapsTotal -= numberOfTurns
	f.receiving.Unlock()

	f.syncPlayers()
}

//TryWord endpoint for when a user tries to guess a word
func (f *FFA) TryWord(socketID uuid.UUID, word string) {
	f.receiving.Lock()
	log.Printf("[Match] [FFA] Guessing the word for the socket id %s", socketID)
	if strings.ToLower(strings.TrimSpace(word)) == f.currentWord && f.currentWord != "" {

		//The word was found
		if f.receivingGuesses.IsSet() && !f.hasFoundit[socketID] {
			f.hasFoundit[socketID] = true
			f.waitingResponse.Release(1)
			f.clientGuess++
			players := f.connections[socketID]

			pointsForWord := f.calculateScore()
			f.scores[players.Order] += pointsForWord
			total := f.scores[players.Order]

			f.receiving.Unlock()

			response := socket.RawMessage{}
			response.ParseMessagePack(byte(socket.MessageType.ResponseGuess), GuessResponse{
				Valid:       true,
				Points:      pointsForWord,
				PointsTotal: total,
			})
			socket.SendRawMessageToSocketID(response, socketID)

			//Broadcast to all the other players that the word was found
			broadcast := socket.RawMessage{}
			broadcast.ParseMessagePack(byte(socket.MessageType.WordFound), WordFound{
				Username:    players.Username,
				UserID:      players.userID.String(),
				Points:      pointsForWord,
				PointsTotal: total,
			})
			f.pbroadcast(&broadcast)
		} else {
			log.Printf("[Match] [FFA] -> Word is alredy guessed or is not ready to receive words for socket %s", socketID)
			players := f.connections[socketID]
			scoreTotal := f.scores[players.Order]
			f.receiving.Unlock()

			response := socket.RawMessage{}
			response.ParseMessagePack(byte(socket.MessageType.ResponseGuess), GuessResponse{
				Valid:       false,
				Points:      0,
				PointsTotal: scoreTotal,
			})
			socket.SendRawMessageToSocketID(response, socketID)
		}
	} else {
		players := f.connections[socketID]
		scoreTotal := f.scores[players.Order]
		f.receiving.Unlock()

		response := socket.RawMessage{}
		response.ParseMessagePack(byte(socket.MessageType.ResponseGuess), GuessResponse{
			Valid:       false,
			Points:      0,
			PointsTotal: scoreTotal,
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
	f.receiving.Lock()
	if !f.players[f.order[f.orderPos]].IsCPU {
		f.receiving.Unlock()

		message := socket.RawMessage{}
		message.ParseMessagePack(byte(socket.MessageType.ResponseHintMatch), HintResponse{
			Hint:  "",
			Error: "Hints are not available for this player. The drawing player needs to be a virtual player.",
		})
		socket.SendRawMessageToSocketID(message, socketID)
		log.Printf("[Match] [FFA] -> Hint requested for a non virutal player. Match: %s", f.info.ID)
	} else {
		f.receiving.Unlock()

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
	defer f.receiving.Unlock()
	f.receiving.Lock()
	log.Printf("[Match] [FFA] Force match shutdown, the game will finish the last lap")
	f.isRunning = false
	f.cancelWait()
}

//GetConnections returns all the socketID of the match
func (f *FFA) GetConnections() []uuid.UUID {
	connections := make([]uuid.UUID, 0, len(f.players))
	for i := range f.players {
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
			Points:   0,
			IsCPU:    false,
		})
	}
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
		log.Printf("[Match] [FFA] -> Cannot access the word library closing the game. Match: %s", f.info.ID)
		f.Close()
	}
	return word
}

//SetOrder used to change the order
func (f *FFA) SetOrder() {
	choices := make([]int, len(f.players))
	f.order = make([]int, 0, len(f.players))
	for i := range f.players {
		choices[i] = i
	}

	for i := len(choices); i > 0; i-- {
		choicePos := f.rand.Intn(i)
		userPos := sliceutils.PopInt(&choices, choicePos)

		f.players[userPos].Order = i
		f.order = append(f.order, userPos)
	}

	for i := range f.order {
		playerPos := f.order[i]
		f.players[playerPos].Order = playerPos
	}

	f.orderPos = 0

}

func (f *FFA) resetGuess() {
	for i := range f.players {
		f.hasFoundit[f.players[i].socketID] = false
	}

	f.clientGuess = 0

}

//syncPlayers message used to keep all the players in sync
func (f *FFA) syncPlayers() {
	f.receiving.Lock()
	players := make([]PlayersData, len(f.scores))
	for i := range f.scores {
		players[i] = PlayersData{
			Username: f.players[f.order[i]].Username,
			UserID:   f.players[f.order[i]].userID.String(),
			Points:   f.scores[i],
			IsCPU:    f.players[f.order[i]].IsCPU,
		}
	}
	f.receiving.Unlock()

	message := socket.RawMessage{}
	imageDuration := time.Now().Sub(f.timeStartImage)
	message.ParseMessagePack(byte(socket.MessageType.PlayerSync), PlayerSync{
		Players:  players,
		Laps:     f.curLap,
		LapTotal: f.lapsTotal,
		Time:     f.timeImage - imageDuration.Milliseconds(),
		GameTime: 0,
	})
	f.pbroadcast(&message)
}

func (f *FFA) waitTimeout() bool {
	c := make(chan struct{})
	defer f.receiving.Unlock()

	go func() {
		for {
			select {
			case <-time.After(time.Second):
				//Send an update to the clients
				f.receiving.Lock()
				f.syncPlayers()
				f.receiving.Unlock()
			case <-c:
				return
			}
		}
	}()

	cnt := context.Background()
	cnt, f.cancelWait = context.WithTimeout(cnt, time.Millisecond*time.Duration(f.timeImage))
	err := f.waitingResponse.Acquire(cnt, f.nbWaitingResponses)
	f.cancelWait()

	close(c)

	if err == nil {
		f.receiving.Lock()
		f.receivingGuesses.UnSet()
		return false // completed normally
	}

	f.receiving.Lock()
	f.receivingGuesses.UnSet()
	return true // timed out
}

//calculateScore based on the number of seconds of remaining and the time associated with the score
func (f *FFA) calculateScore() int {
	const baseScore = 1000
	const minimum = 100
	imageDuration := time.Now().Sub(f.timeStartImage)
	remaining := int((f.timeImage - imageDuration.Milliseconds()) / 10000)
	score := (baseScore / f.clientGuess) - (remaining * 10)
	if score < minimum {
		return minimum
	}
	return score
}

//finish when the match terminates announce winner
func (f *FFA) finish() {
	gameDuration := time.Now().Sub(f.timeStart)
	log.Printf("[Match] [FFA] -> Game has ended. Match: %s", f.info.ID)
	//Identify the winner
	bestPlayerOrder := -1
	bestScore := -1
	players := make([]PlayersData, len(f.scores))

	for i := range f.scores {
		if bestScore < f.scores[i] {
			bestPlayerOrder = i
			bestScore = f.scores[i]
		}
		players[i] = PlayersData{
			Username: f.players[f.order[i]].Username,
			UserID:   f.players[f.order[i]].userID.String(),
			Points:   f.scores[i],
			IsCPU:    f.players[f.order[i]].IsCPU,
		}
	}
	winner := f.players[f.order[bestPlayerOrder]]
	log.Printf("[Match] [FFA] -> Winner is %s Match: %s", winner.Username, f.info.ID)

	//Send a message to all the players to give them the details of the game and who is the winner
	message := socket.RawMessage{}
	message.ParseMessagePack(byte(socket.MessageType.GameEnded), GameEnded{
		Players:    players,
		Winner:     winner.userID.String(),
		WinnerName: winner.Username,
		Time:       gameDuration.Milliseconds(),
	})

	f.broadcast(&message)
	drawing.UnRegisterGame(f)
}

//removePlayer remove the player and set the order
func (f *FFA) removePlayer(p *players, socketID uuid.UUID) {
	//Remove the indexing for the players
	delete(f.connections, socketID)
	for i := range f.players {
		if p.userID == f.players[i].userID {
			currentPos := f.orderPos
			currentUser := f.players[f.order[currentPos]].userID

			scoreMap := make(map[uuid.UUID]int)
			for j := range f.players {
				scoreMap[f.players[j].userID] = f.scores[f.players[j].Order]
			}
			//Remove the player
			f.players[i] = f.players[len(f.players)-1] // Copy last element to index i.
			f.players[len(f.players)-1] = players{}    // Erase last element (write zero value).
			f.players = f.players[:len(f.players)-1]   // Truncate slice.

			//Remove the number from the player
			f.order[i] = f.order[len(f.order)-1]
			f.order[len(f.order)-1] = -1
			f.order = f.order[:len(f.order)-1]

			//Shrunk the score array
			f.scores = f.scores[:len(f.scores)-1]

			//Recompute the order and the order
			sort.Slice(f.players, func(i, j int) bool {
				return (f.players)[i].Order < (f.players)[j].Order
			})
			//We can recompute the order
			for j := 0; j < len(f.players); j++ {
				f.order[j] = j
				f.players[j].Order = j
				f.connections[f.players[j].socketID] = &f.players[j]
				f.scores[j] = scoreMap[f.players[j].userID]
			}

			//Check if the order has changed
			maxPos := len(f.order) - 1
			if maxPos <= 0 {
				f.orderPos = 0
				return
			}

			same := currentPos % maxPos

			if f.players[same].userID == currentUser {
				f.orderPos = same
				return
			}

			//Try increment or decrement
			increment := (currentPos + 1) % maxPos
			if f.players[increment].userID == currentUser {
				f.orderPos = increment
				return
			}

			decrement := (currentPos - 1) % maxPos
			if f.players[decrement].userID == currentUser {
				f.orderPos = decrement
				return
			}
		}
	}
}
