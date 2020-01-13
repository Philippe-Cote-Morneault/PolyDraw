package graceful

import (
	"log"
	"os"
	"os/signal"
	"syscall"
)

type subscriberStruct struct {
	f    func()
	name string
}

var subscriber []subscriberStruct

// Register a function to be called when sigterm is raised
func Register(f func(), name string) {
	subscriber = append(subscriber, subscriberStruct{
		f:    f,
		name: name})
}

// ListenSIG register a thread to listen to a SIGTERM signal
func ListenSIG() {
	c := make(chan os.Signal)
	signal.Notify(c, os.Interrupt, syscall.SIGTERM)
	go func() {
		<-c
		onSIGTERM()
		os.Exit(1)
	}()
}

func onSIGTERM() {
	for _, sub := range subscriber {
		log.Printf("Stopping %s", sub.name)
		sub.f()
	}
}
