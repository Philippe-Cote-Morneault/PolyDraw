package api

import (
	"encoding/json"
	"net/http"

	"gitlab.com/jigsawcorp/log3900/model"
)

// GetUsers returns all users
func GetUsers(w http.ResponseWriter, r *http.Request) {
	var users []model.User
	model.DB().Find(&users)
	json.NewEncoder(w).Encode(users)
}
