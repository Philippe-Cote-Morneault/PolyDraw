package api

import (
	"net/http"
)

func GetHello(w http.ResponseWriter, r *http.Request) {
	message := map[string]int{"apple": 5, "lettuce": 7}
	respondJSON(w, http.StatusOK, message)
}
