package socket

import (
	"github.com/google/uuid"
	"net"
)

// ClientSocket is a client connected to the socket
type ClientSocket struct {
	socket net.Conn
	id     uuid.UUID
}
