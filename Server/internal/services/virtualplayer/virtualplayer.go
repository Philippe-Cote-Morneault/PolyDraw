package virtualplayer

import (
	"log"

	service "gitlab.com/jigsawcorp/log3900/internal/services"
	"gitlab.com/jigsawcorp/log3900/internal/socket"
	"gitlab.com/jigsawcorp/log3900/pkg/cbroadcast"
)

//VirtualPlayer service used to debug the message received by the server
type VirtualPlayer struct {
	kickPlayer cbroadcast.Channel
	addPlayer  cbroadcast.Channel
	gameStarts cbroadcast.Channel
	roundEnds  cbroadcast.Channel
	askHint    cbroadcast.Channel

	shutdown chan bool
}

//Init the messenger service
func (v *VirtualPlayer) Init() {
	v.shutdown = make(chan bool)
	GetManagerInstance().init()
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
		case data := <-v.addPlayer:
			log.Println("[Virtual Player] -> Adding Virtual Player")
			if message, ok := data.(socket.RawMessageReceived); ok {
				//Start a new function to add players
				go v.addVirtualPlayer(message)
			}

		case data := <-v.kickPlayer:
			log.Println("[Virtual Player] -> Kicking Virtual Player")
			if message, ok := data.(socket.RawMessageReceived); ok {
				//Start a new function to kick players
				go v.kickVirtualPlayer(message)
			}
		case <-v.gameStarts:
			log.Println("[Virtual Player] -> Sends game Start message")

		case <-v.roundEnds:
			log.Println("[Virtual Player] -> Sends round End message")

		case <-v.shutdown:
			return
		}
	}

}

func (v *VirtualPlayer) subscribe() {
	v.addPlayer, _, _ = cbroadcast.Subscribe(BAddPlayer)
	v.kickPlayer, _, _ = cbroadcast.Subscribe(BKickPlayer)
	v.gameStarts, _, _ = cbroadcast.Subscribe(BGameStarts)
	v.roundEnds, _, _ = cbroadcast.Subscribe(BRoundEnds)
	v.askHint, _, _ = cbroadcast.Subscribe(BAskHint)
}
