package potrace

import (
	"log"
	"syscall"

	service "gitlab.com/jigsawcorp/log3900/internal/services"
)

//Potrace service used to debug the message received by the server
type Potrace struct {
}

//Init the messenger service
func (p *Potrace) Init() {

}

//Start the potrace service
func (p *Potrace) Start() {
	log.Println("[Potrace] -> Starting service")
	hasError := false
	if !checkCommand("potrace") {
		hasError = true
		log.Println("[Potrace] -> Warning command potrace cannot be found aborting service!")
	}

	if !checkCommand("convert") {
		hasError = true
		log.Println("[Potrace] -> Warning command convert (Imagemagick) cannot be found aborting service!")
	}

	if hasError {
		syscall.Kill(syscall.Getpid(), syscall.SIGTERM)
	}
}

//Shutdown the potrace service
func (p *Potrace) Shutdown() {
	log.Println("[Potrace] -> Closing service")
	service.Closed()
}

//Register register any broadcast not used
func (p *Potrace) Register() {

}
