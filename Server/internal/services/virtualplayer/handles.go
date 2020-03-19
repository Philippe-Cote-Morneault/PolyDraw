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
	PlayerID     uuid.UUID
	Personnality string
	DrawingTime  int
	// LastStartLine     byte
	// LastEndLine       byte
	// LastReferenceLine byte
	// LastRatioLine     byte
	// LastHintLine      byte
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
	return &virtualPlayerInfos{Personnality: []string{"angry", "funny", "mean", "nice", "supportive"}[rand.Intn(5)],
		DrawingTime: -1}
}

func speak(socketID uuid.UUID, message string) {
	// sends message by socket
}

func getLines(interactionType string) *lines {
	switch interactionType {
	case "startGame":
		return iStartGameLines
	case "endRound":
		return iEndRoundLines
	case "hint":
		return iHintLines
	default:
		if rand.Intn(2) == 1 {
			return iPlayerRefLines
		}
		return iWinRatioLines
	}
}

func getInteraction(playerID uuid.UUID, interactionType string) string {

	playerInfos, ok := managerInstance.Players[playerID]

	lines := getLines(interactionType)

	if !ok {
		log.Println("[Virtual Player] -> Can't find bot's id. Aborting interaction...")
		return ""
	}

	switch playerInfos.Personnality {
	case "angry":
		return lines.Angry[rand.Intn(3)]

	case "funny":
		return lines.Funny[rand.Intn(3)]

	case "mean":
		return lines.Mean[rand.Intn(3)]

	case "nice":
		return lines.Nice[rand.Intn(3)]

	case "supportive":
		return lines.Supportive[rand.Intn(3)]

	default:
		return ""
	}
}
