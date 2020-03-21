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
	GroupName  string
	PlayersMax int
	NbRound    int
	GameType   int
	Difficulty int
}

type responsePlayer struct {
	ID       string
	Username string
	IsCPU    bool
}

type responsePlayerReal struct {
	ID       string
	Username string
}

type responseGroupCreate struct {
	GroupName string
	GroupID   string
}
type responseGroup struct {
	ID         string
	GroupName  string
	PlayersMax int
	GameType   int
	Difficulty int
	Status     int
	OwnerName  string
	OwnerID    string
	Language   int
	NbRound    int
	Players    []responsePlayer
}

const maxPlayer = 8

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

	if (request.PlayersMax > maxPlayer || request.PlayersMax < 1) && request.GameType != 1 {
		rbody.JSONError(w, http.StatusBadRequest, fmt.Sprintf("The number of players must be between 1 and %d", maxPlayer))
		return
	}
	if request.GameType == 0 && (request.NbRound <= 0 || request.NbRound > 5) {
		rbody.JSONError(w, http.StatusBadRequest, fmt.Sprintf("The number of round must be between 1 and 5 for the free for all game mode."))
		return
	}
	if request.PlayersMax != 1 && request.GameType == 1 {
		rbody.JSONError(w, http.StatusBadRequest, "The number of players must be one for the game mode Solo")
		return
	}

	if request.PlayersMax > maxPlayer {
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
		OwnerID:    userid,
		Name:       groupName,
		NbRound:    request.NbRound,
		PlayersMax: request.PlayersMax,
		GameType:   request.GameType,
		Difficulty: request.Difficulty,
		Status:     0,
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
		players := make([]responsePlayer, len(groups[i].Users))
		for j := range groups[i].Users {
			players[j] = responsePlayer{
				ID:       groups[i].Users[j].ID.String(),
				Username: groups[i].Users[j].Username,
				IsCPU:    false,
			}
		}

		response[i] = responseGroup{
			ID:         groups[i].ID.String(),
			GroupName:  groups[i].Name,
			PlayersMax: groups[i].PlayersMax,
			GameType:   groups[i].GameType,
			Difficulty: groups[i].Difficulty,
			Status:     0,
			Language:   0,
			NbRound:    groups[i].NbRound,
			OwnerName:  groups[i].Owner.Username,
			OwnerID:    groups[i].OwnerID.String(),
			Players:    players,
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
		players := make([]responsePlayer, len(group.Users))
		for j := range group.Users {
			players[j] = responsePlayer{
				ID:       group.Users[j].ID.String(),
				Username: group.Users[j].Username,
				IsCPU:    false,
			}
		}

		response := responseGroup{
			ID:         group.ID.String(),
			GroupName:  group.Name,
			PlayersMax: group.PlayersMax,
			GameType:   group.GameType,
			Difficulty: group.Difficulty,
			Status:     group.Status,
			Language:   0,
			NbRound:    group.NbRound,
			OwnerName:  group.Owner.Username,
			OwnerID:    group.Owner.ID.String(),
			Players:    players,
		}
		rbody.JSON(w, http.StatusOK, response)
		return
	}

	rbody.JSONError(w, http.StatusNotFound, "The group could not be found.")
	return

}
