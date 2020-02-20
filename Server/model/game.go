package model

import "github.com/google/uuid"

//Game represent a game
type Game struct {
	Base
	Word       string
	Difficulty int
	Hints      []*GameHint
	Image      *GameImage //Represent the hash of the file
}

//GameHint represents a game hint
type GameHint struct {
	Base
	Game   Game `gorm:"foreignkey:GameID"`
	GameID uuid.UUID
	Hint   string
}

//GameImage represents the game image
type GameImage struct {
	Base
	Game      Game `gorm:"foreignkey:GameID"`
	GameID    uuid.UUID
	Mode      int
	SVGFile   string
	ImageFile string
}
