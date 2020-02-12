package api

import (
	"encoding/json"
	"net/http"
	"strings"

	"github.com/google/uuid"
	"gitlab.com/jigsawcorp/log3900/internal/services/auth"
	"gitlab.com/jigsawcorp/log3900/model"
	"gitlab.com/jigsawcorp/log3900/pkg/rbody"
	"gitlab.com/jigsawcorp/log3900/pkg/secureb"
)

type authRegisterRequest struct {
	Username  string
	Password  string
	Email     string
	FirstName string
	LastName  string
	PictureID int
}

type authRequest struct {
	Username string
}

type authBearerRequest struct {
	Bearer   string
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
		if user.NewFakeUser(username) != nil {
			rbody.JSONError(w, http.StatusBadRequest, "The user could not be created!")
			return
		}
		model.AddUser(&user)
	}
	registerSession(&user, w, r)

}

//PostAuthToken authenticate using the bearer token
func PostAuthToken(w http.ResponseWriter, r *http.Request) {
	var request authBearerRequest
	decoder := json.NewDecoder(r.Body)
	err := decoder.Decode(&request)

	if err != nil {
		rbody.JSONError(w, http.StatusBadRequest, err.Error())
		return
	}
	var user model.User
	model.DB().Where("bearer = ? AND username = ?", request.Bearer, request.Username).First(&user)

	if user.ID != uuid.Nil {
		registerSession(&user, w, r)
	} else {
		rbody.JSONError(w, http.StatusUnauthorized, "The bearer token is invalid.")
	}
}

//PostAuthRegister is used to create a new user
func PostAuthRegister(w http.ResponseWriter, r *http.Request) {
	var request authRegisterRequest
	decoder := json.NewDecoder(r.Body)
	err := decoder.Decode(&request)

	if err != nil {
		rbody.JSONError(w, http.StatusBadRequest, err.Error())
		return
	}
	username := strings.ToLower(request.Username)
	if !regexUsername.MatchString(username) {
		rbody.JSONError(w, http.StatusBadRequest, "Invalid username, it must have between 4 and 12.")
		return
	}

	var count int64
	model.DB().Model(&model.User{}).Where("username = ?", username).Count(&count)
	if count > 0 {
		rbody.JSONError(w, http.StatusConflict, "The username already exists. Please choose a diffrent username.")
		return
	}

	firstName := strings.TrimSpace(request.FirstName)
	lastName := strings.TrimSpace(request.LastName)

	if firstName == "" || lastName == "" {
		rbody.JSONError(w, http.StatusBadRequest, "Invalid first name or last name, it should not be empty.")
		return
	}

	if !regexEmail.MatchString(request.Email) {
		rbody.JSONError(w, http.StatusBadRequest, "Invalid email, it must respect the typical email format.")
		return
	}

	if request.PictureID > 0 && request.PictureID > 15 {
		rbody.JSONError(w, http.StatusBadRequest, "Invalid picture id, the number must be between 0 and 15.")
		return
	}

	//Hash the password
	if len(request.Password) < 8 {
		rbody.JSONError(w, http.StatusBadRequest, "Invalid password, it must have a minimum of 8 characters.")
		return
	}

	hash, _ := secureb.HashPassword(request.Password)
	var user model.User
	err = user.New(username, firstName, lastName, request.Email, hash, request.PictureID)

	if err != nil {
		rbody.JSONError(w, http.StatusBadRequest, "The user could not be created.")
		return
	}
	model.DB().Create(&user)

	registerSession(&user, w, r)

}

func registerSession(user *model.User, w http.ResponseWriter, r *http.Request) {
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
