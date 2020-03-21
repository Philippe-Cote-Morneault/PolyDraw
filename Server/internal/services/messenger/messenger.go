package messenger

import (
	"log"

	"github.com/google/uuid"
	service "gitlab.com/jigsawcorp/log3900/internal/services"
	"gitlab.com/jigsawcorp/log3900/internal/socket"
	"gitlab.com/jigsawcorp/log3900/pkg/cbroadcast"
)

//Messenger service
type Messenger struct {
	shutdown        chan bool
	messageSentChan cbroadcast.Channel
	createChan      cbroadcast.Channel
	destroyChan     cbroadcast.Channel
	joinChan        cbroadcast.Channel
	quitChan        cbroadcast.Channel
	connectedChan   cbroadcast.Channel
	disconnectChan  cbroadcast.Channel
	botMessage      cbroadcast.Channel
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
	m.connectedChan, _, _ = cbroadcast.Subscribe(socket.BSocketAuthConnected)
	m.disconnectChan, _, _ = cbroadcast.Subscribe(socket.BSocketAuthCloseClient)

	m.createChan, _, _ = cbroadcast.Subscribe(BCreateChannel)
	m.destroyChan, _, _ = cbroadcast.Subscribe(BDestroyChannel)
	m.joinChan, _, _ = cbroadcast.Subscribe(BJoinChannel)
	m.quitChan, _, _ = cbroadcast.Subscribe(BLeaveChannel)

	m.botMessage, _, _ = cbroadcast.Subscribe(BBotMessage)
}

func (m *Messenger) listen() {
	defer service.Closed()

	h := handler{}
	h.init()
	for {
		select {
		case <-m.shutdown:
			return
		case data := <-m.botMessage:
			if m, ok := data.(MessageReceived); ok {
				h.handleBotMessage(m)
			}

		case data := <-m.messageSentChan:
			if message, ok := data.(socket.RawMessageReceived); ok {
				h.handleMessgeSent(message)
			}

		case data := <-m.createChan:
			if message, ok := data.(socket.RawMessageReceived); ok {
				h.handleCreateChannel(message)
			}
		case data := <-m.destroyChan:
			if message, ok := data.(socket.RawMessageReceived); ok {
				h.handleDestroyChannel(message)
			}
		case data := <-m.joinChan:
			if message, ok := data.(socket.RawMessageReceived); ok {
				h.handleJoinChannel(message)
			}
		case data := <-m.quitChan:
			if message, ok := data.(socket.RawMessageReceived); ok {
				h.handleQuitChannel(message)
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
	}
}
