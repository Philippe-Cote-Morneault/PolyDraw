package mode

import (
	"github.com/google/uuid"
	"github.com/tevino/abool"
	"gitlab.com/jigsawcorp/log3900/internal/services/drawing"
	"gitlab.com/jigsawcorp/log3900/internal/socket"
	"gitlab.com/jigsawcorp/log3900/model"
	"gitlab.com/jigsawcorp/log3900/pkg/sliceutils"
	"log"
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
	orderPos       int
	curLap         int
	lapsTotal      int
	realPlayers    int
	rand           *rand.Rand
	timeImage      int64
	isRunning      bool
	currentWord    string
	timeStart      time.Time
	timeStartImage time.Time

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
	f.scores = make([]int, len(f.players))

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
	f.waitingResponse = sync.WaitGroup{} //Reset the waitgroup

	curDrawer := f.players[f.order[f.orderPos]]
	drawingID := uuid.New()
	if curDrawer.IsCPU {
		f.waitingResponse.Add(f.realPlayers)
	} else {
		f.waitingResponse.Add(f.realPlayers - 1)
		f.hasFoundit[curDrawer.socketID] = true
	}

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
			f.pbroadcast(&timeUpMessage)
			f.receiving.Unlock()
			return
		}
	}

	f.currentWord = ""
	f.resetGuess()
	f.receiving.Unlock()
}

//Disconnect endpoint for when a user exits
func (f *FFA) Disconnect(socketID uuid.UUID) {
}

//TryWord endpoint for when a user tries to guess a word
func (f *FFA) TryWord(socketID uuid.UUID, word string) {
	f.receiving.Lock()
	log.Printf("[Match] [FFA] Guessing the word for the socket id %s", socketID)
	if strings.ToLower(strings.TrimSpace(word)) == f.currentWord && f.currentWord != "" {

		//The word was found
		if f.receivingGuesses.IsSet() && !f.hasFoundit[socketID] {
			f.hasFoundit[socketID] = true
			f.waitingResponse.Done()

			players := f.connections[socketID]
			pointsForWord := 100 //TODO change the point system based with time
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
			log.Printf("[Match] [FFA] -> Word is alredy guessed for socket %s", socketID)
			f.receiving.Unlock()
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

}

//syncPlayers message used to keep all the players in sync
func (f *FFA) syncPlayers() {
	players := make([]PlayersData, len(f.scores))
	for i := range f.scores {
		players[i] = PlayersData{
			Username: f.players[f.order[i]].Username,
			UserID:   f.players[f.order[i]].userID.String(),
			Points:   f.scores[i],
			IsCPU:    f.players[f.order[i]].IsCPU,
		}
	}

	message := socket.RawMessage{}
	imageDuration := time.Now().Sub(f.timeStartImage)
	message.ParseMessagePack(byte(socket.MessageType.PlayerSync), PlayerSync{
		Players:  players,
		Laps:     f.curLap,
		Time:     f.timeImage - imageDuration.Milliseconds(),
		GameTime: 0,
	})
	f.pbroadcast(&message)
}

func (f *FFA) waitTimeout() bool {
	defer f.receiving.Unlock() //Mutex prevents from having a negative semaphore upon cleanup

	c := make(chan struct{})
	go func() {
		defer close(c)
		f.waitingResponse.Wait()
	}()
	imageTimeout := time.After(time.Millisecond * time.Duration(f.timeImage))
	for {
		select {
		//Send the check up message every 1 second
		case <-time.After(time.Second):
			//Send an update to the client
			f.receiving.Lock()
			f.syncPlayers()
			f.receiving.Unlock()
		case <-c:
			f.receiving.Lock()
			f.receivingGuesses.UnSet()
			return false // completed normally
		case <-imageTimeout:
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
