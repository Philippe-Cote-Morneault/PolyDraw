package api

import (
	"encoding/json"
	"net/http"

	"github.com/google/uuid"
	"github.com/gorilla/mux"
	"gitlab.com/jigsawcorp/log3900/model"
	"gitlab.com/jigsawcorp/log3900/pkg/rbody"
)

type channelResponse struct {
	ID    string
	Name  string
	Users []userResponse
}

type userResponse struct {
	Name string
	ID   string
}

//GetChatChannel returns all the channels of the system
func GetChatChannel(w http.ResponseWriter, r *http.Request) {
	var channels []model.ChatChannel
	var channelsResponse []channelResponse
	model.DB().Model(&channels).Related(&model.User{}, "Users")
	model.DB().Preload("Users").Find(&channels)

	for _, channel := range channels {
		if channel.ID != uuid.Nil {
			channelResponse := channelResponse{
				ID:   channel.ID.String(),
				Name: channel.Name,
			}

			users := []userResponse{}
			for _, user := range channel.Users {
				users = append(users, userResponse{
					ID:   user.ID.String(),
					Name: user.Username,
				})
			}
			channelResponse.Users = users
			channelsResponse = append(channelsResponse, channelResponse)
		}
	}
	channelsResponse = append(channelsResponse, getChatGeneralChannel())
	json.NewEncoder(w).Encode(channelsResponse)
}

//GetChatChannelID returns a specific channel
func GetChatChannelID(w http.ResponseWriter, r *http.Request) {
	vars := mux.Vars(r)
	if vars["id"] == uuid.Nil.String() {
		json.NewEncoder(w).Encode(getChatGeneralChannel())
	} else {
		var channel model.ChatChannel
		model.DB().Model(&channel).Related(&model.User{}, "Users")
		model.DB().Preload("Users").Where("id = ?", vars["id"]).Find(&channel)
		if channel.ID != uuid.Nil {
			//Simplify user output
			users := []userResponse{}
			channelResponse := channelResponse{
				ID:   channel.ID.String(),
				Name: channel.Name,
			}

			for _, user := range channel.Users {
				users = append(users, userResponse{
					ID:   user.ID.String(),
					Name: user.Username,
				})
			}
			channelResponse.Users = users
			json.NewEncoder(w).Encode(&channelResponse)
		} else {
			rbody.JSONError(w, http.StatusNotFound, "The specified channel cannot be found.")
		}
	}

}

func getChatGeneralChannel() channelResponse {
	rows, err := model.DB().Model(&model.User{}).Rows()
	defer rows.Close()
	if err != nil {
		panic(err)
	}
	user := model.User{}
	var users []userResponse

	for rows.Next() {
		model.DB().ScanRows(rows, &user)
		users = append(users, userResponse{
			Name: user.Username,
			ID:   user.ID.String(),
		})
	}

	channel := channelResponse{
		ID:    uuid.Nil.String(),
		Name:  "General",
		Users: users,
	}
	return channel
}
