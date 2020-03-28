package rest

import (
	"context"
	"gitlab.com/jigsawcorp/log3900/internal/language"
	"log"
	"net/http"
	"strings"

	"gitlab.com/jigsawcorp/log3900/internal/api"
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
		langStr := r.Header.Get("Language")
		lang := language.EN
		if strings.ToLower(langStr) == "fr" {
			lang = language.FR
		}
		ctx := context.WithValue(r.Context(), api.CtxLang, lang)

		if val, ok := authExceptions[r.URL.Path]; ok && val {
			next.ServeHTTP(w, r.WithContext(ctx))
		} else {
			sessionToken := r.Header.Get("SessionToken")
			if sessionToken != "" {
				//There is a token, check if it's valid and return the userID
				ok, userID := auth.GetUserIDFromToken(sessionToken)
				if !ok {
					rbody.JSONError(w, http.StatusForbidden, "The header SessionToken is invalid.")
				} else {
					ctx = context.WithValue(ctx, api.CtxUserID, userID)
					next.ServeHTTP(w, r.WithContext(ctx))
				}
			} else {
				rbody.JSONError(w, http.StatusForbidden, "The header SessionToken is invalid.")
			}
		}
	})
}
