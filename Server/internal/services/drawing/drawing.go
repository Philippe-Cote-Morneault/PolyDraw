package drawing

import (
	"log"

	service "gitlab.com/jigsawcorp/log3900/internal/services"
	"gitlab.com/jigsawcorp/log3900/internal/socket"
	"gitlab.com/jigsawcorp/log3900/pkg/cbroadcast"
)

//Drawing service used to route the packets and send the preview
type Drawing struct {
	preview cbroadcast.Channel

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

func (d *Drawing) listen() {
	defer service.Closed()

	for {
		select {
		case data := <-d.preview:
			if message, ok := data.(socket.RawMessageReceived); ok {
				//Start a new function to handle the connection
				go d.handlePreview(message)
			}
		case <-d.shutdown:
			return
		}
	}

}

func (d *Drawing) subscribe() {
	d.preview, _, _ = cbroadcast.Subscribe(BPreview)
}
