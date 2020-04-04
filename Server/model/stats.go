package model

import (
	"github.com/google/uuid"
)

//Connection represents the information of a connection
type Connection struct {
	Base
	UserID         uuid.UUID
	ConnectedAt    int64
	DisconnectedAt int64
}

//MatchPlayed represents the summary of the game
type MatchPlayed struct {
	Base
	MatchDuration int64
	WinnerName    string
	WinnerID      string
	MatchType     int // 0 -> FFA | 1 -> solo | 2 -> coop
}

//MatchPlayedMembership represents the membership between a match and user
type MatchPlayedMembership struct {
	MatchID uuid.UUID `gorm:"primary_key;auto_increment:false"`
	UserID  uuid.UUID `gorm:"primary_key;auto_increment:false"`
}
