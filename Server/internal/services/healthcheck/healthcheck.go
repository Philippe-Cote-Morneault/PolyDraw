package healthcheck

import (
	"log"

	service "gitlab.com/jigsawcorp/log3900/internal/services"
)

//HealthCheck service used to debug the message received by the server
type HealthCheck struct {
	shutdown chan bool
}

//Init the messenger service
func (h *HealthCheck) Init() {
	h.shutdown = make(chan bool)
	h.subscribe()
}

//Start the messenger service
func (h *HealthCheck) Start() {
	log.Println("[Healthcheck] -> Starting service")
	go h.listen()
}

//Shutdown the messenger service
func (h *HealthCheck) Shutdown() {
	log.Println("[Healthcheck] -> Closing service")
	close(h.shutdown)
}

//Register register any broadcast not used
func (h *HealthCheck) Register() {

}

func (h *HealthCheck) listen() {
	defer service.Closed()

	for {
		select {
		case <-h.shutdown:
			return
		}
	}

}

func (h *HealthCheck) subscribe() {
}
