package model

import "github.com/google/uuid"

//ChatChannel represents a chat channel in the database
type ChatChannel struct {
	Base
	Name string
}

//ChatChannelSubscribers represents a membership of a client to a channel
type ChatChannelSubscribers struct {
	Base
	User      User `gorm:"foreignkey:UserID"`
	UserID    uuid.UUID
	Channel   ChatChannel `gorm:"foreignkey:ChannelID"`
	ChannelID uuid.UUID
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
