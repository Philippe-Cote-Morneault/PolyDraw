package api

import (
	"encoding/json"
	"fmt"
	"github.com/google/uuid"
	"github.com/moby/moby/pkg/namesgenerator"
	"gitlab.com/jigsawcorp/log3900/model"
	"gitlab.com/jigsawcorp/log3900/pkg/rbody"
	"net/http"
)

type requestGroupCreate struct {
	GroupName      string
	PlayersMax     int
	VirtualPlayers int
	GameType       int
	Difficulty     int
}

type responseGroupCreate struct {
	GroupName string
	GroupID   string
}

const maxPlayer = 12

//PostGroup used to create a new group
func PostGroup(w http.ResponseWriter, r *http.Request) {
	var request requestGroupCreate
	decoder := json.NewDecoder(r.Body)
	err := decoder.Decode(&request)

	if err != nil {
		rbody.JSONError(w, http.StatusBadRequest, err.Error())
		return
	}
	if request.GameType > 2 || request.GameType < 0 {
		rbody.JSONError(w, http.StatusBadRequest, "The game mode must be between 0 and 2")
		return
	}
	if request.VirtualPlayers < 0 || request.VirtualPlayers > 11 {
		rbody.JSONError(w, http.StatusBadRequest, "You must set the number of virtual players between 0 and 11")
		return
	}

	if (request.PlayersMax > maxPlayer || request.PlayersMax < 1) && request.GameType != 1 {
		rbody.JSONError(w, http.StatusBadRequest, fmt.Sprintf("The number of players must be between 1 and %d", maxPlayer))
		return
	}
	if request.PlayersMax != 1 && request.GameType == 1 {
		rbody.JSONError(w, http.StatusBadRequest, "The number of players must be one for the game mode Solo")
		return
	}
	if request.VirtualPlayers != 0 && request.GameType == 1 {
		rbody.JSONError(w, http.StatusBadRequest, "The number of virtual players must be set to zero for the game mode Solo")
		return
	}
	if request.PlayersMax == 1 && request.VirtualPlayers <= 0 && request.GameType != 1 {
		rbody.JSONError(w, http.StatusBadRequest, "There must be some virtual players if you are the only player in the group.")
		return
	}

	totalPlayers := request.VirtualPlayers + request.PlayersMax
	if totalPlayers > maxPlayer {
		rbody.JSONError(w, http.StatusBadRequest, fmt.Sprintf("You cannot have more than %d players in a game", maxPlayer))
		return
	}

	var groupName string
	if request.GroupName != "" {
		groupName = request.GroupName
	} else {
		//Generate a docker like name
		groupName = namesgenerator.GetRandomName(0)
	}

	userid := r.Context().Value(CtxUserID).(uuid.UUID)

	group := model.Group{
		OwnerID:        userid,
		Name:           groupName,
		PlayersMax:     request.PlayersMax,
		VirtualPlayers: request.VirtualPlayers,
		GameType:       request.GameType,
		Difficulty:     request.Difficulty,
		Status:         0,
	}

	model.DB().Create(&group)
	model.DB().Model(&group).Association("Users").Append(&model.User{
		Base: model.Base{
			ID: userid,
		},
	})

	response := responseGroupCreate{
		GroupName: groupName,
		GroupID:   group.ID.String(),
	}

	rbody.JSON(w, http.StatusOK, response)
}
