package rest

import (
	"log"
	"net/http"

	"github.com/gorilla/mux"
)

type RestServer struct {
	Router *mux.Router
}

func (a *RestServer) Initialize() {
	a.Router = mux.NewRouter()
	a.setRouters()
}

// Run the app on it's router
func (a *RestServer) Run(host string) {
	log.Printf("Server is starting on %s", host)
	log.Fatal(http.ListenAndServe(host, a.Router))
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