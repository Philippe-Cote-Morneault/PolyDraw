package match

import (
	"github.com/google/uuid"
	"gitlab.com/jigsawcorp/log3900/model"
)

//BGameStarts message to indicate when game starts
const BGameStarts = "drawing:gamestarts"

//BGameEnds message to indicate when game ends
const BGameEnds = "drawing:gameEnds"

//BRoundStarts message to indicate when round starts
const BRoundStarts = "drawing:roundstarts"

//BRoundEnds message to indicate when round ends
const BRoundEnds = "drawing:roundends"

//BChatNew message used to inform the virtual player of the new chat channel id
const BChatNew = "drawing:chatnew"

//BSize buffer size for the drawing service
const BSize = 5

//Player represent a player for in channels communications
type Player struct {
	IsCPU    bool
	Username string
	ID       uuid.UUID
}

//RoundStart broadcast when the round start
type RoundStart struct {
	MatchID uuid.UUID
	Drawer  Player
	Game    *model.Game
}

//HintRequested broadcast when a user wants a hint
type HintRequested struct {
	MatchID  uuid.UUID
	SocketID uuid.UUID
	Player   Player
}

//ChatNew broadcast when there is a new chat channel for the match
type ChatNew struct {
	MatchID uuid.UUID
	ChatID  uuid.UUID
}

// BotInfos is used to give information to match service
type BotInfos struct {
	BotID    uuid.UUID
	Username string
}
