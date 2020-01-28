package model

import "gitlab.com/jigsawcorp/log3900/pkg/secureb"

import "log"

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

//New Generate a new user with a bearer token
func (u *User) New(Username string) {
	u.Username = Username
	u.FirstName = "Serge"
	u.LastName = "Paquette"
	u.Email = "serge.paquette@veryrealemail.com"
	u.HashedPassword = "random crap for now"

	bearer, err := secureb.GenerateRandomString(32)
	if err != nil {
		log.Println("Cannot create user bearer")
		log.Println(err)
	}
	u.Bearer = bearer
}
