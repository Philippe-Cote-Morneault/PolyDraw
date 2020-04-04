package mode

import (
	"log"
	"math"
	"math/rand"
	"sort"
	"strings"
	"sync"
	"time"
	"unicode/utf8"

	"gitlab.com/jigsawcorp/log3900/internal/services/stats/broadcast"
	"gitlab.com/jigsawcorp/log3900/internal/services/virtualplayer"

	"gitlab.com/jigsawcorp/log3900/internal/language"
	match2 "gitlab.com/jigsawcorp/log3900/internal/match"
	"gitlab.com/jigsawcorp/log3900/internal/services/messenger"

	"gitlab.com/jigsawcorp/log3900/pkg/cbroadcast"

	"github.com/google/uuid"
	"gitlab.com/jigsawcorp/log3900/internal/services/drawing"
	"gitlab.com/jigsawcorp/log3900/internal/socket"
	"gitlab.com/jigsawcorp/log3900/model"
	"gitlab.com/jigsawcorp/log3900/pkg/sliceutils"
	"golang.org/x/sync/semaphore"
)

//FFA Free for all game mode
type FFA struct {
	base
	order  []int
	scores []score //Scores data are in the order of the match so the first user to draw is the first one in the score board
	//We can get the position with the field order
	orderPos       int
	curLap         int
	curDrawer      *players
	lapsTotal      int
	realPlayers    int
	rand           *rand.Rand
	currentWord    string
	timeStart      time.Time
	timeStartImage time.Time

	receiving   sync.Mutex
	hasFoundIt  map[uuid.UUID]bool
	clientGuess int
}

//Init initialize the game mode
func (f *FFA) Init(connections []uuid.UUID, info model.Group) {
	f.init(connections, info)
	f.rand = rand.New(rand.NewSource(time.Now().UnixNano()))
	f.isRunning = true
	f.hasFoundIt = make(map[uuid.UUID]bool, len(connections))
	f.funcSyncPlayer = f.syncPlayers

	f.scores = make([]score, len(f.players))

	f.curLap = 1
	f.lapsTotal = len(f.players) * f.info.NbRound

	f.realPlayers = 0
	for i := range f.players {
		if !f.players[i].IsCPU {
			f.realPlayers++
		}
	}

	switch f.info.Difficulty {
	case 0:
		f.timeImage = 60
	case 1:
		f.timeImage = 45
	case 2:
		f.timeImage = 30
	}
	f.timeImage *= 1000

	drawing.RegisterGame(f)
	cbroadcast.Broadcast(match2.BGameStarts, f)
}

//Start the game mode
func (f *FFA) Start() {

	started := f.waitForPlayers()
	if !started {
		log.Printf("[Match] [FFA] -> Start aborted all client could not call ready. Match: %s", f.info.ID)

		drawing.UnRegisterGame(f)
		messenger.UnRegisterGroup(&f.info, f.GetConnections())
		return
	}

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
	f.receiving.Lock()
	if f.realPlayers <= 0 || len(f.players) <= 0 {
		log.Printf("[Match] [FFA] No players will exit the game loop.")
		f.isRunning = false
		f.receiving.Unlock()
		return
	}
	//Choose a user.
	f.curDrawer = &f.players[f.order[f.orderPos]]
	drawingID := uuid.New()

	var game *model.Game
	if f.curDrawer.IsCPU {
		f.nbWaitingResponses = int64(f.realPlayers)

		game = f.findGame()
		if game.ID == uuid.Nil {
			f.receiving.Unlock()
			log.Printf("[Match] [FFA] Panic, not able to find a game for the virtual players")
			return
		}
		f.currentWord = game.Word
	} else {
		f.currentWord = f.findWord()
		f.nbWaitingResponses = int64(f.realPlayers - 1)
		f.hasFoundIt[f.curDrawer.socketID] = true
	}
	cbroadcast.Broadcast(match2.BRoundStarts, match2.RoundStart{
		MatchID: f.info.ID,
		Drawer: match2.Player{
			IsCPU:    f.curDrawer.IsCPU,
			Username: f.curDrawer.Username,
			ID:       f.curDrawer.userID,
		},
		Game: game,
	})

	f.waitingResponse = semaphore.NewWeighted(f.nbWaitingResponses)
	f.waitingResponse.TryAcquire(f.nbWaitingResponses)

	message := socket.RawMessage{}
	message.ParseMessagePack(byte(socket.MessageType.PlayerDrawThis), PlayerDrawThis{
		Word:      f.currentWord,
		Time:      f.timeImage,
		DrawingID: drawingID.String(),
	})
	socket.SendQueueMessageSocketID(message, f.curDrawer.socketID)

	message.ParseMessagePack(byte(socket.MessageType.PlayerDrawingTurn), PlayerTurnDraw{
		UserID:    f.curDrawer.userID.String(),
		Username:  f.curDrawer.Username,
		Time:      f.timeImage,
		DrawingID: drawingID.String(),
		Length:    utf8.RuneCountInString(f.currentWord),
	})
	f.broadcast(&message)
	f.timeStartImage = time.Now()
	log.Printf("[Match] [FFA] -> Word sent waiting for guesses, Match: %s", f.info.ID)
	f.receiving.Unlock()

	f.receivingGuesses.Set()

	//Make him draw
	if f.waitTimeout() {
		log.Printf("[Match] [FFA] -> Time's up. Not all the players could guess the word, Match: %s", f.info.ID)
	} else {
		log.Printf("[Match] [FFA] -> All players could guess the word, Match: %s", f.info.ID)
	}

	//Send message that the current word have expired unless it's the end of the round
	f.receiving.Lock()
	if f.curLap < f.lapsTotal {
		timeUpMessage := socket.RawMessage{}
		timeUpMessage.ParseMessagePack(byte(socket.MessageType.TimeUp), TimeUp{
			Type: 1,
			Word: f.currentWord,
		})
		f.broadcast(&timeUpMessage)
	}

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
		f.broadcast(&timeUpMessage)
		f.receiving.Unlock()
		return
	}

	f.sendRoundSummary()

	cbroadcast.Broadcast(match2.BRoundEnds, f.info.ID)

	f.currentWord = ""
	f.resetGuess()
	f.receiving.Unlock()

	time.Sleep(time.Second * 7)
}

//Disconnect endpoint for when a user exits
func (f *FFA) Disconnect(socketID uuid.UUID) {
	f.receiving.Lock()
	messenger.HandleQuitGroup(&f.info, socketID)

	leaveMessage := socket.RawMessage{}
	leaveMessage.ParseMessagePack(byte(socket.MessageType.PlayerHasLeftGame), PlayerHasLeft{
		UserID:   f.connections[socketID].userID.String(),
		Username: f.connections[socketID].Username,
	})
	f.broadcast(&leaveMessage)

	//Check if drawing
	if f.curDrawer != nil && f.curDrawer.socketID == socketID {
		if f.cancelWait != nil {
			f.cancelWait() //We cancel the wait and finish the drawing for the client to see
		}
		//Finish the end of the drawing for the clients
		bytes, _ := uuid.Nil.MarshalBinary()
		endDrawing := socket.RawMessage{
			MessageType: byte(socket.MessageType.EndDrawingServer),
			Length:      uint16(len(bytes)),
			Bytes:       bytes,
		}
		f.broadcast(&endDrawing)
	}
	//Check the state of the game if there are enough players to finish the game
	if (f.realPlayers - 1) < 2 {
		f.removePlayer(f.connections[socketID], socketID)
		f.receiving.Unlock()
		f.Close()
		return
	}

	f.removePlayer(f.connections[socketID], socketID)
	f.realPlayers--
	f.lapsTotal -= f.info.NbRound

	//If the game is waiting for the player to receive an answer
	if f.receivingGuesses.IsSet() {
		f.waitingResponse.Release(1)
	}
	f.receiving.Unlock()

	f.syncPlayers()
}

//TryWord endpoint for when a user tries to guess a word
func (f *FFA) TryWord(socketID uuid.UUID, word string) {
	f.receiving.Lock()
	log.Printf("[Match] [FFA] Guessing the word for the socket id %s", socketID)
	if strings.ToLower(strings.TrimSpace(word)) == f.currentWord && f.currentWord != "" {

		//The word was found
		if f.receivingGuesses.IsSet() && !f.hasFoundIt[socketID] {
			f.hasFoundIt[socketID] = true
			f.waitingResponse.Release(1)
			f.clientGuess++
			player := f.connections[socketID]

			pointsForWord := f.calculateScore()
			f.scores[player.Order].commit(pointsForWord)
			f.scores[f.curDrawer.Order].commit(50)
			total := f.scores[player.Order].total

			response := socket.RawMessage{}
			response.ParseMessagePack(byte(socket.MessageType.ResponseGuess), GuessResponse{
				Valid:     true,
				Points:    total,
				NewPoints: pointsForWord,
			})
			socket.SendQueueMessageSocketID(response, socketID)

			//Broadcast to all the other players that the word was found
			broadcast := socket.RawMessage{}
			broadcast.ParseMessagePack(byte(socket.MessageType.WordFound), WordFound{
				Username:  player.Username,
				UserID:    player.userID.String(),
				Points:    total,
				NewPoints: pointsForWord,
			})
			f.broadcast(&broadcast)
			f.receiving.Unlock()

		} else {
			log.Printf("[Match] [FFA] -> Word is alredy guessed or is not ready to receive words for socket %s", socketID)
			players := f.connections[socketID]
			scoreTotal := f.scores[players.Order].total
			f.receiving.Unlock()

			response := socket.RawMessage{}
			response.ParseMessagePack(byte(socket.MessageType.ResponseGuess), GuessResponse{
				Valid:     false,
				NewPoints: 0,
				Points:    scoreTotal,
			})
			socket.SendQueueMessageSocketID(response, socketID)
		}
	} else {
		players := f.connections[socketID]
		scoreTotal := f.scores[players.Order].total
		f.receiving.Unlock()

		response := socket.RawMessage{}
		response.ParseMessagePack(byte(socket.MessageType.ResponseGuess), GuessResponse{
			Valid:     false,
			NewPoints: 0,
			Points:    scoreTotal,
		})
		socket.SendQueueMessageSocketID(response, socketID)
	}
}

//HintRequested method used by the user when requesting a hint
func (f *FFA) HintRequested(socketID uuid.UUID) {
	f.receiving.Lock()
	if len(f.players) > 0 && !f.players[f.order[f.orderPos]].IsCPU {

		message := socket.RawMessage{}
		message.ParseMessagePack(byte(socket.MessageType.ResponseHintMatch), HintResponse{
			Hint:  "",
			Error: language.MustGet("error.hintInvalid", f.info.Language),
		})
		f.receiving.Unlock()

		socket.SendQueueMessageSocketID(message, socketID)
		log.Printf("[Match] [FFA] -> Hint requested for a non virutal player. Match: %s", f.info.ID)
	} else {
		player := f.connections[socketID]
		if f.scores[player.Order].total-50 > 0 {
			f.receiving.Unlock()
			hintSent := virtualplayer.GetHintByBot(&match2.HintRequested{
				GameType: f.info.GameType,
				MatchID:  f.info.ID,
				SocketID: socketID,
				Player: match2.Player{
					IsCPU:    player.IsCPU,
					Username: player.Username,
					ID:       player.userID,
				},
				DrawerID: f.curDrawer.userID,
			})

			if hintSent {
				f.receiving.Lock()
				f.scores[player.Order].commit(-50)
				f.receiving.Unlock()

				f.syncPlayers()
			}
		} else {
			message := socket.RawMessage{}
			message.ParseMessagePack(byte(socket.MessageType.ResponseHintMatch), HintResponse{
				Hint:   "",
				Error:  language.MustGet("error.hintScore", f.info.Language),
				UserID: player.userID.String(),
				BotID:  f.curDrawer.userID.String(),
			})
			f.receiving.Unlock()
			socket.SendQueueMessageSocketID(message, socketID)
			log.Printf("[Match] [FFA] -> Hint requested but not enough points. Match: %s", f.info.ID)
		}

	}
}

//Close forces the game to stop completely. Graceful shutdown
func (f *FFA) Close() {
	f.receiving.Lock()
	log.Printf("[Match] [FFA] Force match shutdown, the game will finish the last lap")
	f.isRunning = false
	if f.cancelWait != nil {
		f.cancelWait()
	}

	cancelMessage := socket.RawMessage{}
	cancelMessage.ParseMessagePack(byte(socket.MessageType.GameCancel), GameCancel{
		Type: 2,
	})
	f.broadcast(&cancelMessage)

	f.receiving.Unlock()
	drawing.UnRegisterGame(f)
	messenger.UnRegisterGroup(&f.info, f.GetConnections())
	cbroadcast.Broadcast(match2.BGameEnds, f.info.ID)
}

//GetConnections returns all the socketID of the match
func (f *FFA) GetConnections() []uuid.UUID {
	f.receiving.Lock()
	connections := make([]uuid.UUID, 0, len(f.connections))
	for i := range f.connections {
		connections = append(connections, f.connections[i].socketID)
	}
	f.receiving.Unlock()
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
			IsCPU:    f.info.Users[i].IsCPU,
		})
	}
	welcome := ResponseGameInfo{
		Players:   players,
		GameType:  0,
		TimeImage: f.timeImage,
		Laps:      f.lapsTotal,
		TotalTime: 0,
		Lives:     0,
	}
	message := socket.RawMessage{}
	message.ParseMessagePack(byte(socket.MessageType.GameWelcome), welcome)
	return message

}

//GetPlayers returns the number of players
func (f *FFA) GetPlayers() []match2.Player {
	defer f.receiving.Unlock()
	f.receiving.Lock()
	return f.getPlayers()
}

//findWord used to the find the word that must be drawn
func (f *FFA) findWord() string {
	key := ""
	switch f.info.Language {
	case language.EN:
		key = "dict_words_en"
	case language.FR:
		key = "dict_words_fr"
	}

	word := ""
	watchDog := 0
	for word == "" {

		var err error
		word, err = model.Redis().SRandMember(key).Result()
		if err != nil {
			log.Printf("[Match] [FFA] -> Cannot access the word library closing the game. Match: %s", f.info.ID)
			f.Close()
			return ""
		}

		if _, inList := f.wordHistory[word]; inList && watchDog < 100 {
			word = ""
		} else {
			//Add the word to the list so it does not come up again.
			f.wordHistory[word] = true
		}
		watchDog++
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
		f.scores[playerPos].init() //Init the score to make sure they are at zero
	}

	f.orderPos = 0

}

func (f *FFA) resetGuess() {
	for i := range f.players {
		if !f.players[i].IsCPU {
			f.hasFoundIt[f.players[i].socketID] = false
		}
		f.scores[f.players[i].Order].reset()
	}

	f.clientGuess = 0

}

//syncPlayers message used to keep all the players in sync
func (f *FFA) syncPlayers() {
	f.receiving.Lock()
	players := make([]PlayersData, len(f.scores))
	for i := range f.players {
		player := &f.players[i]
		players[i] = PlayersData{
			Username: player.Username,
			UserID:   player.userID.String(),
			Points:   f.scores[player.Order].total,
			IsCPU:    player.IsCPU,
		}
	}

	message := socket.RawMessage{}
	imageDuration := time.Now().Sub(f.timeStartImage)
	message.ParseMessagePack(byte(socket.MessageType.PlayerSync), PlayerSync{
		Players:  players,
		Laps:     f.curLap,
		LapTotal: f.lapsTotal,
		Time:     f.timeImage - imageDuration.Milliseconds(),
	})
	f.broadcast(&message)
	f.receiving.Unlock()
}

//calculateScore based on the number of seconds of remaining and the time associated with the score
func (f *FFA) calculateScore() int {
	const baseScore = 1000
	const minimum = 100
	imageDuration := time.Now().Sub(f.timeStartImage)
	score := int(baseScore - minimum*math.Sqrt(imageDuration.Seconds()))
	if score < minimum {
		return minimum
	}
	return score
}

//finish when the match terminates announce winner
func (f *FFA) finish() {
	f.receiving.Lock()
	gameDuration := time.Now().Sub(f.timeStart)
	log.Printf("[Match] [FFA] -> Game has ended. Match: %s", f.info.ID)
	players := make([]PlayersData, len(f.scores))
	if len(f.players) <= 0 {
		f.receiving.Unlock()

		messenger.UnRegisterGroup(&f.info, f.GetConnections()) //Remove the chat messenger
		drawing.UnRegisterGame(f)
		log.Printf("[Match] [FFA] No more players in the match. Will not send finish packet")
		return
	}

	for i := range f.players {
		player := &f.players[i]
		players[i] = PlayersData{
			UserID:   player.userID.String(),
			Username: player.Username,
			IsCPU:    player.IsCPU,
			Points:   f.scores[player.Order].total,
		}
	}

	sort.Slice(players, func(i, j int) bool {
		return (players)[i].Points > (players)[j].Points
	})

	winner := players[0]
	log.Printf("[Match] [FFA] -> Winner is %s Match: %s", winner.Username, f.info.ID)

	//Send a message to all the players to give them the details of the game and who is the winner
	message := socket.RawMessage{}
	message.ParseMessagePack(byte(socket.MessageType.GameEnded), GameEnded{
		Players:    players,
		Winner:     winner.UserID,
		WinnerName: winner.Username,
		Time:       gameDuration.Milliseconds(),
	})

	f.broadcast(&message)
	f.receiving.Unlock()

	drawing.UnRegisterGame(f)
	messenger.UnRegisterGroup(&f.info, f.GetConnections()) //Remove the chat messenger
	cbroadcast.Broadcast(match2.BGameEnds, f.info.ID)
	cbroadcast.Broadcast(broadcast.BUpdateMatch, match2.StatsData{SocketsID: f.GetConnections(), Match: &model.MatchPlayed{
		MatchDuration: gameDuration.Milliseconds(),
		WinnerName:    winner.Username,
		MatchType:     0}})
}

//removePlayer remove the player and set the order
func (f *FFA) removePlayer(p *players, socketID uuid.UUID) {
	//Remove the indexing for the players
	delete(f.connections, socketID)
	delete(f.hasFoundIt, socketID)
	for i := range f.players {
		if p.userID == f.players[i].userID {
			currentPos := f.orderPos

			if len(f.order) <= 0 {
				log.Printf("[Match] [FFA] -> Remove player there are no more players in the game. (f:order %v, f:players: %v)")
				return
			}
			currentUser := f.players[f.order[currentPos]].userID
			isCurrentUser := p.userID == currentUser

			scoreMap := make(map[uuid.UUID]score)
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
			maxPos := len(f.order)
			if maxPos <= 0 {
				f.orderPos = 0
				return
			}

			if !isCurrentUser {
				//Find the current drawer in the list
				same := currentPos % maxPos
				if f.players[same].userID == currentUser {
					f.orderPos = same
					return
				}

				//Look in all the players to find its current position.
				for j := range f.players {
					if f.players[j].userID == currentUser {
						f.orderPos = j
						break
					}
				}
				return
			}

			f.orderPos = (currentPos + 1) % maxPos
			return
		}
	}
}

//sendRoundSummary used to send a summary of the round
func (f *FFA) sendRoundSummary() {
	roundEnd := socket.RawMessage{}
	playersDetails := make([]PlayersRoundSum, len(f.players))
	for i := range f.players {
		player := &f.players[i]
		playersDetails[i] = PlayersRoundSum{
			PlayersData: PlayersData{
				UserID:   player.userID.String(),
				Username: player.Username,
				IsCPU:    player.IsCPU,
				Points:   f.scores[player.Order].total,
			},
			NewPoints: f.scores[player.Order].current,
		}
	}
	roundEnd.ParseMessagePack(byte(socket.MessageType.RoundEndStatus), RoundSummary{
		Players:      playersDetails,
		Achievements: nil,
		Word:         f.currentWord,
		Guessed:      false,
	})
	f.broadcast(&roundEnd)
}

//GetGroupID return the group id
func (f *FFA) GetGroupID() uuid.UUID {
	defer f.receiving.Unlock()
	f.receiving.Lock()
	return f.info.ID
}
