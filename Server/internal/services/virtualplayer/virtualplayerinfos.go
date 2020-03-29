package virtualplayer

import (
	"log"
	"math/rand"
	"strconv"
	"strings"
	"time"

	"gitlab.com/jigsawcorp/log3900/internal/language"
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
	Language          int
}

func (v *virtualPlayerInfos) calculateDrawingTime() {
	rand.Seed(time.Now().UnixNano())
	min := 0.0
	max := 1.0
	switch v.Personality {
	case "Angry":
		min = 0.9
		max = 1.4

	case "Funny":
		min = 0.6
		max = 1.2

	case "Mean":
		min = 1.5
		max = 2

	case "Nice":
		min = 0.5
		max = 0.7

	case "Supportive":
		min = 0.6
		max = 0.8
	}
	v.DrawingTimeFactor = float64(min) + rand.Float64()*float64(max-min)

}

func generateVirtualPlayer(lang int) *virtualPlayerInfos {
	v := &virtualPlayerInfos{Language: lang, Personality: []string{"Angry", "Funny", "Mean", "Nice", "Supportive"}[rand.Intn(5)],
		DrawingTimeFactor: 0, Username: randomdata.GenerateProfile(randomdata.RandomGender).Login.Username}

	v.calculateDrawingTime()

	return v
}

func (v *virtualPlayerInfos) speak(channelID uuid.UUID, interactionType string) {
	log.Println("[VirtualPlayer] -> speak()")
	interaction := v.getInteraction(interactionType)
	log.Printf("[VirtualPlayer] -> getInteraction() returns = %v", interaction)

	cbroadcast.Broadcast(messenger.BBotMessage, messenger.MessageReceived{
		ChannelID: channelID.String(),
		UserID:    v.BotID.String(),
		Username:  v.Username,
		Message:   interaction,
		Timestamp: time.Now().Unix(),
	})
}

func (v *virtualPlayerInfos) getInteraction(interactionType string) string {

	lineNumber := strconv.Itoa(rand.Intn(2) + 1)
	line := language.MustGet("botlines."+interactionType+v.Personality+lineNumber, v.Language)

	if interactionType == "playerRef" || interactionType == "winRatio" {
		line = strings.ReplaceAll(line, "{}", randomUsername(v.GroupID))
	}

	return line
}
