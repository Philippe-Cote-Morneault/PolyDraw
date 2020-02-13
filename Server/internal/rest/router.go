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
		"/healthcheck":   true,
		"/auth":          true,
		"/auth/bearer":   true,
		"/auth/register": true,
	}

	a.Head("/healthcheck", a.handleRequest(api.HeadHealthcheck))
	a.Post("/auth", a.handleRequest(api.PostAuth))
	a.Post("/auth/bearer", a.handleRequest(api.PostAuthToken))
	a.Post("/auth/register", a.handleRequest(api.PostAuthRegister))
	a.Get("/users", a.handleRequest(api.GetUsers))
	a.Put("/users", a.handleRequest(api.PutUser))
	a.Get("/users/{id}", a.handleRequest(api.GetUser))
	a.Get("/chat/channels", a.handleRequest(api.GetChatChannel))
	a.Get("/chat/channels/{id}", a.handleRequest(api.GetChatChannelID))
	a.Get("/chat/messages/{channelID}", a.handleRequest(api.GetChatMessages))
	a.Get("/stats/{userID}", a.handleRequest(api.GetStats))

}

func defaultRoute(w http.ResponseWriter, r *http.Request) {
	if r.URL.Path != "/" {
		rbody.JSONError(w, http.StatusNotFound, "404 page cannot be found")
		return
	}
	rbody.JSON(w, http.StatusOK, "REST Server")
}
