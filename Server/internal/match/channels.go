package match

import (
	"github.com/google/uuid"
	"gitlab.com/jigsawcorp/log3900/model"
)

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
