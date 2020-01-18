package socket

import (
	"net"
	"github.com/google/uuid"
)

type ClientSocket struct {
	socket net.Conn
	id uuid.UUID
}
