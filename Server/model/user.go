package model

import (
	"encoding/json"
	"net/http"
)

//User represented in the database
type User struct {
	Base
	Username       string
	Email          string
	HashedPassword string
	Bearer         string
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
