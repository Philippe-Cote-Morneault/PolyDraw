package api

import (
	"encoding/json"
	"net/http"

	"github.com/gorilla/mux"
	"gitlab.com/jigsawcorp/log3900/model"
)

// GetStats return userStats
func GetStats(w http.ResponseWriter, r *http.Request) {
	vars := mux.Vars(r)

	var users []model.UserStats
	model.DB().Where("userID = ?", vars["userID"]).Find(&users)
	json.NewEncoder(w).Encode(users)
}
