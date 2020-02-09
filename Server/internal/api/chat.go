package api

import (
	"net/http"
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
	//Get all the membership and returns the users
}
