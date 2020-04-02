package auth

import (
	"log"

	"github.com/tevino/abool"

	"github.com/google/uuid"
	service "gitlab.com/jigsawcorp/log3900/internal/services"
	"gitlab.com/jigsawcorp/log3900/internal/socket"
	"gitlab.com/jigsawcorp/log3900/model"
	"gitlab.com/jigsawcorp/log3900/pkg/cbroadcast"
)

//BLanguage message for the language change
const BLanguage = "auth:lang"

var removingSessions *abool.AtomicBool

type languageMessage struct {
	Language int
}

//Auth service used to debug the message received by the server
type Auth struct {
	close    cbroadcast.Channel
	language cbroadcast.Channel
	shutdown chan bool
}

//Init the auth service
func (a *Auth) Init() {
	a.shutdown = make(chan bool)
	removingSessions = abool.New()
	initTokenAvailable()
	a.subscribe()
}

//Start the auth service
func (a *Auth) Start() {
	log.Println("[Auth] -> Starting service")
	go a.listen()

	//TODO make a watchdog to update the database if no more data is present
}

//Shutdown the auth service
func (a *Auth) Shutdown() {
	log.Println("[Auth] -> Closing service")
	a.clearSessionDB()

	close(a.shutdown)
}

//Register register any broadcast
func (a *Auth) Register() {
	cbroadcast.Register(BLanguage, 5)
}

func (a *Auth) listen() {
	defer service.Closed()

	for {
		select {
		case id := <-a.close:
			log.Printf("[Auth] -> Disconnect! UnRegistering session: %s", id)
			sockedID, _ := id.(uuid.UUID)
			UnRegisterSocket(sockedID)

		case data := <-a.language:
			message := data.(socket.RawMessageReceived)

			var languageMessage languageMessage
			if message.Payload.DecodeMessagePack(&languageMessage) == nil {
				log.Printf("[Auth] -> Received language change, socketID: %s", message.SocketID)
				ChangeLang(message.SocketID, languageMessage.Language)
			}
		case <-a.shutdown:
			return
		}
	}

}

func (a *Auth) subscribe() {
	a.close, _ = cbroadcast.Subscribe(socket.BSocketAuthCloseClient)
	a.language, _ = cbroadcast.Subscribe(BLanguage)
}

//clearSessionDB to make sure we start in an known state
func (a *Auth) clearSessionDB() {
	removingSessions.Set()
	model.DB().DropTableIfExists(&model.Session{})
	model.DB().CreateTable(&model.Session{})
}
