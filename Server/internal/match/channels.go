package match

import (
	"github.com/google/uuid"
	"gitlab.com/jigsawcorp/log3900/model"
)

//BGameStarts message to indicate when game starts
const BGameStarts = "virtualplayer:gamestarts"

//BGameEnds message to indicate when game ends
const BGameEnds = "virtualplayer:gameEnds"

//BRoundStarts message to indicate when round starts
const BRoundStarts = "virtualplayer:roundstarts"

//BRoundEnds message to indicate when round ends
const BRoundEnds = "virtualplayer:roundends"

//BChatNew message used to inform the virtual player of the new chat channel id
const BChatNew = "virtualplayer:chatnew"

//BPlayerLeft message used to inform the virtual player that player left
const BPlayerLeft = "virtualplayer:playerleft"

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
	DrawerID uuid.UUID
	GameType int
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

// StatsData contains matches infos to send to stats service
type StatsData struct {
	Match     *model.MatchPlayed
	SocketsID []uuid.UUID
}
