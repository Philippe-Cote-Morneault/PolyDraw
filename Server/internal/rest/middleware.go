package rest

import (
	"log"
	"net/http"

	"github.com/gorilla/context"
	"gitlab.com/jigsawcorp/log3900/internal/services/auth"
	"gitlab.com/jigsawcorp/log3900/pkg/rbody"
)

type statusWriter struct {
	http.ResponseWriter
	status int
	length int
}

func (w *statusWriter) WriteHeader(status int) {
	w.status = status
	w.ResponseWriter.WriteHeader(status)
}

func (w *statusWriter) Write(b []byte) (int, error) {
	if w.status == 0 {
		w.status = 200
	}
	n, err := w.ResponseWriter.Write(b)
	w.length += n
	return n, err
}

func logMiddleware(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		sw := statusWriter{ResponseWriter: w}
		next.ServeHTTP(&sw, r)
		log.Printf("[REST] %d - %s %s", sw.status, r.Method, r.RequestURI)
	})
}

func authMiddleware(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		if !stringInSlice(r.URL.Path, authExceptions) {
			sessionToken := r.Header.Get("SessionToken")
			if sessionToken != "" {
				//There is a token, check if it's valid and return the userID
				ok, userID := auth.GetUserIDFromToken(sessionToken)
				if !ok {
					rbody.JSONError(w, http.StatusForbidden, "The header SessionToken is invalid.")
				} else {
					context.Set(r, "user", userID)
					next.ServeHTTP(w, r)
				}
			} else {
				rbody.JSONError(w, http.StatusForbidden, "The header SessionToken is invalid.")
			}
		} else {
			next.ServeHTTP(w, r)
		}
	})
}

func stringInSlice(a string, list []string) bool {
	for _, b := range list {
		if b == a {
			return true
		}
	}
	return false
}
