package auth

import (
	"fmt"
	"sync"

	"github.com/google/uuid"
	"gitlab.com/jigsawcorp/log3900/internal/socket"
)

var mutex sync.Mutex
var tokenAvailable map[string]uuid.UUID
var sessions map[uuid.UUID]uuid.UUID

//Register the token that the client can use for authentification
func Register(token string, userID uuid.UUID) {
	defer mutex.Unlock()
	mutex.Lock()

	if tokenAvailable == nil {
		tokenAvailable = make(map[string]uuid.UUID)
	}
	tokenAvailable[token] = userID

	//TODO cleanup of unused tokens
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

		if userID, ok := tokenAvailable[token]; ok {
			sessions[messageReceived.SocketID] = userID
			return true
		}
		return false
	}

	_, ok := sessions[messageReceived.SocketID]
	return ok
}

//GetUserID returns the userID associated with a session
func GetUserID(socketID uuid.UUID) (uuid.UUID, error) {
	defer mutex.Unlock()
	mutex.Lock()

	if user, ok := sessions[socketID]; ok {
		return user, nil
	}
	return uuid.Nil, fmt.Errorf("No user is associated with this connection")
}
