package match

import (
	"log"

	service "gitlab.com/jigsawcorp/log3900/internal/services"
	"gitlab.com/jigsawcorp/log3900/internal/socket"
	"gitlab.com/jigsawcorp/log3900/pkg/cbroadcast"
)

//Match service used to manage the matches
type Match struct {
	close cbroadcast.Channel

	shutdown chan bool
}

//Init the match service
func (m *Match) Init() {
	m.shutdown = make(chan bool)
	m.subscribe()
}

//Start the match service
func (m *Match) Start() {
	log.Println("[Match] -> Starting service")
	go m.listen()
}

//Shutdown the match service
func (m *Match) Shutdown() {
	log.Println("[Match] -> Closing service")
	close(m.shutdown)
}

//Register register any broadcast not used
func (m *Match) Register() {

}

func (m *Match) listen() {
	defer service.Closed()

	for {
		select {
		case id := <-m.close:
			log.Printf("[Match] -> disconnect in game id: %s", id)
		case <-m.shutdown:
			return
		}
	}

}

func (m *Match) subscribe() {
	m.close, _, _ = cbroadcast.Subscribe(socket.BSocketAuthCloseClient)
}
