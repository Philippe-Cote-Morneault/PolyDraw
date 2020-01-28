package api

import (
	"encoding/json"
	"io/ioutil"
	"net/http"

	"gitlab.com/jigsawcorp/log3900/model"
	"gitlab.com/jigsawcorp/log3900/pkg/rbody"
)

type authRequest struct {
	Username string
}

// PostAuth authenticate using password
func PostAuth(w http.ResponseWriter, r *http.Request) {
	var request authRequest
	reqBody, _ := ioutil.ReadAll(r.Body)
	err := json.Unmarshal(reqBody, &request)

	if err != nil {
		rbody.JSONError(w, 400, err.Error())
		return
	}

	if len(request.Username) < 4 || len(request.Username) > 12 {
		rbody.JSONError(w, 400, "The username must be between 4 and 12 characters")
		return
	}

	//TEMPORARY FIX
	//Get the user if not create it.
	var user model.User
	model.DB().Where("Username = ?", request.Username).First(&user)

	if user.Username == "" {
		//The username is not set.
		rbody.JSON(w, http.StatusOK, map[string]string{"username": "Empty"})
	} else {
		rbody.JSON(w, http.StatusOK, map[string]string{"username": "Not empty"})

	}

}

//PostAuthToken authenticate using the bearer token
func PostAuthToken(w http.ResponseWriter, r *http.Request) {

}
