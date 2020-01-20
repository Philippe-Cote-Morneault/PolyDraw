package main

import (
	"gitlab.com/jigsawcorp/log3900/internal/rest"
	"gitlab.com/jigsawcorp/log3900/internal/socket"
	"gitlab.com/jigsawcorp/log3900/pkg/graceful"
	"log"
)

func main() {
	restServer := &rest.Server{}
	socketServer := &socket.Server{}

	graceful.Register(restServer.Shutdown, "REST server")
	graceful.Register(socketServer.Shutdown, "Socket server")
	graceful.ListenSIG()

	log.Printf("Server is starting jobs!")

	handleRest := make(chan bool)
	go func() {
		restServer.Initialize()
		restServer.Run(":3000")
		handleRest <- true
	}()

	handleSocket := make(chan bool)
	go func() {
		socketServer.StartListening(":3001")
		handleSocket <- true
	}()

	<-handleRest
	<-handleSocket
}
