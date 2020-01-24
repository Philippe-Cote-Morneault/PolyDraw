package socket

import (
	"encoding/binary"
	"time"
)

// SerializableMessage Represents a serializable message sent over socket
type SerializableMessage struct {
	Type int
	Data interface{}
	Time time.Time
}

// Quick implementation of enum to represent different socket events.
type socketEvent struct {
	Connection    int
	Disconnection int
}

// Quick implementation of enum to represent different message types
type messageType struct {
	ServerConnection    int
	ServerDisconnection int
	UserDisconnected    int
	MessageSent         int
	MessageReceived     int
	JoinChannel         int
	UserJoinedChannel   int
	LeaveChannel        int
	UserLeftChannel     int
	CreateChannel       int
}

// MessageType represents the available message types to send to clients.
var MessageType = &messageType{
	ServerConnection:    0,
	ServerDisconnection: 1,
	UserDisconnected:    2,
	MessageSent:         20,
	MessageReceived:     21,
	JoinChannel:         22,
	UserJoinedChannel:   23,
	LeaveChannel:        24,
	UserLeftChannel:     25,
	CreateChannel:       26,
}

// SocketEvent represents an event that can occur with sockets.
var SocketEvent = &socketEvent{
	Connection:    0,
	Disconnection: 1,
}

// RawMessage represents a message that will not be serialized and be sent raw
type RawMessage struct {
	Type   byte
	Length uint16
	Bytes  []byte
}

// ToBytesSlice converts the raw message into the TLV format
func (message *RawMessage) ToBytesSlice() []byte {
	bytes := make([]byte, message.Length+3)
	bytes[0] = message.Type
	binary.BigEndian.PutUint16(bytes[1:], message.Length)
	copy(bytes[3:], message.Bytes)

	return bytes
}

// ParseMessage create the message object
func (message *RawMessage) ParseMessage(typeMessage byte, length uint16, bytes []byte) {
	message.Type = typeMessage
	message.Length = length
	message.Bytes = bytes[3:]
}
