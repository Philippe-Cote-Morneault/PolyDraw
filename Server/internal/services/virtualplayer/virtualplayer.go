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

	shutdown chan bool
}

//Init the messenger service
func (v *VirtualPlayer) Init() {
	v.shutdown = make(chan bool)
	initLines()
	(&managerInstance).init()
	v.subscribe()
}

//Start the messenger service
func (v *VirtualPlayer) Start() {
	log.Println("[VirtualPlayer] -> Starting service")
	go v.listen()
}

//Shutdown the messenger service
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
			log.Println("[Virtual Player] -> Receives gameStart event")
			match, ok := data.(match.IMatch)
			if !ok {
				log.Println("[VirtualPlayer] -> [Error] Error while parsing match.IMatch struct")
				break
			}
			go handleStartGame(match)

		case data := <-v.gameEnds:
			log.Println("[Virtual Player] -> Receives gameEnds event")

			groupID, ok := data.(uuid.UUID)
			if !ok {
				log.Println("[VirtualPlayer] -> [Error] Error while parsing uuid")
				break
			}
			go handleEndGame(groupID)

		case data := <-v.roundStarts:
			log.Println("[Virtual Player] -> Receives roundStarts event")
			round, ok := data.(match.RoundStart)
			if !ok {
				log.Println("[VirtualPlayer] -> [Error] Error while parsing match.RoundStart struct")
				break
			}

			if round.Drawer.IsCPU {
				log.Println("[Virtual Player] -> About to call startDrawing")
				log.Printf("[Virtual Player] -> round : %v", round)
				log.Printf("[Virtual Player] -> round.Drawer : %v", round.Drawer)
				log.Printf("[Virtual Player] -> round.Game : %v", round.Game)
				log.Printf("[Virtual Player] -> round.Game.Hints : %v", round.Game.Hints)
				log.Printf("[Virtual Player] -> round.Game.Image : %v", *(round.Game.Image))
				go startDrawing(&round)
			}

		case data := <-v.roundEnds:
			log.Println("[Virtual Player] -> Receives roundEnds event")
			groupID, ok := data.(uuid.UUID)
			if !ok {
				log.Println("[VirtualPlayer] -> [Error] Error while parsing uuid")
				break
			}
			go handleRoundEnds(groupID)

		case data := <-v.chatNew:
			log.Println("[Virtual Player] -> Receives chatNew event")
			chat, ok := data.(match.ChatNew)
			if !ok {
				log.Println("[VirtualPlayer] -> [Error] Error while parsing match.ChatNew struct")
				break
			}
			go registerChannelGroup(chat.MatchID, chat.ChatID)

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
}

//Register the broadcast for drawing
func (v *VirtualPlayer) Register() {
	cbroadcast.Register(match.BGameStarts, match.BSize)
	cbroadcast.Register(match.BGameEnds, match.BSize)
	cbroadcast.Register(match.BRoundStarts, match.BSize)
	cbroadcast.Register(match.BRoundEnds, match.BSize)
	cbroadcast.Register(match.BChatNew, match.BSize)
}

func printManager(fromWho string) {
	log.Printf("[Virtual Player] -> {PrintManager} %v: ", fromWho)
	log.Printf("[Virtual Player] -> Manager.Bots : %v", managerInstance.Bots)
	log.Printf("[Virtual Player] -> Manager.Channels : %v", managerInstance.Channels)
	log.Printf("[Virtual Player] -> Manager.Games : %v", managerInstance.Games)
	log.Printf("[Virtual Player] -> Manager.Groups : %v", managerInstance.Groups)
	log.Printf("[Virtual Player] -> Manager.Matches : %v", managerInstance.Matches)
	log.Printf("[Virtual Player] -> Manager.Hints : %v", managerInstance.Hints)
}
