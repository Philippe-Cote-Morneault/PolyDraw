package socket

import (
	"github.com/google/uuid"
	"net"
)

type ClientSocket struct {
	socket net.Conn
	id     uuid.UUID
}
