package api

import (
	"encoding/json"
	"fmt"
	"github.com/google/uuid"
	"github.com/gorilla/mux"
	"github.com/moby/moby/pkg/namesgenerator"
	"gitlab.com/jigsawcorp/log3900/internal/services/lobby"
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
type responseGroup struct {
	ID             string
	GroupName      string
	PlayersMax     int
	VirtualPlayers int
	GameType       int
	Difficulty     int
	Status         int
	Owner          userResponse
	Players        []userResponse
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

	if request.Difficulty < 0 || request.Difficulty > 3 {
		rbody.JSONError(w, http.StatusBadRequest, "The difficulty must be between 0 and 3.")
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
	userid := r.Context().Value(CtxUserID).(uuid.UUID)

	var count int64
	model.DB().Table("groups").Where("owner_id = ? and status = ?", userid, 0).Count(&count)
	if count != 0 {
		rbody.JSONError(w, http.StatusConflict, "You already have a group created you cannot create multiple groups.")
		return
	}
	var groupName string
	if request.GroupName != "" {
		groupName = request.GroupName
	} else {
		//Generate a docker like name
		groupName = namesgenerator.GetRandomName(0)
	}

	group := model.Group{
		OwnerID:        userid,
		Name:           groupName,
		PlayersMax:     request.PlayersMax,
		VirtualPlayers: request.VirtualPlayers,
		GameType:       request.GameType,
		Difficulty:     request.Difficulty,
		Status:         0,
	}
	var user model.User
	model.DB().Model(&user).Where("id = ?", userid).First(&user)
	model.DB().Create(&group)

	group.Owner = user
	response := responseGroupCreate{
		GroupName: groupName,
		GroupID:   group.ID.String(),
	}
	lobby.Instance().CreateGroup(&group)

	rbody.JSON(w, http.StatusOK, response)
}

//GetGroups returns all the groups that are currently available
func GetGroups(w http.ResponseWriter, r *http.Request) {
	var groups []model.Group
	model.DB().Model(&groups).Related(&model.User{}, "Users")
	model.DB().Preload("Users").Preload("Owner").Where("status = ?", 0).Find(&groups)

	response := make([]responseGroup, len(groups))
	for i := range groups {
		owner := userResponse{
			Name: groups[i].Owner.Username,
			ID:   groups[i].OwnerID.String(),
		}
		players := make([]userResponse, len(groups[i].Users))
		for j := range groups[i].Users {
			players[j] = userResponse{
				ID:   groups[i].Users[j].ID.String(),
				Name: groups[i].Users[j].Username,
			}
		}

		response[i] = responseGroup{
			ID:             groups[i].ID.String(),
			GroupName:      groups[i].Name,
			PlayersMax:     groups[i].PlayersMax,
			VirtualPlayers: groups[i].VirtualPlayers,
			GameType:       groups[i].GameType,
			Difficulty:     groups[i].Difficulty,
			Status:         0,
			Owner:          owner,
			Players:        players,
		}
	}
	rbody.JSON(w, http.StatusOK, response)
}

//GetGroup returns the details of the specific group
func GetGroup(w http.ResponseWriter, r *http.Request) {
	vars := mux.Vars(r)
	groupID, err := uuid.Parse(vars["id"])
	if err != nil {
		rbody.JSONError(w, http.StatusBadRequest, "The id is not a correct UUID format.")
		return
	}
	var group model.Group
	model.DB().Model(&group).Related(&model.User{}, "Users")
	model.DB().Preload("Users").Preload("Owner").Where("id = ?", groupID).First(&group)

	if group.ID != uuid.Nil {
		owner := userResponse{
			Name: group.Owner.Username,
			ID:   group.OwnerID.String(),
		}
		players := make([]userResponse, len(group.Users))
		for j := range group.Users {
			players[j] = userResponse{
				ID:   group.Users[j].ID.String(),
				Name: group.Users[j].Username,
			}
		}

		response := responseGroup{
			ID:             group.ID.String(),
			GroupName:      group.Name,
			PlayersMax:     group.PlayersMax,
			VirtualPlayers: group.VirtualPlayers,
			GameType:       group.GameType,
			Difficulty:     group.Difficulty,
			Status:         group.Status,
			Owner:          owner,
			Players:        players,
		}
		rbody.JSON(w, http.StatusOK, response)
		return
	}

	rbody.JSONError(w, http.StatusNotFound, "The group could not be found.")
	return

}
