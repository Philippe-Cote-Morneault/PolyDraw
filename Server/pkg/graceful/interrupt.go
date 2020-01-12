package graceful

import (
	"os"
	"os/signal"
	"syscall"
)

// CatchSigterm Call a function when the signal SIGTERM is raised
func CatchSigterm(f func()) {
	c := make(chan os.Signal)
	signal.Notify(c, os.Interrupt, syscall.SIGTERM)
	go func() {
		<-c
		f()
		os.Exit(1)
	}()
}
