package messenger

import (
	"log"

	"github.com/google/uuid"
	"gitlab.com/jigsawcorp/log3900/internal/socket"
	"gitlab.com/jigsawcorp/log3900/pkg/cbroadcast"
)

//Messenger service
type Messenger struct {
	shutdown        chan bool
	messageSentChan cbroadcast.Channel
	connectedChan   cbroadcast.Channel
	disconnectChan  cbroadcast.Channel
}

//Init the messenger service
func (m *Messenger) Init() {
	m.shutdown = make(chan bool)
	m.subscribe()
}

//Start the messenger service
func (m *Messenger) Start() {
	log.Println("[Messenger] -> Starting service")
	go m.listen()
}

//Shutdown the messenger service
func (m *Messenger) Shutdown() {
	log.Println("[Messenger] -> Closing service")
	close(m.shutdown)
}

//subscribe to broadcast channels
func (m *Messenger) subscribe() {
	m.messageSentChan, _, _ = cbroadcast.Subscribe(BMessageSent)
	m.connectedChan, _, _ = cbroadcast.Subscribe(socket.BSocketConnected)
	m.disconnectChan, _, _ = cbroadcast.Subscribe(socket.BSocketCloseClient)

}

func (m *Messenger) listen() {
	h := handler{}
	h.init()
	for {
		select {
		case <-m.shutdown:
			return
		case data := <-m.messageSentChan:
			if message, ok := data.(socket.RawMessageReceived); ok {
				h.handleMessgeSent(message)
			}
		case data := <-m.connectedChan:
			if socketID, ok := data.(uuid.UUID); ok {
				h.handleConnect(socketID)
			}
		case data := <-m.disconnectChan:
			if socketID, ok := data.(uuid.UUID); ok {
				h.handleDisconnect(socketID)
			}
		}
		//TODO add other handles here
	}
}
