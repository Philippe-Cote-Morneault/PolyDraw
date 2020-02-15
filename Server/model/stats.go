package model

import (
	"time"

	"github.com/google/uuid"
)

//Connection represents the information of a connection
type Connection struct {
	Base
	UserID        uuid.UUID
	ConnectedAt   int64
	DeconnectedAt int64
}

//MatchPlayed represents the summary of the game
type MatchPlayed struct {
	Base
	UserID        uuid.UUID
	MatchDuration int64
	WinnerName    string
	//PlayerNames   pq.StringArray `gorm:"type:varchar(20)[]"` a voir si pertinents car complique en Gorm et couteux en operation
}

// Achievement represent an achievement that can be obtained by a player
type Achievement struct {
	Base
	UserID        uuid.UUID
	TropheeName   string
	Description   string
	ObtainingDate int64
}

// UpdateStats updates user's stats each time a match/game is finished
func UpdateStats(userID uuid.UUID, matchPlayed *MatchPlayed) {
	var user User
	DB().Where("userID = ?", userID).First(&user)

	var gamesPlayed int64 = user.GamesPlayed + 1
	var timePlayed int64 = user.TimePlayed + matchPlayed.MatchDuration

	var winRatio float64 = user.WinRatio
	if matchPlayed.WinnerName == user.Username {
		winRatio = (user.WinRatio*float64(gamesPlayed) + 1) / float64(gamesPlayed)
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
	DB().Model(&Connection{}).Where("id = ?", c.ID).Update("deconnected_at", time.Now().Unix())

}
