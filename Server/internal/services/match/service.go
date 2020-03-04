package match

import (
	"github.com/google/uuid"
	"log"

	service "gitlab.com/jigsawcorp/log3900/internal/services"
	"gitlab.com/jigsawcorp/log3900/internal/socket"
	"gitlab.com/jigsawcorp/log3900/pkg/cbroadcast"
)

//Service service used to manage the matches
type Service struct {
	close cbroadcast.Channel

	guess cbroadcast.Channel
	hint  cbroadcast.Channel
	quit  cbroadcast.Channel
	ready cbroadcast.Channel

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
	s.manager.Close()
}

func (s *Service) listen() {
	defer service.Closed()

	for {
		select {
		case id := <-s.close:
			log.Printf("[Match] -> disconnect in game id: %s", id)
			s.manager.Quit(id.(uuid.UUID))
		case data := <-s.quit:
			if message, ok := data.(socket.RawMessageReceived); ok {
				s.manager.Quit(message.SocketID)
			}
		case data := <-s.ready:
			if message, ok := data.(socket.RawMessageReceived); ok {
				s.manager.Ready(message.SocketID)
			}
		case data := <-s.guess:
			if message, ok := data.(socket.RawMessageReceived); ok {
				s.manager.Guess(message)
			}
		case data := <-s.hint:
			if message, ok := data.(socket.RawMessageReceived); ok {
				s.manager.Hint(message.SocketID)
			}
		case <-s.shutdown:
			return
		}
	}

}

func (s *Service) subscribe() {
	s.close, _, _ = cbroadcast.Subscribe(socket.BSocketAuthCloseClient)

	s.ready, _, _ = cbroadcast.Subscribe(BMatchReady)
	s.hint, _, _ = cbroadcast.Subscribe(BMatchHint)
	s.guess, _, _ = cbroadcast.Subscribe(BMatchGuess)
	s.quit, _, _ = cbroadcast.Subscribe(BMatchQuit)
}
