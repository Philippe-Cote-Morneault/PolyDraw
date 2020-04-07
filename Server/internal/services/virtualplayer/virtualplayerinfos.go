package virtualplayer

import (
	"fmt"
	"log"
	"math"
	"strconv"
	"strings"
	"time"

	"gitlab.com/jigsawcorp/log3900/internal/services/auth"

	"gitlab.com/jigsawcorp/log3900/internal/match"
	"gitlab.com/jigsawcorp/log3900/internal/services/stats"
	"gitlab.com/jigsawcorp/log3900/model"

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

const sendStatsDelay = 60 //Seconds

func (v *virtualPlayerInfos) calculateDrawingTime() {
	resetRandSeed()

	min := 0.0
	max := 1.0
	switch v.Personality {
	case "Angry":
		min = 0.9
		max = 1.3

	case "Funny":
		min = 0.6
		max = 1.2

	case "Mean":
		min = 1.3
		max = 1.4

	case "Nice":
		min = 0.5
		max = 0.7

	case "Supportive":
		min = 0.7
		max = 0.9
	}
	v.DrawingTimeFactor = float64(min) + managerInstance.rand.Float64()*float64(max-min)
}

func generateVirtualPlayer(lang int) *virtualPlayerInfos {
	resetRandSeed()

	v := &virtualPlayerInfos{Language: lang, Personality: []string{"Angry", "Funny", "Mean", "Nice", "Supportive"}[managerInstance.rand.Intn(5)],
		DrawingTimeFactor: 0, Username: randomdata.GenerateProfile(randomdata.RandomGender).Login.Username}

	v.calculateDrawingTime()

	return v
}

func (v *virtualPlayerInfos) speak(channelID uuid.UUID, line string) {
	cbroadcast.Broadcast(messenger.BBotMessage, messenger.MessageReceived{
		ChannelID: channelID.String(),
		UserID:    v.BotID.String(),
		Username:  v.Username,
		Message:   line,
		Timestamp: time.Now().Unix(),
	})
}

func (v *virtualPlayerInfos) getInteraction(interactionType string) string {
	resetRandSeed()

	lineNumber := strconv.Itoa(managerInstance.rand.Intn(2) + 1)
	line := language.MustGet("botlines."+interactionType+v.Personality+lineNumber, v.Language)

	return line
}

func (v *virtualPlayerInfos) sendStatsInteraction(groupID uuid.UUID, match *match.IMatch) {
	resetRandSeed()

	interactionType := "playerRef"

	if (*match).GetType() == 0 {
		interactionType = "winRatio"
	}

	lineNumber := strconv.Itoa(managerInstance.rand.Intn(2) + 1)
	line := language.MustGet("botlines."+interactionType+v.Personality+lineNumber, v.Language)

	userID := getRandomUserID(groupID)
	if userID == uuid.Nil {
		return
	}
	var user model.User
	model.DB().Model(&model.User{}).Where("id = ?", userID).First(&user)
	userStats, _ := stats.GetStats(userID)

	line = strings.ReplaceAll(line, "{}", user.Username)
	if interactionType == "winRatio" {
		winRatio := fmt.Sprintf("%.2f", userStats.WinRatio)
		line = strings.ReplaceAll(line, "[]", winRatio)
	} else {
		line = strings.ReplaceAll(line, "[]", secondsToHuman(userStats.TimePlayed))
	}

	channelID, ok := managerInstance.Channels[groupID]

	if !ok {
		log.Printf("[VirtualPlayer -> [Error] Can't find channelID with groupID : %v. Aborting sendStatsInteraction...", groupID)
	}
	v.speak(channelID, line)
}

func getRandomUserID(groupID uuid.UUID) uuid.UUID {
	if match, ok := managerInstance.Matches[groupID]; ok {
		connections := (*match).GetConnections()
		if len(connections) > 0 {
			i := managerInstance.rand.Intn(len(connections))
			userID, isOk := auth.GetUserID(connections[i])
			if isOk != nil {
				log.Printf("[VirtualPlayer] [Error] Can't find userID of in game socketID : %v", userID)
				return uuid.Nil
			}
			return userID
		}
		log.Printf("[VirtualPlayer] [Error] No connections for getRandomUserID")

		return uuid.Nil
	}
	return uuid.Nil
}

func statsLinesLoop(groupID uuid.UUID) {
	log.Println("[VirtualPlayer] Starting stats loop")
	count := 0
	for count < 5 {
		time.Sleep(sendStatsDelay * time.Second)

		managerInstance.mutex.Lock()
		match, ok := managerInstance.Matches[groupID]
		if !ok {
			managerInstance.mutex.Unlock()
			log.Println("[VirtualPlayer] Stopping statLinesLoop")
			return
		}

		log.Println("[VirtualPlayer] Sending stat interaction")
		for _, bot := range managerInstance.Bots {
			go bot.sendStatsInteraction(groupID, match)
			break
		}
		managerInstance.mutex.Unlock()

		count++
	}
}

func resetRandSeed() {
	managerInstance.rand.Seed(time.Now().UnixNano())
}

func stringify(count int64, unit string) string {
	return fmt.Sprintf("%v %v ", count, unit)
}

func secondsToHuman(input int64) string {
	input = input / 1000
	hours := math.Floor(float64(input) / 60 / 60)
	seconds := input % (60 * 60)
	minutes := math.Floor(float64(seconds) / 60)
	seconds = input % 60
	result := ""

	if hours > 0 {
		result = stringify(int64(hours), "h") + stringify(int64(minutes), "min") + stringify(int64(seconds), "s")
	} else if minutes > 0 {
		result = stringify(int64(minutes), "min") + stringify(int64(seconds), "s")
	} else {
		result = stringify(int64(seconds), "s")
	}

	return result
}
