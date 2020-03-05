package match

import (
	"log"
	"sync"

	"github.com/google/uuid"
	"gitlab.com/jigsawcorp/log3900/internal/services/match/mode"
	"gitlab.com/jigsawcorp/log3900/internal/socket"
	"gitlab.com/jigsawcorp/log3900/model"
)

type matchManager struct {
	matches    map[uuid.UUID]IMatch    //group id
	assignment map[uuid.UUID]uuid.UUID //socket id -> game id
}

var matchManagerInstance *matchManager

//UpgradeGroup method exposed to convert a group to a new game
func UpgradeGroup(groupID uuid.UUID, connections []uuid.UUID, gameInfo model.Group) {
	if matchManagerInstance != nil {
		matchManagerInstance.StartGame(groupID, connections, gameInfo)
	}
}

//Init used to initialize the service match manager
func (m *matchManager) Init() {
	matchManagerInstance = m

	m.matches = make(map[uuid.UUID]IMatch)
	m.assignment = make(map[uuid.UUID]uuid.UUID)
}

//StartGame start the games with the current players in the group
func (m *matchManager) StartGame(groupID uuid.UUID, connections []uuid.UUID, gameInfo model.Group) {
	var match IMatch
	switch gameInfo.GameType {
	case 0:
		match = &mode.FFA{}
	}
	//TODO add other game mode here!

	if match != nil {
		match.Init(connections, gameInfo)
		m.matches[groupID] = match

		m.sendWelcome(groupID) //Send welcome message to all the users

		go match.Start() //Start the match in its own thread
	}

}

func (m *matchManager) Ready(socketID uuid.UUID) {
	if groupID, ok := m.assignment[socketID]; ok {
		m.matches[groupID].Ready(socketID)
	} else {
		log.Printf("[Match] -> Socket not registered to any game | %s", socketID.String())
	}
}

//Send a welcome message to the users of a match
func (m *matchManager) sendWelcome(groupID uuid.UUID) {
	message := m.matches[groupID].GetWelcome()
	connections := m.matches[groupID].GetConnections()
	for i := range connections {
		go socket.SendRawMessageToSocketID(message, connections[i]) //In parallel because this message is not determinist
		m.assignment[connections[i]] = groupID
	}
	log.Printf("[Match] -> Welcome sent to the match | %s", groupID.String())

}

//Guess used when the client wants to guess a word
func (m *matchManager) Guess(message socket.RawMessageReceived) {
	if groupID, ok := m.assignment[message.SocketID]; ok {
		m.matches[groupID].TryWord(message.SocketID, string(message.Payload.Bytes))
	} else {
		log.Printf("[Match] -> Socket not registered to any game | %s", message.SocketID.String())
	}
}

//Hint used when the client wants to have a hint for the word
func (m *matchManager) Hint(socketID uuid.UUID) {
	if groupID, ok := m.assignment[socketID]; ok {
		m.matches[groupID].HintRequested(socketID)
	} else {
		log.Printf("[Match] -> Socket not registered to any game | %s", socketID.String())
	}
}

//Quit quits the match
func (m *matchManager) Quit(socketID uuid.UUID) {
	if groupID, ok := m.assignment[socketID]; ok {
		m.matches[groupID].Disconnect(socketID)
		delete(m.assignment, socketID)
	} else {
		log.Printf("[Match] -> Socket not registered to any game | %s", socketID.String())
	}
}

//Close method used to close all the games currently
func (m *matchManager) Close() {
	wg := sync.WaitGroup{}
	wg.Add(len(m.matches))
	for k := range m.matches {
		m.closeMatch(&wg, k)
	}
	wg.Wait()
}

func (m *matchManager) closeMatch(wg *sync.WaitGroup, groupID uuid.UUID) {
	m.matches[groupID].Close()
	wg.Done()
}
