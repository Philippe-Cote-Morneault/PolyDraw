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

	strokeChunk cbroadcast.Channel
	drawStart   cbroadcast.Channel
	drawEnd     cbroadcast.Channel
	drawErase   cbroadcast.Channel

	router Router

	shutdown chan bool
}

//Init the drawing service
func (d *Drawing) Init() {
	d.shutdown = make(chan bool)
	d.subscribe()

	d.router.Init()
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
		case data := <-d.strokeChunk:
			if message, ok := data.(socket.RawMessageReceived); ok {
				go d.router.Route(&message)
			}
		case data := <-d.drawStart:
			if message, ok := data.(socket.RawMessageReceived); ok {
				go d.router.Route(&message)
			}
		case data := <-d.drawEnd:
			if message, ok := data.(socket.RawMessageReceived); ok {
				go d.router.Route(&message)
			}
		case data := <-d.drawErase:
			if message, ok := data.(socket.RawMessageReceived); ok {
				go d.router.Route(&message)
			}
		case <-d.shutdown:
			return
		}
	}

}

func (d *Drawing) subscribe() {
	d.preview, _ = cbroadcast.Subscribe(BPreview)
	d.strokeChunk, _ = cbroadcast.Subscribe(BStrokeChunk)
	d.drawStart, _ = cbroadcast.Subscribe(BDrawStart)
	d.drawEnd, _ = cbroadcast.Subscribe(BDrawEnd)
	d.drawErase, _ = cbroadcast.Subscribe(BDrawErase)
}
