package socket

import (
	"fmt"
	"net"
)

type Server struct {
	listener *net.Listener
	clientSocketManager *ClientSocketManager
}
// Starts listening to incoming socket connections
func StartListening(host string) {
	ln, err := net.Listen("tcp", host)
	if err != nil {
		// handle error
		fmt.Println(err)
	}
	for {
		conn, err := ln.Accept()
		if err != nil {
			// handle error
			fmt.Println(err)
		}
		fmt.Println(conn.RemoteAddr())
	}
}


