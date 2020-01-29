package api

import (
	"log"
	"encoding/json"
	"net/http"
	"github.com/dgrijalva/jwt-go"
	"gitlab.com/jigsawcorp/log3900/model"
	"gitlab.com/jigsawcorp/log3900/pkg/rbody"
)

// LoginUser Method to test post request for login
func LoginUser(w http.ResponseWriter, r *http.Request) {
	var user *model.User
	decoder := json.NewDecoder(r.Body)
	err := decoder.Decode(&user)

	if err != nil {
		// rbody.JSONError(w, http.StatusBadRequest, "Error of username"})// Gestion d'erreur a voir avec erreur 400
	}
	// Check the username with db and send token if not found
	if model.FindUserByName(user.Username) {
		rbody.JSONError(w, http.StatusConflict, "Username already taken, please choose another username")
	} else {
		var tokenString string = generateToken(user)
		user.Bearer = tokenString
		// Insert in database
		model.AddUser(user)
		rbody.JSON(w, http.StatusOK, map[string]string{"Bearer": "?Bearer?", "SessionToken": tokenString})
		// Launch timeout token
	}
}

func generateToken(user *model.User) string {
	// Voir pour changer le protocole du token pour ameliorer la securite (RSA ou ECDSA)
	var signingKey = []byte("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VybmFtZSI6IjEyMzQ1Njc4OTAifQ.eKHf9rvGRaIEEuOHrQ9KJ9ZdRqkD37kHaeKKFzebOpU")
	token := jwt.NewWithClaims(jwt.SigningMethodHS256, jwt.MapClaims{
		"username": user.Username,
	})

	tokenString, err := token.SignedString(signingKey)
	if err != nil {
		log.Fatal(err)
	}
	return tokenString
}

func parseToken(tokenString string) {
	// Voir pour changer le protocole du token pour ameliorer la securite (RSA ou ECDSA)
	var signingKey = []byte("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VybmFtZSI6IjEyMzQ1Njc4OTAifQ.eKHf9rvGRaIEEuOHrQ9KJ9ZdRqkD37kHaeKKFzebOpU")
	token, err := jwt.Parse(tokenString, func(token *jwt.Token) (interface{}, error) {
		return signingKey, nil
	})
	if claims, ok := token.Claims.(jwt.MapClaims); ok && token.Valid {
		log.Println(claims["username"])
	} else {
		log.Fatal(err)
	}
}
