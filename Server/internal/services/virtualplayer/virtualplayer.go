package virtualplayer

import (
	"log"

	"github.com/google/uuid"

	"gitlab.com/jigsawcorp/log3900/internal/match"
	service "gitlab.com/jigsawcorp/log3900/internal/services"
	"gitlab.com/jigsawcorp/log3900/pkg/cbroadcast"
)

//VirtualPlayer service used to debug the message received by the server
type VirtualPlayer struct {
	gameStarts  cbroadcast.Channel
	gameEnds    cbroadcast.Channel
	roundStarts cbroadcast.Channel
	roundEnds   cbroadcast.Channel
	chatNew     cbroadcast.Channel
	playerLeft  cbroadcast.Channel

	shutdown chan bool
}

//Init the virtualplayer service
func (v *VirtualPlayer) Init() {
	v.shutdown = make(chan bool)
	(&managerInstance).init()
	v.subscribe()
}

//Start the virtualplayer service
func (v *VirtualPlayer) Start() {
	log.Println("[VirtualPlayer] -> Starting service")
	go v.listen()
}

//Shutdown the virtualplayer service
func (v *VirtualPlayer) Shutdown() {
	log.Println("[VirtualPlayer] -> Closing service")
	close(v.shutdown)
}

func (v *VirtualPlayer) listen() {
	defer service.Closed()

	//Message viewer
	for {
		select {
		case data := <-v.gameStarts:
			log.Println("[VirtualPlayer] -> Receives gameStart event")
			match, ok := data.(match.IMatch)
			if !ok {
				log.Println("[VirtualPlayer] -> [Error] Error while parsing match.IMatch struct")
				break
			}
			go handleStartGame(match)

		case data := <-v.gameEnds:
			log.Println("[VirtualPlayer] -> Receives gameEnds event")

			groupID, ok := data.(uuid.UUID)
			if !ok {
				log.Println("[VirtualPlayer] -> [Error] Error while parsing uuid")
				break
			}
			go handleEndGame(groupID)

		case data := <-v.roundStarts:
			log.Println("[VirtualPlayer] -> Receives roundStarts event")
			round, ok := data.(match.RoundStart)
			if !ok {
				log.Println("[VirtualPlayer] -> [Error] Error while parsing match.RoundStart struct")
				break
			}

			if round.Drawer.IsCPU {
				go startDrawing(&round)
			}

		case data := <-v.roundEnds:
			log.Println("[VirtualPlayer] -> Receives roundEnds event")
			groupID, ok := data.(uuid.UUID)
			if !ok {
				log.Println("[VirtualPlayer] -> [Error] Error while parsing uuid")
				break
			}
			go handleRoundEnds(groupID, true)

		case data := <-v.chatNew:
			log.Println("[VirtualPlayer] -> Receives chatNew event")
			chat, ok := data.(match.ChatNew)
			if !ok {
				log.Println("[VirtualPlayer] -> [Error] Error while parsing match.ChatNew struct")
				break
			}
			go registerChannelGroup(chat.MatchID, chat.ChatID)

		case data := <-v.playerLeft:
			log.Println("[VirtualPlayer] -> Receives playerLeft event")
			socketID, ok := data.(uuid.UUID)
			if !ok {
				log.Println("[VirtualPlayer] -> [Error] Error while parsing uuid.UUID struct")
				break
			}
			go stopDrawingOfSocket(socketID)

		case <-v.shutdown:
			return
		}
	}

}

func (v *VirtualPlayer) subscribe() {
	v.gameStarts, _ = cbroadcast.Subscribe(match.BGameStarts)
	v.gameEnds, _ = cbroadcast.Subscribe(match.BGameEnds)
	v.roundStarts, _ = cbroadcast.Subscribe(match.BRoundStarts)
	v.roundEnds, _ = cbroadcast.Subscribe(match.BRoundEnds)
	v.chatNew, _ = cbroadcast.Subscribe(match.BChatNew)
	v.playerLeft, _ = cbroadcast.Subscribe(match.BPlayerLeft)
}

//Register the broadcast for virtualplayer
func (v *VirtualPlayer) Register() {
	cbroadcast.Register(match.BGameStarts, match.BSize)
	cbroadcast.Register(match.BGameEnds, match.BSize)
	cbroadcast.Register(match.BRoundStarts, match.BSize)
	cbroadcast.Register(match.BRoundEnds, match.BSize)
	cbroadcast.Register(match.BChatNew, match.BSize)
	cbroadcast.Register(match.BPlayerLeft, match.BSize)
}
