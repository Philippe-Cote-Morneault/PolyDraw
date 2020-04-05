package auth

import (
	"fmt"
	"log"
	"sync"
	"time"

	"gitlab.com/jigsawcorp/log3900/internal/services/stats/broadcast"

	"github.com/google/uuid"
	"gitlab.com/jigsawcorp/log3900/internal/socket"
	"gitlab.com/jigsawcorp/log3900/model"
	"gitlab.com/jigsawcorp/log3900/pkg/cbroadcast"
)

type session struct {
	userID uuid.UUID
	lang   int
}

var mutex sync.Mutex
var tokenAvailable map[string]session  //token
var sessionCache map[uuid.UUID]session //socketID
var userCache map[uuid.UUID]uuid.UUID  //userID -> socketID

func initTokenAvailable() {
	if tokenAvailable == nil {
		tokenAvailable = make(map[string]session)
	}
	if sessionCache == nil {
		sessionCache = make(map[uuid.UUID]session)
		userCache = make(map[uuid.UUID]uuid.UUID)
	}
}

//Register the token that the client can use for authentification
func Register(token string, userID uuid.UUID, lang int) {
	defer mutex.Unlock()
	mutex.Lock()

	tokenAvailable[token] = session{
		userID: userID,
		lang:   lang,
	}
	log.Printf("[Auth] -> Registering user ID: %s", userID)
}

//UnRegisterSocket removes the session from the socketID
func UnRegisterSocket(socketID uuid.UUID) {
	defer mutex.Unlock()
	mutex.Lock()
	cbroadcast.Broadcast(broadcast.BSetDeconnection, socketID)
	var session model.Session
	if removingSessions.IsSet() {
		return
	}
	model.DB().Where("socket_id = ?", socketID).First(&session)

	if session.ID != uuid.Nil {
		go delayUnregister(&session)
	}
}

//UnRegisterUser removes the session from the userID
func UnRegisterUser(userID uuid.UUID) {
	defer mutex.Unlock()
	mutex.Lock()

	var session model.Session
	if removingSessions.IsSet() {
		return
	}
	model.DB().Where("user_id = ?", userID).First(&session)

	if session.ID != uuid.Nil {
		go delayUnregister(&session)
	}
}

//delayUnregister is used to delete the trace in the system 120 seconds after the connection was closed. It gives time
//for the services that needs this data to
func delayUnregister(session *model.Session) {
	time.Sleep(time.Second * 5)
	//TODO do something better perhaps a seperate queue for the messages that will be deleted.
	defer mutex.Unlock()
	mutex.Lock()

	delete(tokenAvailable, session.SessionToken)
	delete(sessionCache, session.SocketID)
	delete(userCache, session.UserID)
	model.DB().Delete(session) //Remove the session

}

//GetUserIDFromToken returns the userID based on the session token
func GetUserIDFromToken(token string) (bool, uuid.UUID) {
	defer mutex.Unlock()
	mutex.Lock()
	session, ok := tokenAvailable[token]
	if ok {
		return true, session.userID
	}
	return false, uuid.Nil
}

//IsTokenAvailable makes sure that the token does not already exist in the session table
func IsTokenAvailable(token string) bool {
	defer mutex.Unlock()
	mutex.Lock()

	_, ok := tokenAvailable[token]
	return !ok
}

//IsAuthenticated returns if the user is authenticated by the rest API. Parse the message if it is the correct type
func IsAuthenticated(messageReceived socket.RawMessageReceived) bool {
	defer mutex.Unlock()
	mutex.Lock()

	if messageReceived.Payload.MessageType == byte(socket.MessageType.ServerConnection) {

		bytes := messageReceived.Payload.Bytes
		token := string(bytes)

		if session, ok := tokenAvailable[token]; ok {

			if hasUserSession(session.userID) {
				log.Printf("[Auth] -> Connection already exists dropping %s", messageReceived.SocketID)
				sendAuthResponse(false, messageReceived.SocketID)
				return false
			}

			model.DB().Create(&model.Session{
				UserID:       session.userID,
				SessionToken: token,
				SocketID:     messageReceived.SocketID,
			})

			cbroadcast.Broadcast(broadcast.BCreateConnection, messageReceived.SocketID)

			sessionCache[messageReceived.SocketID] = session //Set the value in the cache so pacquets are routed fast
			userCache[session.userID] = messageReceived.SocketID
			log.Printf("[Auth] -> Connection made socket:%s userid:%s", messageReceived.SocketID, session.userID)
			sendAuthResponse(true, messageReceived.SocketID)
			cbroadcast.Broadcast(socket.BSocketAuthConnected, messageReceived.SocketID) //Broadcast only when the auth is connected
			return true
		}
		sendAuthResponse(false, messageReceived.SocketID)
		return false
	}

	_, ok := sessionCache[messageReceived.SocketID]
	if !ok {
		//Send a fail message any time if the socket is not correct or if the user was forced disconnected
		sendAuthResponse(false, messageReceived.SocketID)
	}
	return ok
}

//GetLang returns the language for the socket
func GetLang(socketID uuid.UUID) int {
	if session, ok := sessionCache[socketID]; ok {
		return session.lang
	}
	return 0
}

//GetUser returns the user associated with a session
func GetUser(socketID uuid.UUID) (model.User, error) {
	var session model.Session
	var user model.User
	model.DB().Where("socket_id = ?", socketID).First(&session)
	model.DB().Model(&session).Related(&user)
	if user.ID != uuid.Nil {
		return user, nil
	}
	return model.User{}, fmt.Errorf("No user is associated with this connection")
}

//GetUserID returns the user id associated with a session
func GetUserID(socketID uuid.UUID) (uuid.UUID, error) {
	defer mutex.Unlock()
	mutex.Lock()

	if session, ok := sessionCache[socketID]; ok {
		return session.userID, nil
	}
	return uuid.Nil, fmt.Errorf("No user is associated with this connection")
}

//ChangeLang change the language of the sessionCache
func ChangeLang(socketID uuid.UUID, lang int) {
	defer mutex.Unlock()
	mutex.Lock()
	if _, ok := sessionCache[socketID]; ok && sessionCache[socketID].lang != lang {
		sessionVar := sessionCache[socketID]
		sessionVar.lang = lang
		sessionCache[socketID] = sessionVar
	}
}

//GetSocketID returns the user id associated with a session
func GetSocketID(userID uuid.UUID) (uuid.UUID, error) {
	defer mutex.Unlock()
	mutex.Lock()

	if socketID, ok := userCache[userID]; ok {
		return socketID, nil
	}
	return uuid.Nil, fmt.Errorf("No socketID is associated with this userID")
}

//HasUserSession returns true if the user has a session
func HasUserSession(userID uuid.UUID) bool {
	defer mutex.Unlock()
	mutex.Lock()

	return hasUserSession(userID)
}

//hasUserSession without a deadlock
func hasUserSession(userID uuid.UUID) bool {
	_, ok := userCache[userID]
	return ok
}

//HasUserToken returns true if the user has already a token issued
func HasUserToken(userID uuid.UUID) (bool, string) {
	defer mutex.Unlock()
	mutex.Lock()

	for k, v := range tokenAvailable {
		if v.userID == userID {
			return true, k
		}
	}
	return false, ""
}

//IsAuthenticatedQuick returns only if the socket is in the cache of authenticated sockets
func IsAuthenticatedQuick(socketID uuid.UUID) bool {
	defer mutex.Unlock()
	mutex.Lock()

	_, ok := sessionCache[socketID]
	return ok
}

func sendAuthResponse(response bool, socketID uuid.UUID) {
	message := socket.RawMessage{}
	message.MessageType = byte(socket.MessageType.ServerConnectionResponse)
	message.Length = 1
	if response {
		message.Bytes = []byte{0x01}
	} else {
		message.Bytes = []byte{0x00}
	}
	socket.SendRawMessageToSocketID(message, socketID)
}
