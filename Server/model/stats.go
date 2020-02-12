package model

import (
	"github.com/google/uuid"
)

//UserStats represents the statistics of the player
type UserStats struct {
	Base
	User                 User `gorm:"foreignkey:UserID"`
	UserID               uuid.UUID
	GamesPlayed          int64
	WinRatio             float64
	AvgGameDuration      int64
	TimePlayed           int64         `gorm:"foreignkey:ConnectionHistory"`
	ConnectionHistory    []Connection  `gorm:"foreignkey:MatchesPlayedHistory"`
	MatchesPlayedHistory []MatchPlayed `gorm:"foreignkey:Achievements"`
	Achievements         []Achievement
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

// FindStatsByUserID finds stats by userId
func FindStatsByUserID(userID uuid.UUID, userStats *UserStats) bool {
	DB().Where("userID = ?", userID).Find(&userStats)
	return userStats.ID == userID
}

// UpdateStats updates userStats each time a match/game is finished
func UpdateStats(userID uuid.UUID, matchPlayed *MatchPlayed, connection *Connection) {
	var userStats UserStats
	DB().Where("userID = ?", userID).Find(&userStats)

	var gamesPlayed int64 = userStats.GamesPlayed + 1
	var timePlayed int64 = userStats.TimePlayed + matchPlayed.MatchDuration

	var winRatio float64 = userStats.WinRatio
	if matchPlayed.WinnerName == userStats.User.Username {
		winRatio = (userStats.WinRatio*float64(gamesPlayed) + 1) / float64(gamesPlayed)
	}

	userStats.ConnectionHistory = append(userStats.ConnectionHistory, *connection)
	userStats.MatchesPlayedHistory = append(userStats.MatchesPlayedHistory, *matchPlayed)

	DB().Where("userID = ?", userID).Updates(map[string]interface{}{
		"gamesPlayed":          gamesPlayed,
		"winRatio":             winRatio,
		"timePlayed":           timePlayed,
		"avgGameDuration":      float64(timePlayed) / float64(gamesPlayed),
		"matchesPlayedHistory": userStats.MatchesPlayedHistory,
		"connectionHistory":    userStats.ConnectionHistory,
	})
}

// AddAchievement will add the achievement to the userStats
func AddAchievement(userID uuid.UUID, achievement *Achievement) {
	var userStats UserStats
	DB().Where("userID = ?", userID).Find(&userStats)

	userStats.Achievements = append(userStats.Achievements, *achievement)

	DB().Where("userID = ?", userID).Update("achievements", userStats.Achievements)

}
