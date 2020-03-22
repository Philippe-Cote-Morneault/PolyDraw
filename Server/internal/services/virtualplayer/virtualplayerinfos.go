package virtualplayer

import (
	"log"
	"math/rand"
	"strings"
	"time"

	"gitlab.com/jigsawcorp/log3900/internal/services/messenger"
	"gitlab.com/jigsawcorp/log3900/pkg/cbroadcast"

	"github.com/Pallinder/go-randomdata"
	"github.com/google/uuid"
)

type virtualPlayerInfos struct {
	BotID             uuid.UUID
	GroupID           uuid.UUID
	Personality       string
	DrawingTimeFactor float64
	Username          string
}

func (v *virtualPlayerInfos) calculateDrawingTime() {
	rand.Seed(time.Now().UnixNano())
	min := 0.0
	max := 1.0
	switch v.Personality {
	case "angry":
		min = 0.9
		max = 1.4

	case "funny":
		min = 0.6
		max = 1.2

	case "mean":
		min = 1.5
		max = 2

	case "nice":
		min = 0.5
		max = 0.7

	case "supportive":
		min = 0.6
		max = 0.8
	}
	v.DrawingTimeFactor = float64(min) + rand.Float64()*float64(max-min)

}

func generateVirtualPlayer() *virtualPlayerInfos {
	v := &virtualPlayerInfos{Personality: []string{"angry", "funny", "mean", "nice", "supportive"}[rand.Intn(5)],
		DrawingTimeFactor: 0, Username: randomdata.GenerateProfile(randomdata.RandomGender).Login.Username}

	v.calculateDrawingTime()

	return v
}

func (v *virtualPlayerInfos) speak(channelID uuid.UUID, interactionType string) {
	log.Println("[Virtual Player] -> speak()")
	interaction := v.getInteraction(interactionType)
	log.Printf("[Virtual Player] -> getInteraction() returns = %v", interaction)

	cbroadcast.Broadcast(messenger.BBotMessage, messenger.MessageReceived{
		ChannelID: channelID.String(),
		UserID:    v.BotID.String(),
		Username:  v.Username,
		Message:   interaction,
		Timestamp: time.Now().Unix(),
	})
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

func (v *virtualPlayerInfos) getInteraction(interactionType string) string {

	lines := getLines(interactionType)

	switch v.Personality {
	case "angry":
		return strings.ReplaceAll(lines.Angry[rand.Intn(3)], "{}", randomUsername(v.GroupID))

	case "funny":
		return strings.ReplaceAll(lines.Funny[rand.Intn(3)], "{}", randomUsername(v.GroupID))

	case "mean":
		return strings.ReplaceAll(lines.Mean[rand.Intn(3)], "{}", randomUsername(v.GroupID))

	case "nice":
		return strings.ReplaceAll(lines.Nice[rand.Intn(3)], "{}", randomUsername(v.GroupID))

	case "supportive":
		return strings.ReplaceAll(lines.Supportive[rand.Intn(3)], "{}", randomUsername(v.GroupID))

	default:
		log.Println("[Virtual Player] -> [Error] Bot's personnality doesn't exists. Aborting interaction...")
		return ""
	}
}
