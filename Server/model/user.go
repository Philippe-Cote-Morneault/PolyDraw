package model

//User represented in the database
type User struct {
	Base
	Username       string
	Email          string
	HashedPassword string
	Bearer         string
}
