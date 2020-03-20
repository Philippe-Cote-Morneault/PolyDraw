package virtualplayer

import (
	"log"
	"math/rand"
	"time"

	"github.com/Pallinder/go-randomdata"
	"github.com/google/uuid"
)

type virtualPlayerInfos struct {
	PlayerID          uuid.UUID
	GroupID           uuid.UUID
	Personnality      string
	DrawingTimeFactor float64
	Username          string
}

func (v *virtualPlayerInfos) calculateDrawingTime() {
	rand.Seed(time.Now().UnixNano())
	switch v.Personnality {
	case "angry":
		min := float64(0.9)
		max := float64(1.4)
		v.DrawingTimeFactor = min + rand.Float64()*(max-min)

	case "funny":
		min := float64(0.6)
		max := float64(1.2)
		v.DrawingTimeFactor = min + rand.Float64()*(max-min)

	case "mean":
		min := float64(1.5)
		max := float64(2)
		v.DrawingTimeFactor = min + rand.Float64()*(max-min)

	case "nice":
		min := float64(0.5)
		max := float64(0.7)
		v.DrawingTimeFactor = min + rand.Float64()*(max-min)

	case "supportive":
		min := float64(0.6)
		max := float64(0.8)
		v.DrawingTimeFactor = min + rand.Float64()*(max-min)
	}
}

func generateVirtualPlayer() *virtualPlayerInfos {
	v := &virtualPlayerInfos{Personnality: []string{"angry", "funny", "mean", "nice", "supportive"}[rand.Intn(5)],
		DrawingTimeFactor: 0, Username: randomdata.GenerateProfile(randomdata.RandomGender).Login.Username}

	v.calculateDrawingTime()

	return v
}

func speak(socketID uuid.UUID, message string) {
	// sends message by socket
}

func getLines(interactionType string) *lines {
	switch interactionType {
	case "startGame":
		return &iStartGameLines
	case "endRound":
		return &iEndRoundLines
	case "hint":
		return &iHintLines
	default:
		if rand.Intn(2) == 1 {
			return &iPlayerRefLines
		}
		return &iWinRatioLines
	}
}

func getInteraction(playerID uuid.UUID, interactionType string) string {

	playerInfos, ok := managerInstance.Bots[playerID]

	lines := getLines(interactionType)

	if !ok {
		log.Printf("[Virtual Player] -> [Error] Can't find bot's id : %v. Aborting interaction...", playerID)
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
		log.Println("[Virtual Player] -> [Error] Bot's personnality doesn't exists. Aborting interaction...")
		return ""
	}
}
