package stats

import (
	"log"

	"gitlab.com/jigsawcorp/log3900/internal/services/stats/broadcast"

	"github.com/google/uuid"
	"gitlab.com/jigsawcorp/log3900/internal/match"
	service "gitlab.com/jigsawcorp/log3900/internal/services"
	"gitlab.com/jigsawcorp/log3900/pkg/cbroadcast"
)

//Stats service used to debug the message received by the server
type Stats struct {
	updateMatch      cbroadcast.Channel
	setDeconnection  cbroadcast.Channel
	createConnection cbroadcast.Channel

	shutdown chan bool
}

//Init the stats service
func (s *Stats) Init() {
	s.shutdown = make(chan bool)
	s.subscribe()
}

//Start the stats service
func (s *Stats) Start() {
	log.Println("[Stats] -> Starting service")
	go s.listen()
}

//Shutdown the stats service
func (s *Stats) Shutdown() {
	log.Println("[Stats] -> Closing service")
	close(s.shutdown)
}

func (s *Stats) listen() {
	defer service.Closed()

	//Message viewer
	for {
		select {
		case data := <-s.updateMatch:
			log.Println("[Stats] -> Receives updateMatch event")
			match, ok := data.(match.StatsData)
			if !ok {
				log.Println("[Stats] -> [Error] Error while parsing model.MatchPlayed struct")
				break
			}
			updateMatchesPlayed(match)

		case data := <-s.setDeconnection:
			log.Println("[Stats] -> Receives setDeconnection event")

			userID, ok := data.(uuid.UUID)
			if !ok {
				log.Println("[Stats] -> [Error] Error while parsing uuid")
				break
			}
			setDisconnection(userID)

		case data := <-s.createConnection:
			log.Println("[Stats] -> Receives createConnection event")
			userID, ok := data.(uuid.UUID)
			if !ok {
				log.Println("[Stats] -> [Error] Error while parsing ChatNew struct")
				break
			}
			createConnection(userID)

		case <-s.shutdown:
			return
		}
	}

}

func (s *Stats) subscribe() {
	s.updateMatch, _ = cbroadcast.Subscribe(broadcast.BUpdateMatch)
	s.setDeconnection, _ = cbroadcast.Subscribe(broadcast.BSetDeconnection)
	s.createConnection, _ = cbroadcast.Subscribe(broadcast.BCreateConnection)
}

//Register the broadcast for stats
func (s *Stats) Register() {
	cbroadcast.Register(broadcast.BUpdateMatch, broadcast.BSize)
	cbroadcast.Register(broadcast.BSetDeconnection, broadcast.BSize)
	cbroadcast.Register(broadcast.BCreateConnection, broadcast.BSize)
}
