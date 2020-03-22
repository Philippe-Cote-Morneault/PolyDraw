package mode

import (
	"gitlab.com/jigsawcorp/log3900/internal/services/messenger"
	"gitlab.com/jigsawcorp/log3900/internal/services/virtualplayer"
	"log"
	"strings"
	"sync"
	"time"

	"github.com/google/uuid"
	"github.com/tevino/abool"
	match2 "gitlab.com/jigsawcorp/log3900/internal/match"
	"gitlab.com/jigsawcorp/log3900/internal/socket"
	"gitlab.com/jigsawcorp/log3900/model"
	"gitlab.com/jigsawcorp/log3900/pkg/cbroadcast"
	"golang.org/x/sync/semaphore"
)

const numberOfChances = 3

//Coop represent a cooperative game mode
type Coop struct {
	base
	wordHistory  map[string]bool
	orderVirtual []*players
	curDrawer    *players
	orderPos     int
	chances      int
	isRunning    bool
	currentWord  string
	realPlayers  int
	cancelWait   func()
	commonScore  score

	gameTime       int64
	checkPointTime int64
	lives          int

	receiving        sync.Mutex
	timeStart        time.Time
	timeStartImage   time.Time
	waitingResponse  *semaphore.Weighted
	receivingGuesses *abool.AtomicBool
	nbVirtualPlayers int
	curLap           int
	closing          chan struct{}
}

//Init creates the coop game mode
func (c *Coop) Init(connections []uuid.UUID, info model.Group) {
	c.init(connections, info)

	c.chances = numberOfChances
	c.timeImage = imageDuration
	c.isRunning = true
	c.orderPos = 0
	c.nbWaitingResponses = 1
	c.lives = 3
	c.curLap = 1
	c.checkPointTime = 0
	c.commonScore.init()
	c.orderVirtual = make([]*players, info.VirtualPlayers)
	c.closing = make(chan struct{})

	c.receivingGuesses = abool.New()
	c.funcSyncPlayer = c.syncPlayers

	c.computeDifficulty()
	c.computeOrder()

	cbroadcast.Broadcast(match2.BGameStarts, c)
}

//Start the game and the game loop
func (c *Coop) Start() {
	c.waitForPlayers()

	//We can start the game loop
	log.Printf("[Match] [Coop] -> Starting gameloop Match: %s", c.info.ID)

	timeOut := make(chan bool)
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
			case <-c.closing:
				return
			}
		}
	}()

	c.timeStart = time.Now()
	for c.isRunning {
		c.GameLoop()
	}
	<-timeOut
}

//GameLoop is called every new round
func (c *Coop) GameLoop() {
	c.receiving.Lock()
	c.curDrawer = &c.players[c.orderPos]
	drawingID := uuid.New()

	game := c.findGame()

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
		Length:    len(c.currentWord),
	})

	c.pbroadcast(&message)
	c.timeStartImage = time.Now()
	log.Printf("[Match] [Coop] -> Word sent waiting for guesses, Match: %s", c.info.ID)
	c.receiving.Unlock()

	c.receivingGuesses.Set()

	if c.waitTimeout() {
		log.Printf("[Match] [Coop] -> Time's up. The word could not be found, Match: %s", c.info.ID)
	} else {
		log.Printf("[Match] [Coop] -> The word was found, Match: %s", c.info.ID)
	}

	timeUpMessage := socket.RawMessage{}
	timeUpMessage.ParseMessagePack(byte(socket.MessageType.TimeUp), TimeUp{
		Type: 1,
		Word: c.currentWord,
	})
	c.pbroadcast(&timeUpMessage)

	//End of round
	c.receiving.Lock()
	if c.lives <= 0 {
		c.isRunning = false
		c.receiving.Unlock()
		//Exit the game since all the lives are expired
		return
	}

	if c.realPlayers <= 0 {
		c.isRunning = false
		c.receiving.Unlock()

		c.Close()
		return
	}

	//Prepare next round
	c.orderPos++
	c.curLap++
	c.orderPos = c.orderPos % c.nbVirtualPlayers
	c.commonScore.reset()
	c.currentWord = ""
	c.receiving.Unlock()
	cbroadcast.Broadcast(match2.BRoundEnds, c.info.ID)

	time.Sleep(time.Millisecond * 500)
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
	player := c.connections[socketID]

	leaveMessage := socket.RawMessage{}
	leaveMessage.ParseMessagePack(byte(socket.MessageType.PlayerHasLeftGame), PlayerHasLeft{
		UserID:   player.userID.String(),
		Username: player.Username,
	})
	c.pbroadcast(&leaveMessage)

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

	messenger.HandleQuitGroup(&c.info, socketID)
	c.syncPlayers()
}

//TryWord handle when a client wants to try a word
func (c *Coop) TryWord(socketID uuid.UUID, word string) {
	c.receiving.Lock()
	log.Printf("[Match] [Coop] Guessing the word for the socket id %s", socketID)
	if strings.ToLower(strings.TrimSpace(word)) == c.currentWord && c.currentWord != "" && c.lives > 0 {
		//The word was found
		if c.receivingGuesses.IsSet() {
			imageDuration := time.Now().Sub(c.timeStartImage)
			bonus := c.timeImage - imageDuration.Milliseconds()

			c.checkPointTime += bonus
			gameDuration := time.Now().Sub(c.timeStart)
			remaining := c.gameTime - gameDuration.Milliseconds() + c.checkPointTime

			c.waitingResponse.Release(1)
			player := c.connections[socketID]

			pointsForWord := 100
			c.commonScore.commit(pointsForWord)
			total := c.commonScore.total

			c.receiving.Unlock()

			response := socket.RawMessage{}
			response.ParseMessagePack(byte(socket.MessageType.ResponseGuess), GuessResponse{
				Valid:     true,
				Points:    total,
				NewPoints: pointsForWord,
			})
			socket.SendRawMessageToSocketID(response, socketID)

			//Broadcast to all the other players that the word was found
			broadcast := socket.RawMessage{}
			broadcast.ParseMessagePack(byte(socket.MessageType.WordFound), WordFound{
				Username:  player.Username,
				UserID:    player.userID.String(),
				Points:    total,
				NewPoints: pointsForWord,
			})
			c.pbroadcast(&broadcast)

			checkpoint := socket.RawMessage{}
			checkpoint.ParseMessagePack(byte(socket.MessageType.Checkpoint), Checkpoint{
				TotalTime: remaining,
				Bonus:     bonus,
			})
			c.pbroadcast(&checkpoint)
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
			socket.SendRawMessageToSocketID(response, socketID)
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
		c.pbroadcast(&response)
		if lives <= 0 {
			log.Printf("[Match] [Coop] No more lives in the match. Closing game ,match: %s", c.info.ID)
			c.finish()
		}

		c.syncPlayers()
	}
}

//HintRequested for the current virtual player drawing
func (c *Coop) HintRequested(socketID uuid.UUID) {
	player := c.connections[socketID]
	c.receiving.Unlock()

	hintSent := virtualplayer.GetHintByBot(match2.HintRequested{
		GameType: c.info.GameType,
		MatchID:  c.info.ID,
		SocketID: socketID,
		Player: match2.Player{
			IsCPU:    player.IsCPU,
			Username: player.Username,
			ID:       player.userID,
		},
	})
	if hintSent {
		c.receiving.Lock()
		penalty := int64(10 * 1000)
		c.checkPointTime -= penalty
		gameDuration := time.Now().Sub(c.timeStart)
		remaining := c.gameTime - gameDuration.Milliseconds() + c.checkPointTime
		c.receiving.Unlock()

		checkpoint := socket.RawMessage{}
		checkpoint.ParseMessagePack(byte(socket.MessageType.Checkpoint), Checkpoint{
			TotalTime: remaining,
			Bonus:     penalty,
		})
		c.pbroadcast(&checkpoint)

		c.syncPlayers()
	}
}

//Close method used to force close the current game
func (c *Coop) Close() {
	c.receiving.Lock()
	log.Printf("[Match] [Coop] Force match shutdown, the game will finish the last lap")
	if c.cancelWait != nil {
		c.cancelWait()
		c.isRunning = false
	}
	close(c.closing)
	c.receiving.Unlock()

	cbroadcast.Broadcast(match2.BGameEnds, c.info.ID)
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
			IsCPU:    false,
		})
	}
	welcome := ResponseGameInfo{
		Players:   players,
		GameType:  c.info.GameType,
		TimeImage: c.timeImage,
		Laps:      0,
		TotalTime: c.gameTime,
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
	c.receiving.Lock()
	if c.cancelWait != nil {
		c.cancelWait()
	}

	//Send the time's up message
	timeUpMessage := socket.RawMessage{}
	timeUpMessage.ParseMessagePack(byte(socket.MessageType.TimeUp), TimeUp{
		Type: 2,
		Word: c.currentWord,
	})
	c.pbroadcast(&timeUpMessage)

	c.receiving.Unlock()
	cbroadcast.Broadcast(match2.BGameEnds, c.info.ID)

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
		c.gameTime = 300
	case 1:
		c.gameTime = 240
	case 2:
		c.gameTime = 180
	case 3:
		c.gameTime = 120
	}
	c.gameTime *= 1000
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
			Points:   c.commonScore.total,
			IsCPU:    player.IsCPU,
		}
	}
	checkPointTime := c.checkPointTime
	lives := c.lives
	c.receiving.Unlock()

	message := socket.RawMessage{}
	imageDuration := time.Now().Sub(c.timeStartImage)
	gameDuration := time.Now().Sub(c.timeStart)
	message.ParseMessagePack(byte(socket.MessageType.PlayerSync), PlayerSync{
		Players:  players,
		Laps:     c.curLap,
		LapTotal: 0,
		Time:     c.timeImage - imageDuration.Milliseconds(),
		GameTime: c.gameTime - gameDuration.Milliseconds() + checkPointTime,
		Lives:    lives,
	})
	c.pbroadcast(&message)
}
