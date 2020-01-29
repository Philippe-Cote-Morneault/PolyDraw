package messenger

import (
	"log"
	"time"

	"github.com/google/uuid"
	"gitlab.com/jigsawcorp/log3900/internal/services/auth"
	"gitlab.com/jigsawcorp/log3900/internal/socket"
)

type handler struct {
	connections map[uuid.UUID]bool
}

func (h *handler) init() {
	h.connections = make(map[uuid.UUID]bool)
}

func (h *handler) handleMessgeSent(message socket.RawMessageReceived) {
	var messageParsed MessageSent
	timestamp := int(time.Now().Unix())
	if message.Payload.DecodeMessagePack(&messageParsed) == nil {
		//Send to all other connected users
		user, err := auth.GetUserID(message.SocketID)
		if err != nil {
			log.Printf("[Messenger] -> %s", err)
		}
		log.Printf("[Messenger] -> Received: \"%s\" Username: \"%s\" ChannelID: %s", messageParsed.Message, user.Username, messageParsed.ChannelID)
		messageToFoward := MessageReceived{
			ChannelID:  messageParsed.ChannelID,
			SenderID:   user.ID.String(),
			SenderName: user.Username,
			Message:    messageParsed.Message,
			Timestamp:  timestamp,
		}
		rawMessage := socket.RawMessage{}
		if rawMessage.ParseMessagePack(byte(socket.MessageType.MessageReceived), messageToFoward) != nil {
			log.Printf("[Messenger] -> Can't pack message. Dropping packet!")
			return
		}
		for k := range h.connections {
			// Send message to the socket in async way
			go socket.SendRawMessageToSocketID(rawMessage, k)
		}
	} else {
		log.Printf("[Messenger] -> Wrong data format. Dropping packet!")
	}
}

func (h *handler) handleConnect(socketID uuid.UUID) {
	h.connections[socketID] = true
}

func (h *handler) handleDisconnect(socketID uuid.UUID) {
	delete(h.connections, socketID)
}
