package main

import (
	"gitlab.com/jigsawcorp/log3900/internal/rest"
	"gitlab.com/jigsawcorp/log3900/pkg/graceful"
)

var restServer *rest.RestServer
func main() {
	graceful.CatchSigterm(onSIGTERM)

	restServer = &rest.RestServer{}
	restServer.Initialize()
	restServer.Run(":3000")
}

// Call this function on crtl+c
// use safe shutdown any process
func onSIGTERM() {
	restServer.Shutdown()
}