package rest

import (
	"log"
	"net/http"
)

func logMiddleware(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		log.Printf("[REST] %s", r.RequestURI)
		next.ServeHTTP(w, r)
	})
}
