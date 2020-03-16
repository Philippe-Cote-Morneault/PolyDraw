package messenger

import (
	"log"
	"strings"
	"time"

	"github.com/google/uuid"
	"gitlab.com/jigsawcorp/log3900/internal/services/auth"
	"gitlab.com/jigsawcorp/log3900/internal/socket"
	"gitlab.com/jigsawcorp/log3900/model"
)

type handler struct {
	channelsConnections map[uuid.UUID]map[uuid.UUID]bool //channelID - socketID
}

func (h *handler) createGroupChannel(group *model.Group, connections []uuid.UUID) uuid.UUID {
	channel := model.ChatChannel{
		Name:       group.Name,
		IsGameChat: true,
	}
	model.DB().Create(&channel)

	//Init the hashmap for the connections
	h.channelsConnections[channel.ID] = make(map[uuid.UUID]bool)

	//Create request
	response := ChannelCreateResponse{
		ChannelName: channel.Name,
		ChannelID:   channel.ID.String(),
		Username:    "",
		UserID:      uuid.Nil.String(),
		Timestamp:   time.Now().Unix(),
	}
	rawMessage := socket.RawMessage{}
	if rawMessage.ParseMessagePack(byte(socket.MessageType.UserCreateChannel), response) != nil {
		log.Printf("[Messenger] -> Create: Can't pack message. Dropping packet!")
		return channel.ID
	}

	for i := range connections {
		//Check if the user has a session
		go socket.SendRawMessageToSocketID(rawMessage, connections[i])
	}
	log.Printf("[Messenger] -> Create: channel %s created", channel.Name)

	return channel.ID
}

func (h *handler) deleteGroupChannel(group *model.Group) {
	var channel model.ChatChannel
	model.DB().Where("name = ?", group.Name).First(&channel)

	//Create a destroy message
	destroyMessage := ChannelDestroyResponse{
		UserID:    uuid.Nil.String(),
		Username:  "",
		ChannelID: channel.ID.String(),
		Timestamp: time.Now().Unix(),
	}
	rawMessage := socket.RawMessage{}
	if rawMessage.ParseMessagePack(byte(socket.MessageType.UserDestroyedChannel), destroyMessage) != nil {
		log.Printf("[Messenger] -> Destroy: Can't pack message. Dropping packet!")
		return
	}

	for socketID := range h.channelsConnections[channel.ID] {
		go socket.SendRawMessageToSocketID(rawMessage, socketID)
	}

	delete(h.channelsConnections, channel.ID)
	model.DB().Delete(&channel)

	return
}

func (h *handler) quitChannel(socketID uuid.UUID, channelID uuid.UUID) {
	//Check if channel exists
	channel := model.ChatChannel{}
	model.DB().Model(&channel).Related(&model.User{}, "Users")
	model.DB().Preload("Users").Where("id = ?", channelID).First(&channel)
	if channel.ID != uuid.Nil {
		user, _ := auth.GetUser(socketID)
		if _, ok := h.channelsConnections[channel.ID][socketID]; ok {
			model.DB().Model(&channel).Association("Users").Delete(user)

			//Create a quit message
			quitResponse := ChannelLeaveResponse{
				UserID:    user.ID.String(),
				Username:  user.Username,
				ChannelID: channel.ID.String(),
				Timestamp: time.Now().Unix(),
			}
			rawMessage := socket.RawMessage{}
			if rawMessage.ParseMessagePack(byte(socket.MessageType.UserLeftChannel), quitResponse) != nil {
				log.Printf("[Messenger] -> Quit: Can't pack message. Dropping packet!")
				return
			}

			for socketID := range h.channelsConnections[channel.ID] {
				go socket.SendRawMessageToSocketID(rawMessage, socketID)
			}
			delete(h.channelsConnections[channelID], socketID)
			log.Printf("[Messenger] -> Quit: User %s quit %s", user.ID.String(), channelID)
		} else {
			log.Printf("[Messenger] -> Quit: User is not in the channel")
			socket.SendErrorToSocketID(socket.MessageType.LeaveChannel, 400, "User is not in the channel.", socketID)
		}
	} else {
		log.Printf("[Messenger] -> Quit: Invalid channel UUID, not found")
		socket.SendErrorToSocketID(socket.MessageType.LeaveChannel, 404, "Invalid channel UUID, not found.", socketID)
	}
}

func (h *handler) joinChannel(socketID uuid.UUID, channelID uuid.UUID) {
	channel := model.ChatChannel{}
	model.DB().Model(&channel).Related(&model.User{}, "Users")
	model.DB().Preload("Users").Where("id = ?", channelID).First(&channel)

	if channel.ID != uuid.Nil {
		user, _ := auth.GetUser(socketID)
		if _, ok := h.channelsConnections[channel.ID][socketID]; !ok {
			joinServer := ChannelJoin{
				UserID:    user.ID.String(),
				Username:  user.Username,
				ChannelID: channel.ID.String(),
				Timestamp: time.Now().Unix(),
			}

			rawMessage := socket.RawMessage{}
			if rawMessage.ParseMessagePack(byte(socket.MessageType.UserJoinedChannel), joinServer) != nil {
				log.Printf("[Messenger] -> Join: Can't pack message. Dropping packet!")
				return
			}

			//We can join the channel
			model.DB().Model(&channel).Association("Users").Append(user)
			h.channelsConnections[channel.ID][socketID] = true

			for socketID := range h.channelsConnections[channel.ID] {
				go socket.SendRawMessageToSocketID(rawMessage, socketID)
			}
			log.Printf("[Messenger] -> Join: User %s join %s", user.ID.String(), channelID)
		} else {
			log.Printf("[Messenger] -> Join: User is already joined to the channel")
			socket.SendErrorToSocketID(socket.MessageType.JoinChannel, 409, "User is already joined to the channel.", socketID)
		}
	} else {
		log.Printf("[Messenger] -> Join: Channel UUID not found, %s", channelID.String())
		socket.SendErrorToSocketID(socket.MessageType.JoinChannel, 404, "Channel UUID not found.", socketID)
	}
}

func (h *handler) init() {
	h.channelsConnections = map[uuid.UUID]map[uuid.UUID]bool{}
	h.channelsConnections[uuid.Nil] = make(map[uuid.UUID]bool)

	var channels []model.ChatChannel
	model.DB().Find(&channels)

	for i := range channels {
		h.channelsConnections[channels[i].ID] = make(map[uuid.UUID]bool)
	}
}
func (h *handler) handleMessgeSent(message socket.RawMessageReceived) {
	var messageParsed MessageSent
	timestamp := time.Now().Unix()
	if message.Payload.DecodeMessagePack(&messageParsed) == nil {
		//Send to all other connected users
		user, err := auth.GetUser(message.SocketID)
		if err != nil {
			log.Printf("[Messenger] -> %s", err)
		}
		channelID, err := uuid.Parse(messageParsed.ChannelID)
		if err == nil {
			if _, ok := h.channelsConnections[channelID][message.SocketID]; ok {
				messageToFoward := MessageReceived{
					ChannelID: messageParsed.ChannelID,
					UserID:    user.ID.String(),
					Username:  user.Username,
					Message:   messageParsed.Message,
					Timestamp: timestamp,
				}
				rawMessage := socket.RawMessage{}
				if rawMessage.ParseMessagePack(byte(socket.MessageType.MessageReceived), messageToFoward) != nil {
					log.Printf("[Messenger] -> Receive: Can't pack message. Dropping packet!")
					return
				}
				for k := range h.channelsConnections[channelID] {
					// Send message to the socket in async way
					go socket.SendRawMessageToSocketID(rawMessage, k)
				}
				log.Printf("[Messenger] -> Receive: \"%s\" Username: \"%s\" ChannelID: %s", messageParsed.Message, user.Username, messageParsed.ChannelID)
				model.AddMessage(messageParsed.Message, channelID, user.ID, timestamp)
			} else {
				log.Printf("[Messenger] -> Receive: The user needs to join the channel first. Dropping packet!")
				socket.SendErrorToSocketID(socket.MessageType.MessageSent, 409, "The user needs to join the channel first.", message.SocketID)
			}
		} else {
			log.Printf("[Messenger] -> Receive: Invalid channel ID. Dropping packet!")
			socket.SendErrorToSocketID(socket.MessageType.MessageSent, 404, "Invalid channel ID", message.SocketID)
		}
	} else {
		log.Printf("[Messenger] -> Receive: Wrong data format. Dropping packet!")
		socket.SendErrorToSocketID(socket.MessageType.MessageSent, 404, "Wrong data format.", message.SocketID)
	}
}

func (h *handler) handleCreateChannel(message socket.RawMessageReceived) {
	var channelParsed ChannelCreateRequest
	timestamp := time.Now().Unix()
	if message.Payload.DecodeMessagePack(&channelParsed) == nil {
		name := channelParsed.ChannelName
		if strings.TrimSpace(name) != "" && name != "General" {
			user, err := auth.GetUser(message.SocketID)
			if err == nil {
				//Check if channel already exists
				var count int64
				model.DB().Model(&model.ChatChannel{}).Where("name = ?", name).Count(&count)
				if count == 0 {
					channel := model.ChatChannel{
						Name: name,
					}
					model.DB().Create(&channel)

					//Init the hashmap for the connections
					h.channelsConnections[channel.ID] = make(map[uuid.UUID]bool)

					//Create request
					response := ChannelCreateResponse{
						ChannelName: name,
						ChannelID:   channel.ID.String(),
						Username:    user.Username,
						UserID:      user.ID.String(),
						Timestamp:   timestamp,
					}
					rawMessage := socket.RawMessage{}
					if rawMessage.ParseMessagePack(byte(socket.MessageType.UserCreateChannel), response) != nil {
						log.Printf("[Messenger] -> Create: Can't pack message. Dropping packet!")
						return
					}

					for socketID := range h.channelsConnections[uuid.Nil] {
						//Check if the user has a session
						go socket.SendRawMessageToSocketID(rawMessage, socketID)
					}
					log.Printf("[Messenger] -> Create: channel %s created", channelParsed.ChannelName)
				} else {
					log.Printf("[Messenger] -> Create: Channel already exists. Dropping packet!")
					socket.SendErrorToSocketID(socket.MessageType.CreateChannel, 409, "Channel already exists.", message.SocketID)
				}
			} else {
				log.Printf("[Messenger] -> Create: Can't find user. Dropping packet!")
				socket.SendErrorToSocketID(socket.MessageType.CreateChannel, 404, "Can't find user.", message.SocketID)
			}
		} else {
			log.Printf("[Messenger] -> Create: Invalid channel name. Dropping packet!")
			socket.SendErrorToSocketID(socket.MessageType.CreateChannel, 400, "Invalid channel UUID.", message.SocketID)
		}
	} else {
		log.Printf("[Messenger] -> Create: Invalid channel decoding. Dropping packet!")
		socket.SendErrorToSocketID(socket.MessageType.CreateChannel, 400, "Invalid channel decoding.", message.SocketID)
	}
}

func (h *handler) handleJoinChannel(message socket.RawMessageReceived) {
	if message.Payload.Length == 16 {
		channelID, err := uuid.FromBytes(message.Payload.Bytes)
		if err == nil {
			h.joinChannel(message.SocketID, channelID)
		} else {
			log.Printf("[Messenger] -> Join: Invalid channel UUID")
			socket.SendErrorToSocketID(socket.MessageType.JoinChannel, 400, "Invalid channel UUID.", message.SocketID)
		}
	} else {
		log.Printf("[Messenger] -> Join: Invalid channel UUID")
		socket.SendErrorToSocketID(socket.MessageType.JoinChannel, 400, "Invalid channel UUID.", message.SocketID)
	}
}

func (h *handler) handleQuitChannel(message socket.RawMessageReceived) {
	if message.Payload.Length == 16 {
		channelID, err := uuid.FromBytes(message.Payload.Bytes)
		if err == nil {
			h.quitChannel(message.SocketID, channelID)
		} else {
			log.Printf("[Messenger] -> Quit: Invalid channel UUID")
			socket.SendErrorToSocketID(socket.MessageType.LeaveChannel, 400, "Invalid channel UUID.", message.SocketID)
		}
	} else {
		log.Printf("[Messenger] -> Quit: Invalid channel UUID")
		socket.SendErrorToSocketID(socket.MessageType.LeaveChannel, 400, "Invalid channel UUID.", message.SocketID)
	}
}

func (h *handler) handleDestroyChannel(message socket.RawMessageReceived) {
	if message.Payload.Length == 16 {
		channelID, err := uuid.FromBytes(message.Payload.Bytes)
		if err == nil {
			//Check if channel exists
			channel := model.ChatChannel{}
			model.DB().Model(&channel).Related(&model.User{}, "Users")
			model.DB().Preload("Users").Where("id = ?", channelID).First(&channel)

			if channel.ID != uuid.Nil {
				user, _ := auth.GetUser(message.SocketID)
				delete(h.channelsConnections, channel.ID)
				model.DB().Model(&channel).Delete(&channel)

				//Create a destroy message
				destroyMessage := ChannelDestroyResponse{
					UserID:    user.ID.String(),
					Username:  user.Username,
					ChannelID: channel.ID.String(),
					Timestamp: time.Now().Unix(),
				}
				rawMessage := socket.RawMessage{}
				if rawMessage.ParseMessagePack(byte(socket.MessageType.UserDestroyedChannel), destroyMessage) != nil {
					log.Printf("[Messenger] -> Destroy: Can't pack message. Dropping packet!")
					return
				}

				for socketID := range h.channelsConnections[uuid.Nil] {
					go socket.SendRawMessageToSocketID(rawMessage, socketID)
				}
				log.Printf("[Messenger] -> Destroy: Removed channel %s", channelID)
			} else {
				log.Printf("[Messenger] -> Destroy: Invalid channel UUID, not found")
				socket.SendErrorToSocketID(socket.MessageType.DestroyChannel, 404, "Invalid channel UUID, not found.", message.SocketID)
			}
		} else {
			log.Printf("[Messenger] -> Destroy: Invalid channel UUID")
			socket.SendErrorToSocketID(socket.MessageType.DestroyChannel, 400, "Invalid channel UUID", message.SocketID)
		}
	} else {
		log.Printf("[Messenger] -> Destroy: Invalid channel UUID")
		socket.SendErrorToSocketID(socket.MessageType.DestroyChannel, 400, "Invalid channel UUID", message.SocketID)
	}
}

func (h *handler) handleConnect(socketID uuid.UUID) {
	h.channelsConnections[uuid.Nil][socketID] = true

	user, _ := auth.GetUser(socketID)

	var channels []model.ChatChannel

	model.DB().Joins("left join chat_channel_membership on chat_channel_membership.chat_channel_id = chat_channels.id").Where("chat_channel_membership.user_id = ?", user.ID).Find(&channels)
	joinServer := ChannelJoin{
		UserID:    user.ID.String(),
		Username:  user.Username,
		ChannelID: uuid.Nil.String(),
		Timestamp: time.Now().Unix(),
	}

	rawMessage := socket.RawMessage{}
	if rawMessage.ParseMessagePack(byte(socket.MessageType.UserJoinedChannel), joinServer) != nil {
		log.Printf("[Messenger] -> Connect: Can't pack message. Dropping packet!")
		return
	}
	for connectionSocketID := range h.channelsConnections[uuid.Nil] {
		go socket.SendRawMessageToSocketID(rawMessage, connectionSocketID)
	}

	//Update the cache
	for _, channel := range channels {
		h.channelsConnections[channel.ID][socketID] = true
	}
}

func (h *handler) handleDisconnect(socketID uuid.UUID) {
	delete(h.channelsConnections[uuid.Nil], socketID)
	user, _ := auth.GetUser(socketID)

	var channels []model.ChatChannel
	model.DB().Joins("left join chat_channel_membership on chat_channel_membership.chat_channel_id = chat_channels.id").Where("chat_channel_membership.user_id = ?", user.ID).Find(&channels)

	leaveServer := ChannelJoin{
		UserID:    user.ID.String(),
		Username:  user.Username,
		ChannelID: uuid.Nil.String(),
		Timestamp: time.Now().Unix(),
	}

	rawMessage := socket.RawMessage{}
	if rawMessage.ParseMessagePack(byte(socket.MessageType.UserLeftChannel), leaveServer) != nil {
		log.Printf("[Messenger] -> Disconnect: Can't pack message. Dropping packet!")
		return
	}
	for connectionSocketID := range h.channelsConnections[uuid.Nil] {
		go socket.SendRawMessageToSocketID(rawMessage, connectionSocketID)
	}

	//Update the cache
	for _, channel := range channels {
		delete(h.channelsConnections[channel.ID], socketID)
	}
}
