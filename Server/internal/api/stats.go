package api

import (
	"encoding/json"
	"fmt"
	"net/http"

	"github.com/google/uuid"
	"gitlab.com/jigsawcorp/log3900/model"
)

type userStats struct {
	GamesPlayed          int64
	WinRatio             float64
	AvgGameDuration      int64
	TimePlayed           int64
	ConnectionHistory    []connection
	MatchesPlayedHistory []matchPlayed
	Achievements         []achievement
}

type connection struct {
	ConnectedAt   int64
	DeconnectedAt int64
}

type matchPlayed struct {
	PlayerNames   []string
	MatchDuration int64
	WinnerName    string
}

type achievement struct {
	TropheeName   string
	Description   string
	ObtainingDate int64
}

// GetStats return userStats
func GetStats(w http.ResponseWriter, r *http.Request) {

	var stats userStats
	var userID uuid.UUID = uuid.MustParse(fmt.Sprintf("%v", r.Context().Value(CtxUserID)))

	model.DB().Where("userID = ?", userID).Find(&stats)

	// TODO: A implementer une fois avoir implementer l'ajout des Connection
	// if  {
	// 	rbody.JSONError(w, http.StatusNotFound, "UserID doesn't exists")
	// 	return
	// }

	var connectionHistory []connection
	model.DB().Where("user_id = ?", userID).Find(&connectionHistory)
	stats.ConnectionHistory = connectionHistory

	var matchesPlayedHistory []matchPlayed
	model.DB().Where("user_id = ?", userID).Find(&matchesPlayedHistory)
	stats.MatchesPlayedHistory = matchesPlayedHistory

	var achievements []achievement
	model.DB().Where("user_id = ?", userID).Find(&achievements)
	stats.Achievements = achievements

	json.NewEncoder(w).Encode(stats)
}
