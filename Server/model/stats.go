package model

import (
	"github.com/google/uuid"
)

//PlayerStats represents the statistics of the player
type PlayerStats struct {
	Base               `gorm:"foreignkey:UserID"`
	UserID             uuid.UUID
	GamesPlayed        int64
	WinRatio           float64
	AvgGameDuration    int64
	TimePlayed         int64         `gorm:"foreignkey:ConnectionHistory"`
	ConnectionHistory  []Connection  `gorm:"foreignkey:GamesPlayedHistory"`
	GamesPlayedHistory []MatchPlayed `gorm:"foreignkey:Achievements"`
	Achievements       []Achievement
}

//Connection represents the information of a connection
type Connection struct {
	Base
	ConnectedAt   int64
	DeconnectedAt int64
}

//MatchPlayed represents the summary of the game
type MatchPlayed struct {
	Base
	PlayerNames   []string // a voir si on laisse le tableau de nom ou si on met un tableau de joueur (moins couteux string)
	DatePlayed    int64
	MatchDuration int64
	WinnerName    string
}

// Achievement represent an achievement that can be obtained by a player
type Achievement struct {
	Base
	TropheeName   string
	Description   string
	ObtainingDate int64
}
