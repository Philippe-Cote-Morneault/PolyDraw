package drawing

import (
	"github.com/google/uuid"
	"github.com/tevino/abool"
	geometry "gitlab.com/jigsawcorp/log3900/pkg/geometry/model"
)

// Draw specifications that contains the informations of the sketch
type Draw struct {
	SVGFile           string
	DrawingTimeFactor float64
	Mode              int
}

// DrawState gives the state of drawing
type DrawState struct {
	StopDrawing *abool.AtomicBool
	Time        float64
}

//Stroke represent a stroke to be drawn on the client canvas
type Stroke struct {
	ID        uuid.UUID
	color     byte
	isEraser  bool
	isSquared bool
	brushSize byte
	points    []geometry.Point
}
