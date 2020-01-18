package socket

import (
	"encoding/binary"
)

type RawMessage struct {
	Type byte
	Length uint16
	Bytes []byte
}

// Converts the raw message into the TLV format
func (message *RawMessage) ToBytesSlice() []byte {
	bytes := make([]byte, message.Length + 3)
	bytes[0] = message.Type
	binary.BigEndian.PutUint16(bytes[1:], message.Length)
	copy(bytes[3:], message.Bytes)

	return bytes
}
