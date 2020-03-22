package virtualplayer

import (
	"gitlab.com/jigsawcorp/log3900/internal/socket"
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
		printManager()
		select {
		case data := <-v.gameStarts:
			log.Println("[Virtual Player] -> Receives game Start message")
			match, ok := data.(match.IMatch)
			if !ok {
				log.Println("[Virtual Player] -> [Error] Error while parsing match.IMatch struct")
				break
			}
			go handleStartGame(match)

		case data := <-v.gameEnds:
			log.Println("[Virtual Player] -> Sends game End message")
			groupID, ok := data.(uuid.UUID)
			if !ok {
				log.Println("[Virtual Player] -> [Error] Error while parsing uuid")
				break
			}
			go handleEndGame(groupID)

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

		case data := <-v.roundEnds:
			log.Println("[Virtual Player] -> Sends round End message")
			groupID, ok := data.(uuid.UUID)
			if !ok {
				log.Println("[Virtual Player] -> [Error] Error while parsing uuid")
				break
			}
			go handleRoundEnds(groupID)

		case data := <-v.chatNew:
			log.Println("[Virtual Player] -> Sends chat New message")
			chat, ok := data.(match.ChatNew)
			if !ok {
				log.Println("[Virtual Player] -> [Error] Error while parsing match.ChatNew struct")
				break
			}
			go registerChannelGroup(chat.MatchID, chat.ChatID)

		case data := <-v.askHint:
			if message, ok := data.(socket.RawMessageReceived); ok {
				log.Printf("[Match] -> received HINT request socket id: %v", message.SocketID)
			}

		case <-v.shutdown:
			return
		}
	}

}

func (v *VirtualPlayer) subscribe() {
	v.gameStarts, _, _ = cbroadcast.Subscribe(match.BGameStarts)
	v.gameEnds, _, _ = cbroadcast.Subscribe(match.BGameEnds)
	v.roundStarts, _, _ = cbroadcast.Subscribe(match.BRoundStarts)
	v.roundEnds, _, _ = cbroadcast.Subscribe(match.BRoundEnds)
	v.askHint, _, _ = cbroadcast.Subscribe(match.BAskHint)
	v.chatNew, _, _ = cbroadcast.Subscribe(match.BChatNew)
}

//Register the broadcast for drawing
func (v *VirtualPlayer) Register() {
	cbroadcast.Register(match.BGameStarts, match.BSize)
	cbroadcast.Register(match.BGameEnds, match.BSize)
	cbroadcast.Register(match.BRoundStarts, match.BSize)
	cbroadcast.Register(match.BRoundEnds, match.BSize)
	cbroadcast.Register(match.BAskHint, match.BSize)
	cbroadcast.Register(match.BChatNew, match.BSize)
}

func printManager() {
	log.Printf("[Virtual Player] -> Manager.Channels : %v", managerInstance.Channels)
	log.Printf("[Virtual Player] -> Manager.Groups : %v", managerInstance.Groups)
	log.Printf("[Virtual Player] -> Manager.Bots : %v", managerInstance.Bots)
	log.Printf("[Virtual Player] -> Manager.Games : %v", managerInstance.Games)
}
