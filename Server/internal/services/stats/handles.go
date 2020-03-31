package stats

import (
	"log"
	"math"
	"time"

	"github.com/google/uuid"
	"gitlab.com/jigsawcorp/log3900/internal/match"
	"gitlab.com/jigsawcorp/log3900/internal/services/auth"
	"gitlab.com/jigsawcorp/log3900/model"
)

// DataStats stats of user
type DataStats struct {
	GamesPlayed     int64
	WinRatio        float64
	AvgGameDuration float64
	TimePlayed      int64
	BestScoreSolo   int64
}

func updateMatchesPlayed(stats match.StatsData) {
	for _, socketID := range stats.SocketsID {
		userID, err := auth.GetUserID(socketID)
		if err != nil {
			log.Printf("[Stats] -> [Error] Can't find userID from socketID: %v.", socketID)
			continue
		}
		stats.Match.UserID = userID
		model.DB().Create(&(*stats.Match))
	}
}

func setDeconnection(socketID uuid.UUID) {
	userID, err := auth.GetUserID(socketID)
	if err != nil {
		log.Printf("[Stats] -> [Error] Can't find userID from socketID: %v.", socketID)
		return
	}
	var c model.Connection
	model.DB().Model(&model.Connection{}).Where("user_id = ?", userID).Order("created_at desc").Offset(0).Limit(1).Find(&c)
	model.DB().Model(&model.Connection{}).Where("id = ?", c.ID).Update("disconnected_at", time.Now().Unix())
}

func createConnection(userID uuid.UUID) {
	model.DB().Create(&model.Connection{UserID: userID, ConnectedAt: time.Now().Unix()})
}

// GetStats find in BD all stats of user
func GetStats(userID uuid.UUID) (DataStats, string) {
	var user model.User
	model.DB().Model(&model.User{}).Where("id = ?", userID).Find(&user)

	if user.ID == uuid.Nil {
		return DataStats{}, "No user found with this userID"
	}

	var matches []model.MatchPlayed
	model.DB().Model(&model.MatchPlayed{}).Where("user_id = ?", userID).Find(&matches)

	if len(matches) == 0 {
		return DataStats{}, "No matches found with this userID"
	}

	gamesPlayed := int64(len(matches))
	nbWins := 0
	timePlayed := int64(0)
	bestScoreSolo := int64(math.MaxInt64)
	for _, match := range matches {
		//Cherche minimum score solo
		if match.MatchType == 1 && match.MatchDuration < bestScoreSolo {
			bestScoreSolo = match.MatchDuration
		}

		if user.Username == match.WinnerName {
			nbWins++
		}

		timePlayed += match.MatchDuration
	}
	// Si on ne trouve pas de game solo
	if bestScoreSolo == int64(math.MaxInt64) {
		bestScoreSolo = -1
	}

	winRatio := float64(nbWins) / float64(len(matches))

	return DataStats{AvgGameDuration: float64(timePlayed) / float64(gamesPlayed), GamesPlayed: gamesPlayed, TimePlayed: timePlayed, WinRatio: winRatio, BestScoreSolo: bestScoreSolo}, ""
}
