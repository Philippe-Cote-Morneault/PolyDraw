package rest

import (
	"gitlab.com/jigsawcorp/log3900/internal/api"
)

// setRouters sets the all required routers
func (a *RestServer) setRouters() {
	a.Get("/hello", a.handleRequest(api.GetHello))
	a.Get("/hello/{title}", a.handleRequest(api.GetHello))
}
