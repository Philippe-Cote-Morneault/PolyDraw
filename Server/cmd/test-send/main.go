package main

import (
	"fmt"
	"net"
	"time"
)

func startclient(conn *net.TCPConn, data string) {
	fmt.Println("Staring")
	for {
		conn.Write([]byte(data))
		time.Sleep(time.Millisecond * 5)
	}
}

func main() {
	servAddr := "127.0.0.1:8888"
	tcpAddr, err := net.ResolveTCPAddr("tcp", servAddr)
	if err != nil {
		fmt.Println(err)
		return
	}
	conn, err := net.DialTCP("tcp", nil, tcpAddr)
	if err != nil {
		fmt.Println(err)
		return
	}
	go startclient(conn, "🍎🍎🍎🍎🍎🍎🍎🍎🍎🍎🍎🍎🍎🍎🍎🍎🍎")
	go startclient(conn, "🍏🍏🍏🍏🍏🍏🍏🍏🍏🍏🍏🍏🍏🍏🍏🍏🍏")

	for {
	}
}
