package drawing

import (
	"github.com/google/uuid"
	geometry "gitlab.com/jigsawcorp/log3900/pkg/geometry/model"
)

// Draw specifications that contains the informations of the sketch
type Draw struct {
	SVGFile     string
	DrawingTime float64
	Mode        int
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
