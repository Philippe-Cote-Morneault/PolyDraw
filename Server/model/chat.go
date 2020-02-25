package model

import "github.com/google/uuid"

//ChatChannel represents a chat channel in the database
type ChatChannel struct {
	Base
	Name  string
	Users []*User `gorm:"many2many:chat_channel_membership"`
}

//ChatMessage represents a message from a client to the channel.
type ChatMessage struct {
	Base
	Channel   ChatChannel `gorm:"foreignkey:ChannelID"`
	ChannelID uuid.UUID
	User      User `gorm:"foreignkey:UserID"`
	UserID    uuid.UUID
	Message   string
	Timestamp int64
}

//AddMessage to a user and channel
func AddMessage(message string, channelID uuid.UUID, userID uuid.UUID, timestamp int64) {
	DB().Create(&ChatMessage{
		ChannelID: channelID,
		UserID:    userID,
		Message:   message,
		Timestamp: timestamp,
	})
}
