package api

import (
	"encoding/json"
	"fmt"
	"net/http"
	"strconv"

	"gitlab.com/jigsawcorp/log3900/internal/services/stats"

	"gitlab.com/jigsawcorp/log3900/internal/context"
	"gitlab.com/jigsawcorp/log3900/internal/language"

	"github.com/google/uuid"
	"gitlab.com/jigsawcorp/log3900/model"
	"gitlab.com/jigsawcorp/log3900/pkg/rbody"
)

type connection struct {
	ConnectedAt    int64
	DisconnectedAt int64
}

type matchPlayed struct {
	MatchDuration int64
	WinnerName    string
	WinnerID      string
	MatchType     string
	Players       []player
}

type player struct {
	Username string
	UserID   string
}

type history struct {
	MatchesPlayedHistory []matchPlayed
	ConnectionHistory    []connection
}

// GetStats returns userStats
func GetStats(w http.ResponseWriter, r *http.Request) {

	userID := uuid.MustParse(fmt.Sprintf("%v", r.Context().Value(context.CtxUserID)))

	stats, err := stats.GetStats(userID)
	if err != "" {
		rbody.JSONError(w, http.StatusNotFound, err)
		return
	}

	json.NewEncoder(w).Encode(stats)
}

// GetHistory returns the history
func GetHistory(w http.ResponseWriter, r *http.Request) {
	var userID uuid.UUID = uuid.MustParse(fmt.Sprintf("%v", r.Context().Value(context.CtxUserID)))

	offset := 0
	limit := 100
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
					rbody.JSONError(w, http.StatusBadRequest, language.MustGetRest("error.channelInvalidStart", r))
					return
				}
			} else {
				rbody.JSONError(w, http.StatusBadRequest, language.MustGetRest("error.channelInvalidUrl", r))
				return
			}
		}
	}

	var h history

	var connectionHistory []connection
	model.DB().Model(&model.Connection{}).Where("user_id = ?", userID).Order("created_at desc").Offset(offset).Limit(limit).Find(&connectionHistory)
	h.ConnectionHistory = connectionHistory

	for i := len(h.ConnectionHistory)/2 - 1; i >= 0; i-- {
		opp := len(h.ConnectionHistory) - 1 - i
		h.ConnectionHistory[i], h.ConnectionHistory[opp] = h.ConnectionHistory[opp], h.ConnectionHistory[i]
	}

	var matchesPlayedHistory []model.MatchPlayed
	model.DB().Model(&model.MatchPlayed{}).Joins("JOIN match_played_memberships ON match_played_memberships.match_id = match_playeds.id AND match_played_memberships.user_id = ?", userID).Order("created_at desc").Offset(offset).Limit(limit).Find(&matchesPlayedHistory)

	for _, match := range matchesPlayedHistory {
		var matchType string

		switch match.MatchType {
		case 0:
			matchType = "FFA"
		case 1:
			matchType = "Solo"
		case 2:
			matchType = "Coop"
		}
		h.MatchesPlayedHistory = append(h.MatchesPlayedHistory, matchPlayed{MatchDuration: match.MatchDuration,
			WinnerName: match.WinnerName, WinnerID: match.WinnerID, MatchType: matchType, Players: getPlayersInMatch(match.ID)})
	}

	for i := len(h.MatchesPlayedHistory)/2 - 1; i >= 0; i-- {
		opp := len(h.MatchesPlayedHistory) - 1 - i
		h.MatchesPlayedHistory[i], h.MatchesPlayedHistory[opp] = h.MatchesPlayedHistory[opp], h.MatchesPlayedHistory[i]
	}

	json.NewEncoder(w).Encode(h)
}

func getPlayersInMatch(matchID uuid.UUID) []player {
	var players []player
	var users []model.User
	model.DB().Model(&model.User{}).Joins("JOIN match_played_memberships ON match_played_memberships.user_id = users.id AND match_played_memberships.match_id = ?", matchID).Find(&users)

	for _, user := range users {
		if user.ID != uuid.Nil {
			players = append(players, player{UserID: user.ID.String(), Username: user.Username})
		}
	}
	return players
}
