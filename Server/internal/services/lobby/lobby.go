package lobby

import (
	"log"

	"github.com/google/uuid"
	"gitlab.com/jigsawcorp/log3900/model"

	service "gitlab.com/jigsawcorp/log3900/internal/services"
	"gitlab.com/jigsawcorp/log3900/internal/socket"
	"gitlab.com/jigsawcorp/log3900/pkg/cbroadcast"
)

//Lobby service used the manage the groups before the match
type Lobby struct {
	connected cbroadcast.Channel
	close     cbroadcast.Channel
	groups    *groups
	shutdown  chan bool
}

//Init the lobby service
func (l *Lobby) Init() {
	l.shutdown = make(chan bool)
	l.groups = &groups{}
	l.groups.Init()

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

//CreateGroup method used to broadcast a message that a new group was created
func (l *Lobby) CreateGroup(group *model.Group) {
	l.groups.AddGroup(group)
}

func (l *Lobby) listen() {
	defer service.Closed()

	for {
		select {
		case id := <-l.connected:
			log.Printf("[Lobby] -> New session id: %s", id)
			l.groups.RegisterSession(id.(uuid.UUID))
		case id := <-l.close:
			log.Printf("[Lobby] -> Session disconnected id: %s", id)
			l.groups.UnRegisterSession(id.(uuid.UUID))
		case <-l.shutdown:
			return
		}
	}

}

func (l *Lobby) subscribe() {
	l.connected, _, _ = cbroadcast.Subscribe(socket.BSocketAuthConnected)
	l.close, _, _ = cbroadcast.Subscribe(socket.BSocketAuthCloseClient)
}
