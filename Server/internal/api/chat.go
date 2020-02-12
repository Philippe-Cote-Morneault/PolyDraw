package api

import (
	"encoding/json"
	"net/http"
	"strconv"

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

type messageResponse struct {
	ChannelID string
	UserID    string
	Username  string
	Timestamp int64
	Message   string
}
type messageTotalResponse struct {
	Messages      *[]messageResponse
	MessagesTotal int64
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

//GetChatMessages returns all the chat messages in the order from most recent to least recent
func GetChatMessages(w http.ResponseWriter, r *http.Request) {
	vars := mux.Vars(r)
	channelID, err := uuid.Parse(vars["channelID"])
	if err == nil {
		//Check if the channel exist
		channel := model.ChatChannel{}
		model.DB().Where("id = ?", channelID).Find(&channel)
		if channel.ID != uuid.Nil || channelID == uuid.Nil {
			//Get the first range of a 100 messages
			offset := 0
			limit := 100

			start, startOk := r.URL.Query()["start"]
			if startOk && len(start[0]) > 0 {
				end, endOk := r.URL.Query()["end"]
				if endOk && len(end[0]) > 0 {
					//Check if the number is valid
					startNum, err := strconv.Atoi(start[0])
					endNum, err := strconv.Atoi(end[0])
					if err == nil {
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

			var totalMessages int64
			model.DB().Model(&model.ChatMessage{}).Where("channel_id = ?", channelID).Count(&totalMessages)

			messages := []model.ChatMessage{}
			model.DB().Model(&model.ChatMessage{}).Related(&model.User{}, "User")
			model.DB().Preload("User").Where("channel_id = ?", channelID).Order("created_at desc").Offset(offset).Limit(limit).Find(&messages)

			messagesResponse := make([]messageResponse, len(messages))
			for i := range messages {
				messagesResponse[i] = messageResponse{
					UserID:    messages[i].UserID.String(),
					Username:  messages[i].User.Username,
					ChannelID: messages[i].ChannelID.String(),
					Timestamp: messages[i].Timestamp,
					Message:   messages[i].Message,
				}
			}
			response := messageTotalResponse{
				Messages:      &messagesResponse,
				MessagesTotal: totalMessages,
			}
			json.NewEncoder(w).Encode(&response)
		} else {
			rbody.JSONError(w, http.StatusNotFound, "The channel ID could not be found.")
		}
	} else {
		rbody.JSONError(w, http.StatusBadRequest, "Invalid channel ID. It must respect the UUID format.")
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
