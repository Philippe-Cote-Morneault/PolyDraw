package rest

import (
	"context"
	"log"
	"net/http"
	"time"

	"github.com/gorilla/mux"
)

// Server represents a restserver
type Server struct {
	Router *mux.Router
	h      *http.Server
}

// Initialize method to call when creating a new rest server
func (a *Server) Initialize() {
	a.Router = mux.NewRouter()
	a.Router.Use(logMiddleware)
	a.Router.Use(authMiddleware)
	a.setRouters()

	// Set 404 handle and call logMiddleware
	a.Router.NotFoundHandler = logMiddleware(http.HandlerFunc(defaultRoute))
}

// Run the app on it's router
func (a *Server) Run(host string) {
	a.h = &http.Server{Addr: host, Handler: a.Router}

	log.Printf("[REST] -> Server is started on %s", host)

	err := a.h.ListenAndServe()
	if err != nil {
		errString := err.Error()
		if errString != "http: Server closed" {
			log.Fatal("[REST] -> ", err)
		}
	}
}

// Shutdown handler to close the server when a signal is catched
func (a *Server) Shutdown() {
	log.Println("[REST] -> Shutting down the REST API server...")
	ctx, _ := context.WithTimeout(context.Background(), 5*time.Second)
	a.h.Shutdown(ctx)
}

// RequestHandlerFunction type to use for various http verbs
type RequestHandlerFunction func(w http.ResponseWriter, r *http.Request)

func (a *Server) handleRequest(handler RequestHandlerFunction) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		handler(w, r)
	}
}

// Functions that can be called by the various HTTP requests types

//Get handler for method GET
func (a *Server) Get(path string, f func(w http.ResponseWriter, r *http.Request)) {
	a.Router.HandleFunc(path, f).Methods("GET")
}

//Post handler for method POST
func (a *Server) Post(path string, f func(w http.ResponseWriter, r *http.Request)) {
	a.Router.HandleFunc(path, f).Methods("POST")
}

//Put handler for method PUT
func (a *Server) Put(path string, f func(w http.ResponseWriter, r *http.Request)) {
	a.Router.HandleFunc(path, f).Methods("PUT")
}

//Delete handler for method DELETE
func (a *Server) Delete(path string, f func(w http.ResponseWriter, r *http.Request)) {
	a.Router.HandleFunc(path, f).Methods("DELETE")
}

//Head handler for method GET
func (a *Server) Head(path string, f func(w http.ResponseWriter, r *http.Request)) {
	a.Router.HandleFunc(path, f).Methods("HEAD")
}
