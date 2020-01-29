package api

import (
	"gitlab.com/jigsawcorp/log3900/pkg/rbody"
	"net/http"
)

// HeadHealthcheck exmample of rest call
func HeadHealthcheck(w http.ResponseWriter, r *http.Request) {
	rbody.JSON(w, http.StatusOK, "It's up!")
}
