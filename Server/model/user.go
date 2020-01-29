package model

import (
	"encoding/json"
	"log"
	"net/http"

	"github.com/google/uuid"
	"gitlab.com/jigsawcorp/log3900/pkg/secureb"
)

//User represented in the database
type User struct {
	Base
	FirstName      string
	LastName       string
	Username       string
	Email          string
	HashedPassword string
	Bearer         string
}

//Session represents a session in the database
type Session struct {
	Base
	User         User `gorm:"foreignkey:UserID"`
	UserID       uuid.UUID
	SocketID     uuid.UUID
	SessionToken string
}

//New Generate a new user with a bearer token
func (u *User) New(Username string) error {
	u.Username = Username
	u.FirstName = "Serge"
	u.LastName = "Paquette"
	u.Email = "serge.paquette@veryrealemail.com"
	u.HashedPassword = "random crap for now"

	bearer, err := secureb.GenerateToken()
	if err != nil {
		log.Println("Cannot create user bearer")
		log.Println(err)
		return err
	}
	u.Bearer = bearer

	return err
}

// AllUsers Function Test to get All Users #Allan
func AllUsers(w http.ResponseWriter, r *http.Request) {
	var users []User
	DB().Find(&users)
	json.NewEncoder(w).Encode(users)

}

// FindUserByName Function to find a User by Name
func FindUserByName(username string) bool {
	var user User
	DB().Where("username = ?", username).Find(&user)
	return user.Username == username
}

// AddUser add user in DB
func AddUser(user *User) {
	DB().Create(&user)
}
