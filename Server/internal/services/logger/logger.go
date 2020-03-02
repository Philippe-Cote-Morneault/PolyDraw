package logger

import (
	"log"

	service "gitlab.com/jigsawcorp/log3900/internal/services"
	"gitlab.com/jigsawcorp/log3900/internal/socket"
	"gitlab.com/jigsawcorp/log3900/pkg/cbroadcast"
)

//Logger service used to debug the message received by the server
type Logger struct {
	ready     cbroadcast.Channel
	receiver  cbroadcast.Channel
	connected cbroadcast.Channel
	close     cbroadcast.Channel

	shutdown chan bool
}

//Init the messenger service
func (l *Logger) Init() {
	l.shutdown = make(chan bool)
	l.subscribe()
}

//Start the messenger service
func (l *Logger) Start() {
	log.Println("[Logger] -> Starting service")
	go l.listen()
}

//Shutdown the messenger service
func (l *Logger) Shutdown() {
	log.Println("[Logger] -> Closing service")
	close(l.shutdown)
}

//Register register any broadcast not used
func (l *Logger) Register() {

}

func (l *Logger) listen() {
	defer service.Closed()

	//Message viewer
	for {
		select {
		case <-l.ready:
			log.Println("[Logger] -> Socket is ready")
		case data := <-l.receiver:
			message, ok := data.(socket.RawMessageReceived)

			if ok {
				log.Printf("[Logger] -> Socket data received: %s | %d | %d | %#x", message.SocketID, message.Payload.MessageType, message.Payload.Length, message.Payload.Bytes)
			}
		case id := <-l.connected:
			log.Printf("[Logger] -> connected id: %s", id)
		case id := <-l.close:
			log.Printf("[Logger] -> disconnect id: %s", id)
		case <-l.shutdown:
			return
		}
	}

}

func (l *Logger) subscribe() {
	l.ready, _, _ = cbroadcast.Subscribe(socket.BSocketReady)
	l.receiver, _, _ = cbroadcast.Subscribe(socket.BSocketReceive)
	l.connected, _, _ = cbroadcast.Subscribe(socket.BSocketConnected)
	l.close, _, _ = cbroadcast.Subscribe(socket.BSocketCloseClient)
}
