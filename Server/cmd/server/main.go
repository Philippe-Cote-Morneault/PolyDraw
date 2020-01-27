package main

import (
	"log"

	"gitlab.com/jigsawcorp/log3900/internal/rest"
	service "gitlab.com/jigsawcorp/log3900/internal/services"
	"gitlab.com/jigsawcorp/log3900/internal/services/logger"
	"gitlab.com/jigsawcorp/log3900/internal/services/messenger"
	"gitlab.com/jigsawcorp/log3900/internal/services/router"
	"gitlab.com/jigsawcorp/log3900/internal/socket"
	"gitlab.com/jigsawcorp/log3900/pkg/graceful"
)

func main() {
	restServer := &rest.Server{}
	socketServer := &socket.Server{}
	socket.RegisterBroadcast()

	graceful.Register(restServer.Shutdown, "REST server")
	graceful.Register(socketServer.Shutdown, "Socket server")
	graceful.Register(service.ShutdownAll, "Services") //Shutdown all services

	graceful.ListenSIG()

	registerServices()

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

	service.StartAll()

	<-handleRest
	<-handleSocket
}

func registerServices() {
	service.Add(&logger.Logger{})
	service.Add(&router.Router{})
	service.Add(&messenger.Messenger{})
}
