package main

import (
	"log"

	"gitlab.com/jigsawcorp/log3900/internal/rest"
	"gitlab.com/jigsawcorp/log3900/pkg/graceful"
)

var restServer *rest.Server

func main() {
	graceful.CatchSigterm(onSIGTERM)

	restServer = &rest.Server{}

	hRestServer := make(chan bool)
	go func() {
		restServer.Initialize()
		restServer.Run(":3000")
		hRestServer <- true
	}()

	log.Printf("Server is starting jobs!")

	//TODO Launch other servers and handles

	<-hRestServer
}

// Call this function on crtl+c
// use safe shutdown any process
func onSIGTERM() {
	restServer.Shutdown()
}
