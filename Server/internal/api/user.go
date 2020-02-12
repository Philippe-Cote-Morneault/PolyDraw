package api

import (
	"encoding/json"
	"net/http"

	"github.com/google/uuid"
	"github.com/gorilla/mux"
	"gitlab.com/jigsawcorp/log3900/model"
	"gitlab.com/jigsawcorp/log3900/pkg/rbody"
)

type singleUserResponse struct {
	ID        string
	FirstName string
	LastName  string
	Username  string
	Email     string
	PictureID int
	CreatedAt int64
	UpdatedAt int64
}

// GetUsers returns all users
func GetUsers(w http.ResponseWriter, r *http.Request) {
	var users []model.User
	model.DB().Find(&users)
	json.NewEncoder(w).Encode(users)
}

//GetUser return a single user
func GetUser(w http.ResponseWriter, r *http.Request) {
	vars := mux.Vars(r)
	var user model.User
	model.DB().Where("id = ?", vars["id"]).First(&user)
	if user.ID != uuid.Nil {
		json.NewEncoder(w).Encode(singleUserResponse{
			ID:        user.ID.String(),
			FirstName: user.FirstName,
			LastName:  user.LastName,
			Username:  user.Username,
			Email:     user.Email,
			PictureID: user.PictureID,
			CreatedAt: user.CreatedAt.Unix(),
			UpdatedAt: user.CreatedAt.Unix(),
		})
	} else {
		rbody.JSONError(w, http.StatusNotFound, "The specified user cannot be found.")
	}
}
