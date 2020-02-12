package model

import (
	"log"

	"github.com/google/uuid"
	"gitlab.com/jigsawcorp/log3900/pkg/secureb"
)

//User represented in the database
type User struct {
	Base
	FirstName      string
	LastName       string
	Username       string
	PictureID      int
	Email          string
	HashedPassword string
	Bearer         string
	Channels       []*ChatChannel `gorm:"many2many:chat_channel_membership"`
}

//Session represents a session in the database
type Session struct {
	Base
	User         User `gorm:"foreignkey:UserID"`
	UserID       uuid.UUID
	SocketID     uuid.UUID
	SessionToken string
}

//NewFakeUser Generate a new user with a bearer token used to bypass auth. Will be removed
//TODO remove this method
func (u *User) NewFakeUser(Username string) error {
	u.Username = Username
	u.FirstName = "Serge"
	u.LastName = "Paquette"
	u.Email = "serge.paquette@veryrealemail.com"
	u.HashedPassword = "random crap for now"
	u.PictureID = 0

	bearer, err := secureb.GenerateToken()
	if err != nil {
		log.Println("Cannot create user bearer")
		log.Println(err)
		return err
	}
	u.Bearer = bearer

	return err
}

//New Generate a new user with a bearer token
func (u *User) New(username string, firstName string, lastName string, email string, hash string, pictureID int) error {
	u.Username = username
	u.FirstName = firstName
	u.LastName = lastName
	u.Email = email
	u.HashedPassword = hash
	u.PictureID = pictureID

	bearer, err := secureb.GenerateToken()
	if err != nil {
		log.Println("Cannot create user bearer")
		log.Println(err)
		return err
	}
	u.Bearer = bearer

	return err
}

// FindUserByName Function to find a User by Name
func FindUserByName(username string, user *User) bool {
	DB().Where("username = ?", username).Find(&user)
	return user.Username == username
}

// AddUser add user in DB
func AddUser(user *User) {
	DB().Create(&user)
}
