package socket

import (
	"fmt"
	"net"
)

// Starts listening to incoming socket connections
func StartListening() {
	ln, err := net.Listen("tcp", ":8080")
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


