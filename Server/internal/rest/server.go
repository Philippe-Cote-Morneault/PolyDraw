package rest

import (
	"context"
	"log"
	"net/http"
	"time"

	"github.com/gorilla/mux"
)

type RestServer struct {
	Router *mux.Router
	h      *http.Server
}

func (a *RestServer) Initialize() {
	a.Router = mux.NewRouter()
	a.Router.Use(logMiddleware)
	a.setRouters()

	// Set 404 handle and call logMiddleware
	a.Router.NotFoundHandler = logMiddleware(http.HandlerFunc(defaultRoute))
}

// Run the app on it's router
func (a *RestServer) Run(host string) {
	a.h = &http.Server{Addr: host, Handler: a.Router}

	log.Printf("[REST] -> Server is started on %s", host)
	log.Fatal("[REST] -> ", a.h.ListenAndServe())
}

// Shutdown handler to close the server when a signal is catched
func (a *RestServer) Shutdown() {
	log.Println("[REST] -> Shutting down the REST API server...")
	ctx, _ := context.WithTimeout(context.Background(), 5*time.Second)
	a.h.Shutdown(ctx)
}

type RequestHandlerFunction func(w http.ResponseWriter, r *http.Request)

func (a *RestServer) handleRequest(handler RequestHandlerFunction) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		handler(w, r)
	}
}

// Functions that can be called by the various HTTP requests types
func (a *RestServer) Get(path string, f func(w http.ResponseWriter, r *http.Request)) {
	a.Router.HandleFunc(path, f).Methods("GET")
}

func (a *RestServer) Post(path string, f func(w http.ResponseWriter, r *http.Request)) {
	a.Router.HandleFunc(path, f).Methods("POST")
}

func (a *RestServer) Put(path string, f func(w http.ResponseWriter, r *http.Request)) {
	a.Router.HandleFunc(path, f).Methods("PUT")
}

func (a *RestServer) Delete(path string, f func(w http.ResponseWriter, r *http.Request)) {
	a.Router.HandleFunc(path, f).Methods("DELETE")
}
