package socket

import (
	"encoding/binary"

	"github.com/google/uuid"
)

// SerializableMessage Represents a serializable message sent over socket
type SerializableMessage struct {
	messageType int
	data        interface{}
}

// RawMessage represents a message that will not be serialized and be sent raw
type RawMessage struct {
	messageType byte
	length      uint16
	bytes       []byte
}

//RawMessageReceived represents a message that was received. It is associated with a client id
type RawMessageReceived struct {
	message  RawMessage
	socketid uuid.UUID
}

// ToBytesSlice converts the raw message into the TLV format
func (message *RawMessage) ToBytesSlice() []byte {
	bytes := make([]byte, message.length+3)
	bytes[0] = message.messageType
	binary.BigEndian.PutUint16(bytes[1:], message.length)
	copy(bytes[3:], message.bytes)

	return bytes
}

// ParseMessage create the message object
func (message *RawMessage) ParseMessage(typeMessage byte, length uint16, bytes []byte) {
	message.messageType = typeMessage
	message.length = length
	message.bytes = bytes[3:]
}
