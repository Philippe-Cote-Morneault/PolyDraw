package main

import (
	"log"

	"gitlab.com/jigsawcorp/log3900/internal/rest"
	"gitlab.com/jigsawcorp/log3900/internal/socket"
	"gitlab.com/jigsawcorp/log3900/pkg/cbroadcast"
	"gitlab.com/jigsawcorp/log3900/pkg/graceful"
)

func main() {
	restServer := &rest.Server{}
	socketServer := &socket.Server{}
	socket.RegisterBroadcast()

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

	//Message viewer
	go func() {
		ready, _, _ := cbroadcast.Subscribe(socket.BSocketReady)
		receiver, _, _ := cbroadcast.Subscribe(socket.BSocketReceive)
		connected, _, _ := cbroadcast.Subscribe(socket.BSocketConnected)
		close, _, _ := cbroadcast.Subscribe(socket.BSocketCloseClient)

		for {
			select {
			case <-ready:
				log.Println("Socket is ready")
			case data := <-receiver:
				message, ok := data.(socket.RawMessage)

				if ok {
					log.Printf("Socket data received: %#x | %d | %#x -> %s", message.Type, message.Length, message.Bytes, message.Bytes)
				}

				//fmt.Printf("data: %s | %#x\n", data, data)
			case id := <-connected:
				log.Printf("connected id: %s", id)
			case id := <-close:
				log.Printf("disconnect id: %s", id)
			}
		}

	}()

	<-handleRest
	<-handleSocket
}
