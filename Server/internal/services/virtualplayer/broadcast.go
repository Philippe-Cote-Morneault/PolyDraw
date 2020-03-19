package virtualplayer

import "gitlab.com/jigsawcorp/log3900/pkg/cbroadcast"

//BAddPlayer message to indicate to add virtual player
const BAddPlayer = "drawing:addplayer"

//BKickPlayer message to indicate to kick virtual player
const BKickPlayer = "drawing:kickplayer"

//BGameStarts message to indicate when game starts
const BGameStarts = "drawing:gamestarts"

//BRoundEnds message to indicate when round ends
const BRoundEnds = "drawing:roundends"

//BAskHint message to ask for a hint
const BAskHint = "drawing:askhint"

//BSize buffer size for the drawing service
const BSize = 5

//Register the broadcast for drawing
func (v *VirtualPlayer) Register() {
	cbroadcast.Register(BAddPlayer, BSize)
	cbroadcast.Register(BKickPlayer, BSize)
	cbroadcast.Register(BGameStarts, BSize)
	cbroadcast.Register(BRoundEnds, BSize)
	cbroadcast.Register(BAskHint, BSize)
}
