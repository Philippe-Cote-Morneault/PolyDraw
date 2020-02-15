package model

import (
	"time"

	"github.com/google/uuid"
)

//Stats contains the stats of an User
type Stats struct {
	Base
	User            User `gorm:"foreignkey:UserID"`
	UserID          uuid.UUID
	GamesPlayed     int64   `gorm:"default:0"`
	WinRatio        float64 `gorm:"default:0.0"`
	AvgGameDuration int64   `gorm:"default:0"`
	TimePlayed      int64   `gorm:"default:0"`
}

//Connection represents the information of a connection
type Connection struct {
	Base
	User           User `gorm:"foreignkey:UserID"`
	UserID         uuid.UUID
	ConnectedAt    int64
	DisconnectedAt int64
}

//MatchPlayed represents the summary of the game
type MatchPlayed struct {
	Base
	User          User `gorm:"foreignkey:UserID"`
	UserID        uuid.UUID
	MatchDuration int64
	WinnerName    string
	MatchType     string
}

//PlayerName represents the name of player in MatchPlayed
type PlayerName struct {
	Base
	MatchPlayed MatchPlayed `gorm:"foreignkey:MatchID"`
	MatchID     uuid.UUID
	PlayerName  string
}

// Achievement represent an achievement that can be obtained by a player
type Achievement struct {
	Base
	User          User `gorm:"foreignkey:UserID"`
	UserID        uuid.UUID
	TropheeName   string
	Description   string
	ObtainingDate int64
}

// UpdateStats updates user's stats each time a match/game is finished
func UpdateStats(userID uuid.UUID, matchPlayed *MatchPlayed) {
	var stats Stats
	DB().Where("userID = ?", userID).First(&stats)

	var gamesPlayed int64 = stats.GamesPlayed + 1
	var timePlayed int64 = stats.TimePlayed + matchPlayed.MatchDuration
	var winRatio float64 = stats.WinRatio

	var user User
	DB().Model(&stats).Related(&user)

	if matchPlayed.WinnerName == user.Username {
		winRatio = (stats.WinRatio*float64(gamesPlayed) + 1) / float64(gamesPlayed)
	}

	DB().Where("userID = ?", userID).Updates(map[string]interface{}{
		"gamesPlayed":     gamesPlayed,
		"winRatio":        winRatio,
		"timePlayed":      timePlayed,
		"avgGameDuration": float64(timePlayed) / float64(gamesPlayed),
	})
}

// UpdateDeconnection sets the deconnection time of user
func UpdateDeconnection(userID uuid.UUID) {
	var c Connection
	DB().Model(&Connection{}).Where("user_id = ?", userID).Order("created_at desc").Offset(0).Limit(1).Find(&c)
	DB().Model(&Connection{}).Where("id = ?", c.ID).Update("disconnected_at", time.Now().Unix())

}
