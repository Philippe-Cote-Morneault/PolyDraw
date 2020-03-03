package lobby

import (
	"log"

	service "gitlab.com/jigsawcorp/log3900/internal/services"
	"gitlab.com/jigsawcorp/log3900/internal/socket"
	"gitlab.com/jigsawcorp/log3900/pkg/cbroadcast"
)

//Lobby service used the manage the group before the match
type Lobby struct {
	connected cbroadcast.Channel
	close     cbroadcast.Channel

	shutdown chan bool
}

//Init the lobby service
func (l *Lobby) Init() {
	l.shutdown = make(chan bool)
	l.subscribe()
}

//Start the lobby service
func (l *Lobby) Start() {
	log.Println("[Lobby] -> Starting service")
	go l.listen()
	//TODO include a cleanup for unused groups after x minutes
}

//Shutdown the lobby service
func (l *Lobby) Shutdown() {
	log.Println("[Lobby] -> Closing service")
	close(l.shutdown)
}

//Register register any broadcast not used
func (l *Lobby) Register() {

}

func (l *Lobby) listen() {
	defer service.Closed()

	for {
		select {
		case id := <-l.connected:
			log.Printf("[Lobby] -> New session id: %s", id)
		case id := <-l.close:
			log.Printf("[Lobby] -> Session disconnected id: %s", id)
		case <-l.shutdown:
			return
		}
	}

}

func (l *Lobby) subscribe() {
	l.connected, _, _ = cbroadcast.Subscribe(socket.BSocketAuthConnected)
	l.close, _, _ = cbroadcast.Subscribe(socket.BSocketAuthCloseClient)
}
