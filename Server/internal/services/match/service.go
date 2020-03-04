package match

import (
	"log"

	service "gitlab.com/jigsawcorp/log3900/internal/services"
	"gitlab.com/jigsawcorp/log3900/internal/socket"
	"gitlab.com/jigsawcorp/log3900/pkg/cbroadcast"
)

//Service service used to manage the matches
type Service struct {
	close   cbroadcast.Channel
	manager matchManager

	shutdown chan bool
}

//Init the match service
func (s *Service) Init() {
	s.shutdown = make(chan bool)
	s.manager = matchManager{}
	s.manager.Init()

	s.subscribe()
}

//Start the match service
func (s *Service) Start() {
	log.Println("[Match] -> Starting service")
	go s.listen()
}

//Shutdown the match service
func (s *Service) Shutdown() {
	log.Println("[Match] -> Closing service")
	close(s.shutdown)
}

func (s *Service) listen() {
	defer service.Closed()

	for {
		select {
		case id := <-s.close:
			log.Printf("[Match] -> disconnect in game id: %s", id)
		case <-s.shutdown:
			return
		}
	}

}

func (s *Service) subscribe() {
	s.close, _, _ = cbroadcast.Subscribe(socket.BSocketAuthCloseClient)
}
