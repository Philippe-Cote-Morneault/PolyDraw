package api

import (
	"encoding/json"
	"fmt"
	"net/http"
	"strconv"

	"github.com/google/uuid"
	"gitlab.com/jigsawcorp/log3900/model"
	"gitlab.com/jigsawcorp/log3900/pkg/rbody"
)

type userStats struct {
	Stats                stats
	ConnectionHistory    []connection
	MatchesPlayedHistory []matchPlayed
	Achievements         []achievement
}

type stats struct {
	GamesPlayed     int64
	WinRatio        float64
	AvgGameDuration int64
	TimePlayed      int64
}

type connection struct {
	ConnectedAt   int64
	DeconnectedAt int64
}

type matchPlayed struct {
	MatchDuration int64
	WinnerName    string
	MatchType     string
	PlayersNames  []playerName
}

type playerName struct {
	PlayerName string
}

type achievement struct {
	TropheeName   string
	Description   string
	ObtainingDate int64
}

type history struct {
	MatchesPlayedHistory []matchPlayed
	ConnectionHistory    []connection
}

// GetStats returns userStats
func GetStats(w http.ResponseWriter, r *http.Request) {

	var userID uuid.UUID = uuid.MustParse(fmt.Sprintf("%v", r.Context().Value(CtxUserID)))

	addJunk(userID)
	var statsDB stats
	model.DB().Model(model.Stats{}).Where("id = ?", userID).Find(&statsDB)
	var stats userStats = userStats{Stats: statsDB}

	// TODO: A implementer une fois avoir implementer l'ajout des Connection
	// if  {
	// 	rbody.JSONError(w, http.StatusNotFound, "UserID doesn't exists")
	// 	return
	// }

	var connectionHistory []connection
	model.DB().Where("user_id = ?", userID).Order("created_at desc").Find(&connectionHistory)
	stats.ConnectionHistory = connectionHistory

	var matchesPlayedHistory []model.MatchPlayed
	model.DB().Where("user_id = ?", userID).Order("created_at desc").Find(&matchesPlayedHistory)
	for _, match := range matchesPlayedHistory {
		var playersNames []playerName
		model.DB().Model(&model.PlayerName{}).Where("match_id = ?", match.ID).Find(&playersNames)
		stats.MatchesPlayedHistory = append(stats.MatchesPlayedHistory, matchPlayed{MatchDuration: match.MatchDuration,
			WinnerName: match.WinnerName, MatchType: match.MatchType, PlayersNames: playersNames})
	}

	var achievements []achievement
	model.DB().Where("user_id = ?", userID).Order("created_at desc").Find(&achievements)
	stats.Achievements = achievements

	json.NewEncoder(w).Encode(stats)
}

// GetHistory returns the history
func GetHistory(w http.ResponseWriter, r *http.Request) {
	var userID uuid.UUID = uuid.MustParse(fmt.Sprintf("%v", r.Context().Value(CtxUserID)))

	offset := 0
	limit := 100
	addJunk(userID)
	start, startOk := r.URL.Query()["start"]
	if startOk && len(start[0]) > 0 {
		end, endOk := r.URL.Query()["end"]
		if endOk && len(end[0]) > 0 {
			//Check if the number is valid
			startNum, errS := strconv.Atoi(start[0])
			endNum, errE := strconv.Atoi(end[0])
			if errS == nil && errE == nil {
				if startNum < endNum {
					offset = startNum
					newLimit := endNum - startNum + 1
					if newLimit < limit {
						limit = newLimit
					}
				} else {
					rbody.JSONError(w, http.StatusBadRequest, "Invalid parameters, start must be the lowest parameter.")
					return
				}
			} else {
				rbody.JSONError(w, http.StatusBadRequest, "Invalid parameters, the url parameters must be a number.")
				return
			}
		}
	}

	var h history

	var connectionHistory []connection
	model.DB().Where("user_id = ?", userID).Order("created_at desc").Offset(offset).Limit(limit).Find(&connectionHistory)
	h.ConnectionHistory = connectionHistory

	var matchesPlayedHistory []matchPlayed
	model.DB().Where("user_id = ?", userID).Order("created_at desc").Offset(offset).Limit(limit).Find(&matchesPlayedHistory)
	h.MatchesPlayedHistory = matchesPlayedHistory

	json.NewEncoder(w).Encode(h)
}

//AddJunk add junk for Connection, Achievement et Matchplayed
// TODO: A supprimer
func addJunk(userID uuid.UUID) {

	for i := 0; i < 20; i++ {
		model.DB().Create(&model.Connection{ConnectedAt: int64(i), DisconnectedAt: int64(i * i), UserID: userID})
	}

	for i := 0; i < 20; i++ {
		model.DB().Create(&model.Achievement{TropheeName: "Geek", Description: "A depasse les 1000000000 heures de jeu", ObtainingDate: int64(i), UserID: userID})
	}

	for i := 0; i < 20; i++ {
		var ach model.MatchPlayed = model.MatchPlayed{MatchDuration: int64(i), WinnerName: "PascalWinner", UserID: userID, MatchType: "Solo"}
		model.DB().Create(&ach)
		for i := 0; i < 3; i++ {
			model.DB().Create(&model.PlayerName{MatchID: ach.ID, PlayerName: "PascalPlayer"})
		}
	}
}
