package rest

import (
	"net/http"

	"gitlab.com/jigsawcorp/log3900/internal/api"
	"gitlab.com/jigsawcorp/log3900/pkg/rbody"
)

// setRouters sets the all required routers
func (a *Server) setRouters() {
	a.Get("/hello", a.handleRequest(api.GetHello))
	a.Get("/hello/{title}", a.handleRequest(api.GetHello))
	a.Post("/auth", a.handleRequest(api.PostAuth))
	a.Post("/auth/token", a.handleRequest(api.PostAuthToken))
}

func defaultRoute(w http.ResponseWriter, r *http.Request) {
	if r.URL.Path != "/" {
		rbody.JSONError(w, http.StatusNotFound, "404 page cannot be found")
		return
	}
	rbody.JSON(w, http.StatusOK, "REST Server")
}
