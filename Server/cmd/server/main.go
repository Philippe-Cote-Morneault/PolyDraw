package main

import (	
	"gitlab.com/jigsawcorp/log3900/internal/rest"
)


func main() {
	restServer := &rest.RestServer{}
	restServer.Initialize()
	restServer.Run(":3000")
}