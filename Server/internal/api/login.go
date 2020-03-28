package api

import (
	"encoding/json"
	"gitlab.com/jigsawcorp/log3900/internal/context"
	"gitlab.com/jigsawcorp/log3900/internal/language"
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
	Password string
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
		rbody.JSONError(w, http.StatusBadRequest, language.MustGetRest("error.usernameInvalid", r))
		return
	}

	//TEMPORARY FIX
	//Get the user if not create it.
	var user model.User
	if !model.FindUserByName(username, &user) {
		rbody.JSONError(w, http.StatusNotFound, language.MustGetRest("error.userNotFound", r))
		return
	}
	if !secureb.CheckPasswordHash(request.Password, user.HashedPassword) {
		rbody.JSONError(w, http.StatusForbidden, language.MustGetRest("error.passwordWrong", r))
		return
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
		rbody.JSONError(w, http.StatusBadRequest, language.MustGetRest("error.usernameInvalid", r))
		return
	}

	var count int64
	model.DB().Model(&model.User{}).Where("username = ?", username).Count(&count)
	if count > 0 {
		rbody.JSONError(w, http.StatusConflict, language.MustGetRest("error.usernameExists", r))
		return
	}

	firstName := strings.TrimSpace(request.FirstName)
	lastName := strings.TrimSpace(request.LastName)

	if firstName == "" || lastName == "" {
		rbody.JSONError(w, http.StatusBadRequest, language.MustGetRest("error.firstNameInvalid", r))
		return
	}

	if !regexEmail.MatchString(request.Email) {
		rbody.JSONError(w, http.StatusBadRequest, language.MustGetRest("error.emailInvalid", r))
		return
	}

	if request.PictureID < 1 || request.PictureID > 16 {
		rbody.JSONError(w, http.StatusBadRequest, "Invalid picture id, the number must be between 0 and 15.")
		return
	}

	//Hash the password
	if len(request.Password) < 8 {
		rbody.JSONError(w, http.StatusBadRequest, language.MustGetRest("error.passwordInvalid", r))
		return
	}

	hash, err := secureb.HashPassword(request.Password)
	if err != nil {
		rbody.JSONError(w, http.StatusBadRequest, language.MustGetRest("error.usernameFail", r))
		return
	}

	var user model.User
	err = user.New(username, firstName, lastName, request.Email, hash, request.PictureID)
	if err != nil {
		rbody.JSONError(w, http.StatusBadRequest, language.MustGetRest("error.usernameFail", r))
		return
	}
	model.AddUser(&user)

	registerSession(&user, w, r)

}

func registerSession(user *model.User, w http.ResponseWriter, r *http.Request) {
	//Check if there is already a session
	if auth.HasUserSession(user.ID) {
		//There is already a session we abort the creation of a new session
		rbody.JSONError(w, http.StatusConflict, language.MustGetRest("error.sessionExists", r))
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
		auth.Register(sessionToken, user.ID, r.Context().Value(context.CtxLang).(int))
		rbody.JSON(w, http.StatusOK, authResponse{Bearer: user.Bearer, SessionToken: sessionToken, UserID: user.ID.String()})
	}
}
