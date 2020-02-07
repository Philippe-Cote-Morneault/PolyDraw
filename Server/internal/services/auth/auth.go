package auth

import (
	"github.com/tevino/abool"
	"log"

	"github.com/google/uuid"
	service "gitlab.com/jigsawcorp/log3900/internal/services"
	"gitlab.com/jigsawcorp/log3900/internal/socket"
	"gitlab.com/jigsawcorp/log3900/model"
	"gitlab.com/jigsawcorp/log3900/pkg/cbroadcast"
)

var removingSessions *abool.AtomicBool

//Auth service used to debug the message received by the server
type Auth struct {
	close    cbroadcast.Channel
	shutdown chan bool
}

//Init the messenger service
func (a *Auth) Init() {
	a.shutdown = make(chan bool)
	removingSessions = abool.New()
	a.subscribe()
}

//Start the messenger service
func (a *Auth) Start() {
	log.Println("[Auth] -> Starting service")
	go a.listen()

	//TODO make a watchdog to update the database if no more data is present
}

//Shutdown the messenger service
func (a *Auth) Shutdown() {
	log.Println("[Auth] -> Closing service")
	a.clearSessionDB()

	close(a.shutdown)
}

//Register register any broadcast not used
func (a *Auth) Register() {

}

func (a *Auth) listen() {
	defer service.Closed()

	//Message viewer
	for {
		select {
		case id := <-a.close:
			log.Printf("[Auth] -> Disconnect! UnRegistering session: %s", id)
			sockedID, _ := id.(uuid.UUID)
			UnRegisterSocket(sockedID)
		case <-a.shutdown:
			return
		}
	}

}

func (a *Auth) subscribe() {
	a.close, _, _ = cbroadcast.Subscribe(socket.BSocketAuthCloseClient)
}

//clearSessionDB to make sure we start in an known state
func (a *Auth) clearSessionDB() {
	removingSessions.Set()
	model.DB().DropTableIfExists(&model.Session{})
	model.DB().CreateTable(&model.Session{})
}
