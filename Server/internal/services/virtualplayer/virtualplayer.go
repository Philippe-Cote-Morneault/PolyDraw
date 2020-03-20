package virtualplayer

import (
	"log"

	"gitlab.com/jigsawcorp/log3900/internal/match"
	service "gitlab.com/jigsawcorp/log3900/internal/services"
	"gitlab.com/jigsawcorp/log3900/pkg/cbroadcast"
)

//VirtualPlayer service used to debug the message received by the server
type VirtualPlayer struct {
	kickPlayer  cbroadcast.Channel
	gameStarts  cbroadcast.Channel
	gameEnds    cbroadcast.Channel
	roundStarts cbroadcast.Channel
	roundEnds   cbroadcast.Channel
	askHint     cbroadcast.Channel
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
	log.Println("[Virtual Player] -> Starting service")
	go v.listen()
}

//Shutdown the messenger service
func (v *VirtualPlayer) Shutdown() {
	log.Println("[Virtual Player] -> Closing service")
	close(v.shutdown)
}

func (v *VirtualPlayer) listen() {
	defer service.Closed()

	//Message viewer
	for {
		select {
		case data := <-v.gameStarts:
			log.Println("[Virtual Player] -> Receives game Start message")
			match, ok := data.(match.IMatch)
			if !ok {
				log.Println("[Virtual Player] -> [Error] Error while parsing match.RoundStart struct")
				break
			}
			startGame(match)

		case data := <-v.roundStarts:
			log.Println("[Virtual Player] -> Receives round Start message")

			round, ok := data.(match.RoundStart)
			if !ok {
				log.Println("[Virtual Player] -> [Error] Error while parsing match.RoundStart struct")
				break

			}

			if round.Drawer.IsCPU {
				go startDrawing(&round)
			}

			log.Println(*round.Game)

		case <-v.roundEnds:
			log.Println("[Virtual Player] -> Sends round End message")

		case <-v.shutdown:
			return
		}
	}

}

func (v *VirtualPlayer) subscribe() {
	v.kickPlayer, _, _ = cbroadcast.Subscribe(match.BKickPlayer)
	v.gameStarts, _, _ = cbroadcast.Subscribe(match.BGameStarts)
	v.gameEnds, _, _ = cbroadcast.Subscribe(match.BGameEnds)
	v.roundStarts, _, _ = cbroadcast.Subscribe(match.BRoundStarts)
	v.roundEnds, _, _ = cbroadcast.Subscribe(match.BRoundEnds)
	v.askHint, _, _ = cbroadcast.Subscribe(match.BAskHint)
	v.chatNew, _, _ = cbroadcast.Subscribe(match.BChatNew)
}

//Register the broadcast for drawing
func (v *VirtualPlayer) Register() {
	cbroadcast.Register(match.BKickPlayer, match.BSize)
	cbroadcast.Register(match.BGameStarts, match.BSize)
	cbroadcast.Register(match.BGameEnds, match.BSize)
	cbroadcast.Register(match.BRoundStarts, match.BSize)
	cbroadcast.Register(match.BRoundEnds, match.BSize)
	cbroadcast.Register(match.BAskHint, match.BSize)
	cbroadcast.Register(match.BChatNew, match.BSize)
}
