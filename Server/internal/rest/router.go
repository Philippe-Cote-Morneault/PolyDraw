package rest

import (
	"net/http"

	"gitlab.com/jigsawcorp/log3900/internal/api"
	"gitlab.com/jigsawcorp/log3900/pkg/rbody"
)

var authExceptions map[string]bool

// setRouters sets the all required routers
func (a *Server) setRouters() {
	authExceptions = map[string]bool{
		"/healthcheck": true,
		"/auth":        true,
	}

	a.Head("/healthcheck", a.handleRequest(api.HeadHealthcheck))
	a.Post("/auth", a.handleRequest(api.PostAuth))
	a.Get("/users", a.handleRequest(api.GetUsers))
	a.Get("/chat/channels", a.handleRequest(api.GetChatChannel))
	a.Get("/chat/channels/{id}", a.handleRequest(api.GetChatChannelID))

}

func defaultRoute(w http.ResponseWriter, r *http.Request) {
	if r.URL.Path != "/" {
		rbody.JSONError(w, http.StatusNotFound, "404 page cannot be found")
		return
	}
	rbody.JSON(w, http.StatusOK, "REST Server")
}
