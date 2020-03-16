package match

import "gitlab.com/jigsawcorp/log3900/pkg/cbroadcast"

//BMatchReady send the ready signal to the server
const BMatchReady = "match:ready"

//BMatchQuit client can use this message to advertise its going to leave the game
const BMatchQuit = "match:quit"

//BMatchGuess use to guess a word
const BMatchGuess = "match:guess"

//BMatchHint request a hint for the server
const BMatchHint = "match:hint"

//BSize buffer size for the messenger service
const BSize = 5

//Register the broadcast for messenger
func (s *Service) Register() {
	cbroadcast.Register(BMatchReady, BSize)
	cbroadcast.Register(BMatchQuit, BSize)
	cbroadcast.Register(BMatchGuess, BSize)
	cbroadcast.Register(BMatchHint, BSize)
}
