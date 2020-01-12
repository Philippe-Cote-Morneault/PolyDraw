package api

import (
	"gitlab.com/jigsawcorp/log3900/pkg/rbody"
	"net/http"
)

// GetHello exmample of rest call
func GetHello(w http.ResponseWriter, r *http.Request) {
	message := map[string]int{"apple": 5, "lettuce": 7}
	rbody.JSON(w, http.StatusOK, message)
}
