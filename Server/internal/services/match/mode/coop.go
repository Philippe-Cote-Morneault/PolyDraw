package mode

import (
	"context"
	"log"
	"strings"
	"sync"
	"time"
	"unicode/utf8"

	"gitlab.com/jigsawcorp/log3900/internal/services/drawing"
	"gitlab.com/jigsawcorp/log3900/internal/services/stats/broadcast"

	"gitlab.com/jigsawcorp/log3900/internal/language"

	"gitlab.com/jigsawcorp/log3900/internal/services/messenger"
	"gitlab.com/jigsawcorp/log3900/internal/services/virtualplayer"

	"github.com/google/uuid"
	"github.com/tevino/abool"
	match2 "gitlab.com/jigsawcorp/log3900/internal/match"
	"gitlab.com/jigsawcorp/log3900/internal/socket"
	"gitlab.com/jigsawcorp/log3900/model"
	"gitlab.com/jigsawcorp/log3900/pkg/cbroadcast"
	"golang.org/x/sync/semaphore"
)

//Coop represent a cooperative game mode
type Coop struct {
	base
	orderVirtual []*players
	curDrawer    *players
	orderPos     int
	chances      int
	currentWord  string
	realPlayers  int
	commonScore  score

	gameTime              int64
	checkPointTime        int64
	minimumBonus          int
	minimumScore          int
	checkpointScore       int
	maximumCheckPointTime int64
	lives                 int
	wordFound             bool

	receiving        sync.Mutex
	timeStart        time.Time
	timeStartImage   time.Time
	nbVirtualPlayers int
	curLap           int

	closingTimeKeeper chan struct{}
	lastLoop          chan struct{}
	penalty           int64
}

//Init creates the coop game mode
func (c *Coop) Init(connections []uuid.UUID, info model.Group) {
	c.init(connections, info)

	c.isRunning = true
	c.orderPos = 0
	c.nbWaitingResponses = 1
	c.curLap = 1
	c.wordFound = false
	c.checkPointTime = 0
	c.commonScore.init()
	c.orderVirtual = make([]*players, info.VirtualPlayers)
	c.closingTimeKeeper = make(chan struct{})

	c.receivingGuesses = abool.New()
	c.funcSyncPlayer = c.syncPlayers

	c.computeDifficulty()
	c.computeOrder()

	cbroadcast.Broadcast(match2.BGameStarts, c)
}

//Start the game and the game loop
func (c *Coop) Start() {
	started := c.waitForPlayers()
	if !started {
		log.Printf("[Match] [Coop] -> Start aborted all client could not call ready. Match: %s", c.info.ID)
		messenger.UnRegisterGroup(&c.info, c.GetConnections())
		return
	}
	//We can start the game loop
	log.Printf("[Match] [Coop] -> Starting gameloop Match: %s", c.info.ID)

	timeOut := make(chan bool)
	c.timeStart = time.Now()
	go func() {
		defer close(timeOut)
		//Check if the time has expired
		for {
			select {
			case <-time.After(time.Millisecond * 100):
				c.receiving.Lock()
				gameDuration := time.Now().Sub(c.timeStart).Milliseconds()
				expired := gameDuration > c.gameTime+c.checkPointTime
				c.receiving.Unlock()
				if expired {
					c.finish()
					return
				}
			case <-c.closingTimeKeeper:
				return
			}
		}
	}()

	for c.isRunning {
		c.GameLoop()
	}
	<-timeOut
}

//GameLoop is called every new round
func (c *Coop) GameLoop() {
	c.receiving.Lock()
	c.lastLoop = make(chan struct{})
	defer close(c.lastLoop)
	c.curDrawer = c.orderVirtual[c.orderPos]
	drawingID := uuid.New()

	game := c.findGame()
	c.lives = c.chances

	if game.ID == uuid.Nil {
		c.receiving.Unlock()
		log.Printf("[Match] [Coop] Panic, not able to find a game for the virtual players")
		return
	}
	c.currentWord = game.Word
	cbroadcast.Broadcast(match2.BRoundStarts, match2.RoundStart{
		MatchID: c.info.ID,
		Drawer: match2.Player{
			IsCPU:    c.curDrawer.IsCPU,
			Username: c.curDrawer.Username,
			ID:       c.curDrawer.userID,
		},
		Game: game,
	})

	c.waitingResponse = semaphore.NewWeighted(c.nbWaitingResponses)
	c.waitingResponse.TryAcquire(c.nbWaitingResponses)

	message := socket.RawMessage{}
	message.ParseMessagePack(byte(socket.MessageType.PlayerDrawingTurn), PlayerTurnDraw{
		UserID:    c.curDrawer.userID.String(),
		Username:  c.curDrawer.Username,
		Time:      c.timeImage,
		DrawingID: drawingID.String(),
		Length:    utf8.RuneCountInString(c.currentWord),
	})

	c.broadcast(&message)
	c.timeStartImage = time.Now()
	log.Printf("[Match] [Coop] -> Word sent waiting for guesses, Match: %s", c.info.ID)
	c.receiving.Unlock()

	c.receivingGuesses.Set()

	if c.waitGuess() {
		log.Printf("[Match] [Coop] -> Word aborted could not be found., Match: %s", c.info.ID)
	} else {
		log.Printf("[Match] [Coop] -> The word was found, Match: %s", c.info.ID)
	}
	c.receivingGuesses.UnSet()

	c.receiving.Lock()
	if c.isRunning { //Do not send the message at the end of the game.
		timeUpMessage := socket.RawMessage{}
		timeUpMessage.ParseMessagePack(byte(socket.MessageType.TimeUp), TimeUp{
			Type: 1,
			Word: c.currentWord,
		})
		c.broadcast(&timeUpMessage)
	}

	//End of round
	if c.realPlayers <= 0 {
		c.isRunning = false
		c.receiving.Unlock()

		c.Close()
		return
	}

	//Prepare next round
	c.sendRoundSummary()

	c.orderPos++
	c.curLap++
	c.orderPos = c.orderPos % c.nbVirtualPlayers
	c.commonScore.reset()
	c.currentWord = ""
	c.wordFound = false

	c.checkPointTime += 5000 //Time for the sleep
	c.receiving.Unlock()

	cbroadcast.Broadcast(match2.BRoundEnds, c.info.ID)

	time.Sleep(time.Second * 5)
	return
}

//Ready client register to make sure they are ready to start the game
func (c *Coop) Ready(socketID uuid.UUID) {
	defer c.receiving.Unlock()
	c.receiving.Lock()

	c.ready(socketID)
}

//Disconnect handle disconnect for the coop
func (c *Coop) Disconnect(socketID uuid.UUID) {
	//Remove the player
	c.receiving.Lock()
	messenger.HandleQuitGroup(&c.info, socketID)
	drawing.UnRegisterSession(socketID)

	player := c.connections[socketID]

	leaveMessage := socket.RawMessage{}
	leaveMessage.ParseMessagePack(byte(socket.MessageType.PlayerHasLeftGame), PlayerHasLeft{
		UserID:   player.userID.String(),
		Username: player.Username,
	})
	c.broadcast(&leaveMessage)

	//Remove the player
	for i := range c.players {
		if c.players[i].userID == player.userID {
			c.players[i] = c.players[len(c.players)-1] // Copy last element to index i.
			c.players[len(c.players)-1] = players{}    // Erase last element (write zero value).
			c.players = c.players[:len(c.players)-1]   // Truncate slice.

			c.realPlayers--
			realPlayer := c.realPlayers
			delete(c.connections, socketID)
			c.receiving.Unlock()

			if realPlayer <= 0 {
				//No more players close the game
				c.Close()
			}
			return
		}
	}
	c.receiving.Unlock()

	c.syncPlayers()
}

//TryWord handle when a client wants to try a word
func (c *Coop) TryWord(socketID uuid.UUID, word string) {
	c.receiving.Lock()
	player := c.connections[socketID]

	log.Printf("[Match] [Coop] Guessing the word for the socket id %s", socketID)
	if strings.ToLower(strings.TrimSpace(word)) == c.currentWord && c.currentWord != "" && c.lives > 0 {
		//The word was found
		if c.receivingGuesses.IsSet() {
			imageDuration := time.Now().Sub(c.timeStartImage)
			bonus := c.timeImage - imageDuration.Milliseconds()
			c.wordFound = true

			pointsForWord := 0
			if bonus > 0 {
				pointsForWord = c.checkpointScore
			} else {
				pointsForWord = c.minimumScore
				bonus = int64(c.minimumBonus)
			}
			c.checkPointTime += bonus

			gameDuration := time.Now().Sub(c.timeStart)
			remaining := c.gameTime - gameDuration.Milliseconds() + c.checkPointTime
			word := c.currentWord

			c.commonScore.commit(pointsForWord)
			total := c.commonScore.total
			c.waitingResponse.Release(1)

			response := socket.RawMessage{}
			response.ParseMessagePack(byte(socket.MessageType.ResponseGuess), GuessResponse{
				Valid:     true,
				Points:    total,
				NewPoints: pointsForWord,
			})
			socket.SendQueueMessageSocketID(response, socketID)

			//Broadcast to all the other players that the word was found
			broadcast := socket.RawMessage{}
			broadcast.ParseMessagePack(byte(socket.MessageType.WordFoundCoop), WordFoundCoop{
				WordFound: WordFound{
					Username:  player.Username,
					UserID:    player.userID.String(),
					Points:    total,
					NewPoints: pointsForWord,
				},
				Word: word,
			})
			c.broadcast(&broadcast)

			checkpoint := socket.RawMessage{}
			checkpoint.ParseMessagePack(byte(socket.MessageType.Checkpoint), Checkpoint{
				TotalTime: remaining,
				Bonus:     bonus,
			})
			c.broadcast(&checkpoint)
			c.receiving.Unlock()
		} else {
			log.Printf("[Match] [Coop] -> Word is alredy guessed or is not ready to receive words for socket %s", socketID)
			scoreTotal := c.commonScore.total
			c.receiving.Unlock()

			response := socket.RawMessage{}
			response.ParseMessagePack(byte(socket.MessageType.ResponseGuess), GuessResponse{
				Valid:     false,
				NewPoints: 0,
				Points:    scoreTotal,
			})
			socket.SendQueueMessageSocketID(response, socketID)
		}
	} else {
		scoreTotal := c.commonScore.total
		c.lives--
		lives := c.lives
		c.receiving.Unlock()

		response := socket.RawMessage{}
		response.ParseMessagePack(byte(socket.MessageType.ResponseGuess), GuessResponse{
			Valid:     false,
			NewPoints: 0,
			Points:    scoreTotal,
		})
		socket.SendQueueMessageSocketID(response, socketID)

		if player != nil {
			c.receiving.Lock()
			messageFail := socket.RawMessage{}
			messageFail.ParseMessagePack(byte(socket.MessageType.GuessFailUser), GuessFail{
				Username: player.Username,
				UserID:   player.userID.String(),
				Lives:    lives,
			})
			c.broadcast(&messageFail)
			c.receiving.Unlock()
		}

		if lives <= 0 {
			log.Printf("[Match] [Coop] No more lives for the drawing. Penalty will apply ,match: %s", c.info.ID)
			c.receiving.Lock()
			if c.cancelWait != nil {
				c.receiving.Unlock()
				c.cancelWait()
			} else {
				c.receiving.Unlock()
			}

			c.applyPenalty()
			return
		}

		c.syncPlayers()
	}
}

//HintRequested for the current virtual player drawing
func (c *Coop) HintRequested(socketID uuid.UUID) {

	c.receiving.Lock()
	player := c.connections[socketID]
	gameDuration := time.Now().Sub(c.timeStart)
	remaining := c.gameTime - gameDuration.Milliseconds() + c.checkPointTime
	c.receiving.Unlock()

	if remaining > 10000 {
		hintSent := virtualplayer.GetHintByBot(&match2.HintRequested{
			GameType: c.info.GameType,
			MatchID:  c.info.ID,
			SocketID: socketID,
			Player: match2.Player{
				IsCPU:    player.IsCPU,
				Username: player.Username,
				ID:       player.userID,
			},
			DrawerID: c.curDrawer.userID,
		})

		if hintSent {
			c.applyPenalty()
		}
	} else {
		c.receiving.Lock()
		message := socket.RawMessage{}
		message.ParseMessagePack(byte(socket.MessageType.ResponseHintMatch), HintResponse{
			Hint:   "",
			Error:  language.MustGet("error.hintTime", c.info.Language),
			UserID: player.userID.String(),
			BotID:  c.curDrawer.userID.String(),
		})
		c.broadcast(&message)
		c.receiving.Unlock()
	}

}

//Close method used to force close the current game
func (c *Coop) Close() {
	cbroadcast.Broadcast(match2.BGameEnds, c.info.ID)

	c.receiving.Lock()
	log.Printf("[Match] [Coop] Force match shutdown, the game will close")
	if c.isRunning {
		close(c.closingTimeKeeper)
	}
	if c.cancelWait != nil {
		c.cancelWait()
		c.isRunning = false
	}
	duration := time.Now().Sub(c.timeStart).Milliseconds()

	matchType := 1

	if len(c.connections) > 1 {
		matchType = 2
	}
	total := c.commonScore.total
	c.receiving.Unlock()

	cbroadcast.Broadcast(broadcast.BUpdateMatch, match2.StatsData{SocketsID: c.GetConnections(), Match: &model.MatchPlayed{
		MatchDuration:  duration,
		MatchType:      matchType,
		PointsSoloCoop: total}})

	cancelMessage := socket.RawMessage{}
	cancelMessage.ParseMessagePack(byte(socket.MessageType.GameCancel), GameCancel{
		Type: 2,
	})

	c.receiving.Lock()
	c.broadcast(&cancelMessage)

	c.sendGameEndMessage(duration)
	c.receiving.Unlock()

	messenger.UnRegisterGroup(&c.info, c.GetConnections())
}

//GetConnections method used to return all the connections of the players
func (c *Coop) GetConnections() []uuid.UUID {
	defer c.receiving.Unlock()
	c.receiving.Lock()

	connections := make([]uuid.UUID, 0, len(c.players))
	for i := range c.players {
		if !c.players[i].IsCPU {
			connections = append(connections, c.players[i].socketID)
		}
	}
	return connections
}

//GetWelcome message used for the broadcast of the type of game
func (c *Coop) GetWelcome() socket.RawMessage {
	defer c.receiving.Unlock()
	c.receiving.Lock()
	players := make([]PlayersData, 0, len(c.info.Users))
	for i := range c.info.Users {
		players = append(players, PlayersData{
			UserID:   c.info.Users[i].ID.String(),
			Username: c.info.Users[i].Username,
			Points:   0,
			IsCPU:    c.info.Users[i].IsCPU,
		})
	}
	welcome := ResponseGameInfo{
		Players:   players,
		GameType:  c.info.GameType,
		TimeImage: c.timeImage,
		Laps:      0,
		TotalTime: c.gameTime,
		Lives:     c.lives,
	}
	message := socket.RawMessage{}
	message.ParseMessagePack(byte(socket.MessageType.GameWelcome), welcome)
	return message
}

//GetGroupID return group id
func (c *Coop) GetGroupID() uuid.UUID {
	defer c.receiving.Unlock()
	c.receiving.Lock()
	return c.info.ID
}

//GetPlayers return all the players
func (c *Coop) GetPlayers() []match2.Player {
	defer c.receiving.Unlock()
	c.receiving.Lock()
	return c.getPlayers()
}

//finish used to properly finish the coop mode
func (c *Coop) finish() {
	cbroadcast.Broadcast(match2.BGameEnds, c.info.ID)
	c.receiving.Lock()
	duration := time.Now().Sub(c.timeStart).Milliseconds()
	total := c.commonScore.total
	c.isRunning = false
	if c.cancelWait != nil {
		c.receiving.Unlock()
		c.cancelWait()

		<-c.lastLoop //wait for the last loop to end
	} else {
		c.receiving.Unlock()
	}

	matchType := 1

	if len(c.connections) > 1 {
		matchType = 2
	}

	cbroadcast.Broadcast(broadcast.BUpdateMatch, match2.StatsData{SocketsID: c.GetConnections(), Match: &model.MatchPlayed{
		MatchDuration:  duration,
		MatchType:      matchType,
		PointsSoloCoop: total}})

	c.receiving.Lock()
	//Send the time's up message
	timeUpMessage := socket.RawMessage{}
	timeUpMessage.ParseMessagePack(byte(socket.MessageType.TimeUp), TimeUp{
		Type: 2,
		Word: c.currentWord,
	})
	c.broadcast(&timeUpMessage)
	c.sendGameEndMessage(duration)

	log.Printf("[Match] [Coop] Match is finished!, match %s", c.info.ID)

	c.receiving.Unlock()
	messenger.UnRegisterGroup(&c.info, c.GetConnections())
}

//computeOrder used to compute the order for the coop
func (c *Coop) computeOrder() {
	c.nbVirtualPlayers = 0
	c.realPlayers = 0

	//Count the number of virtualplayers
	for i := range c.players {
		if c.players[i].IsCPU {
			c.orderVirtual[c.nbVirtualPlayers] = &c.players[i]
			c.nbVirtualPlayers++
		} else {
			c.realPlayers++
		}
	}
}

//computeDifficulty determine the time associated with the game
func (c *Coop) computeDifficulty() {
	//Determine the time based of the difficulty
	switch c.info.Difficulty {
	case 0:
		c.lives = 3
		c.gameTime = 60
		c.timeImage = 15
		c.penalty = 10
		c.minimumBonus = 5

		c.minimumScore = 50
		c.checkpointScore = 100
	case 1:
		c.lives = 2
		c.gameTime = 45
		c.timeImage = 10
		c.penalty = 15
		c.minimumBonus = 4

		c.minimumScore = 100
		c.checkpointScore = 200
	case 2:
		c.lives = 1
		c.gameTime = 30
		c.timeImage = 10
		c.penalty = 20
		c.minimumBonus = 2

		c.minimumScore = 200
		c.checkpointScore = 400
	}
	c.chances = c.lives
	c.gameTime *= 1000
	c.timeImage *= 1000
	c.penalty *= 1000
	c.minimumBonus *= 1000
}

//syncPlayers used to send all the sync to all the players
func (c *Coop) syncPlayers() {
	c.receiving.Lock()
	players := make([]PlayersData, len(c.players))
	for i := range c.players {
		player := &c.players[i]
		players[i] = PlayersData{
			Username: player.Username,
			UserID:   player.userID.String(),
			IsCPU:    player.IsCPU,
		}
		if !player.IsCPU {
			players[i].Points = c.commonScore.total
		}
	}
	lives := c.lives

	message := socket.RawMessage{}
	gameDuration := time.Now().Sub(c.timeStart)
	timeRemaining := c.gameTime - gameDuration.Milliseconds() + c.checkPointTime
	if timeRemaining < 0 {
		timeRemaining = 0
	}

	message.ParseMessagePack(byte(socket.MessageType.PlayerSync), PlayerSync{
		Players:  players,
		Laps:     c.curLap,
		LapTotal: 0,
		Time:     timeRemaining,
		Lives:    lives,
	})
	c.broadcast(&message)
	c.receiving.Unlock()
}

func (c *Coop) waitGuess() bool {
	ch := make(chan struct{})

	go func() {
		for {
			select {
			case <-time.After(time.Second):
				//Send an update to the clients
				c.funcSyncPlayer()
			case <-ch:
				return
			}
		}
	}()

	cnt := context.Background()
	cnt, c.cancelWait = context.WithCancel(cnt)
	err := c.waitingResponse.Acquire(cnt, c.nbWaitingResponses)
	c.cancelWait()

	close(ch)
	if err == nil {
		c.receivingGuesses.UnSet()
		return false // completed normally
	}

	c.receivingGuesses.UnSet()
	return true // timed out
}

//applyPenalty, is calling lock
func (c *Coop) applyPenalty() {
	c.receiving.Lock()
	c.checkPointTime -= c.penalty

	gameDuration := time.Now().Sub(c.timeStart)
	remaining := c.gameTime - gameDuration.Milliseconds() + c.checkPointTime

	checkpoint := socket.RawMessage{}
	checkpoint.ParseMessagePack(byte(socket.MessageType.Checkpoint), Checkpoint{
		TotalTime: remaining,
		Bonus:     -c.penalty,
	})
	c.broadcast(&checkpoint)
	c.receiving.Unlock()

	c.syncPlayers()
}

//sendGameEndMessage used to broadcast the end of the game
func (c *Coop) sendGameEndMessage(duration int64) {
	playersDetails := make([]PlayersData, len(c.players))
	for i := range c.players {
		player := &c.players[i]
		var points int
		if !player.IsCPU {
			points = c.commonScore.total
		} else {
			points = 0
		}
		playersDetails[i] = PlayersData{
			UserID:   player.userID.String(),
			Username: player.Username,
			IsCPU:    player.IsCPU,
			Points:   points,
		}
	}
	message := socket.RawMessage{}
	message.ParseMessagePack(byte(socket.MessageType.GameEnded), GameEnded{
		Players:    playersDetails,
		Winner:     uuid.Nil.String(),
		WinnerName: "",
		Time:       duration,
	})
	c.broadcast(&message)
}

//sendRoundSummary used to send a summary of the round
func (c *Coop) sendRoundSummary() {
	roundEnd := socket.RawMessage{}
	playersDetails := make([]PlayersRoundSum, len(c.players))
	for i := range c.players {
		player := &c.players[i]
		var points, newPoints int
		if !player.IsCPU {
			points = c.commonScore.total
			newPoints = c.commonScore.current
		} else {
			points = 0
			newPoints = 0
		}
		playersDetails[i] = PlayersRoundSum{
			PlayersData: PlayersData{
				UserID:   player.userID.String(),
				Username: player.Username,
				IsCPU:    player.IsCPU,
				Points:   points,
			},
			NewPoints: newPoints,
		}
	}
	roundEnd.ParseMessagePack(byte(socket.MessageType.RoundEndStatus), RoundSummary{
		Players:      playersDetails,
		Achievements: nil,
		Word:         c.currentWord,
		Guessed:      c.wordFound,
	})
	c.broadcast(&roundEnd)
}
