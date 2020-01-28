package model

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
