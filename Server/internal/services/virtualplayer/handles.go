package virtualplayer

import (
	"log"
	"math/rand"
	"sync"

	"github.com/google/uuid"
	"gitlab.com/jigsawcorp/log3900/internal/socket"
)

var managerInstance *Manager

type virtualPlayerInfos struct {
	PlayerID          uuid.UUID
	Username          string
	Personnality      string
	DrawingTime       int
	LastStartLine     byte
	LastEndLine       byte
	LastReferenceLine byte
	LastRatioLine     byte
	LastHintLine      byte
}

type group struct {
	id              string
	playerUsernames []string
}

// Manager represents a struct that manage all virtual players
type Manager struct {
	mutex    sync.Mutex
	Players  map[uuid.UUID]*virtualPlayerInfos // playerID -> virtualPlayerInfos
	Channels map[uuid.UUID][]uuid.UUID         //channelID -> []playerID
	Games    map[uuid.UUID]*group              //channelID -> group
}

func (m *Manager) init() {
	m.Players = make(map[uuid.UUID]*virtualPlayerInfos)
	m.Channels = make(map[uuid.UUID][]uuid.UUID)
	m.Games = make(map[uuid.UUID]*group)
}

func (v *VirtualPlayer) addVirtualPlayer(message socket.RawMessageReceived) {

}

func hasVirtualPlayer(playerID uuid.UUID) bool {
	_, ok := managerInstance.Players[playerID]
	return ok
}

func (v *VirtualPlayer) kickVirtualPlayer(message socket.RawMessageReceived) {
	log.Println("In kickVirtualPlayer")
}

func generateVirtualPlayer() *virtualPlayerInfos {
	choice := rand.Intn(5)

	return &virtualPlayerInfos{Personnality: []string{"angry", "funny", "mean", "nice", "supportive"}[choice],
		Username:    "",
		DrawingTime: -1, LastStartLine: 0, LastEndLine: 0, LastReferenceLine: 0, LastRatioLine: 0, LastHintLine: 0}
}

func speak(line string) {

}
