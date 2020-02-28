package drawing

import (
	"log"

	service "gitlab.com/jigsawcorp/log3900/internal/services"
	"gitlab.com/jigsawcorp/log3900/internal/socket"
	"gitlab.com/jigsawcorp/log3900/pkg/cbroadcast"
)

//Drawing service used to route the packets and send the preview
type Drawing struct {
	connected cbroadcast.Channel
	close     cbroadcast.Channel

	shutdown chan bool
}

//Init the drawing service
func (d *Drawing) Init() {
	d.shutdown = make(chan bool)
	d.subscribe()
}

//Start the drawing service
func (d *Drawing) Start() {
	log.Println("[Drawing] -> Starting service")
	go d.listen()
}

//Shutdown the drawing service
func (d *Drawing) Shutdown() {
	log.Println("[Drawing] -> Closing service")
	close(d.shutdown)
}

//Register register any broadcast not used
func (d *Drawing) Register() {

}

func (d *Drawing) listen() {
	defer service.Closed()

	for {
		select {
		case id := <-d.connected:
			log.Printf("[Drawing] -> connected id: %s", id)
		case id := <-d.close:
			log.Printf("[Drawing] -> disconnect id: %s", id)
		case <-d.shutdown:
			return
		}
	}

}

func (d *Drawing) subscribe() {
	d.connected, _, _ = cbroadcast.Subscribe(socket.BSocketConnected)
	d.close, _, _ = cbroadcast.Subscribe(socket.BSocketCloseClient)
}
