package api

import (
	"encoding/json"
	"net/http"
	"strings"

	"gitlab.com/jigsawcorp/log3900/internal/services/auth"
	"gitlab.com/jigsawcorp/log3900/model"
	"gitlab.com/jigsawcorp/log3900/pkg/rbody"
	"gitlab.com/jigsawcorp/log3900/pkg/secureb"
)

type authRequest struct {
	Username string
}

type authResponse struct {
	Bearer       string
	SessionToken string
	UserID       string
}

// PostAuth authenticate using password
func PostAuth(w http.ResponseWriter, r *http.Request) {
	var request authRequest
	decoder := json.NewDecoder(r.Body)
	err := decoder.Decode(&request)

	if err != nil {
		rbody.JSONError(w, http.StatusBadRequest, err.Error())
		return
	}
	username := strings.ToLower(request.Username)

	if len(username) < 4 || len(username) > 12 {
		rbody.JSONError(w, http.StatusBadRequest, "The username must be between 4 and 12 characters")
		return
	}

	//TEMPORARY FIX
	//Get the user if not create it.
	var user model.User
	if !model.FindUserByName(username, &user) {
		//The user does not already exists create it
		user = model.User{}
		if user.New(username) != nil {
			rbody.JSONError(w, http.StatusBadRequest, "The user could not be created!")
			return
		}
		model.AddUser(&user)
	}

	//Check if there is already a session
	if auth.HasUserSession(user.ID) {
		//There is already a session we abort the creation of a new session
		rbody.JSONError(w, http.StatusConflict, "The user already has an other session tied to this account. Please disconnect the other session before connecting.")
		return
	}

	if ok, sessionToken := auth.HasUserToken(user.ID); ok {
		//The token is already registered
		rbody.JSON(w, http.StatusOK, authResponse{Bearer: user.Bearer, SessionToken: sessionToken, UserID: user.ID.String()})
	} else {
		//Generate session token
		sessionToken, _ := secureb.GenerateToken()
		for !auth.IsTokenAvailable(sessionToken) {
			sessionToken, _ = secureb.GenerateToken()
		}
		auth.Register(sessionToken, user.ID)
		rbody.JSON(w, http.StatusOK, authResponse{Bearer: user.Bearer, SessionToken: sessionToken, UserID: user.ID.String()})
	}

}

//PostAuthToken authenticate using the bearer token
func PostAuthToken(w http.ResponseWriter, r *http.Request) {

}
