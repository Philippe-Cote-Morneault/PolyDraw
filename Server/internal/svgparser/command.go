package svgparser

import (
	"log"
	"strconv"
	"strings"
	"unicode"
)

//Command represents a command of the D string
type Command struct {
	Command    rune
	StartPos   Point
	EndPos     Point
	Parameters []float32
}

//Parse is used to validate if the command is ok and to keep track of the current point
func (c *Command) Parse(lastPoint *Point) {
	commandLower := unicode.ToLower(c.Command)
	switch commandLower {
	case 'c':
		c.parseParam(lastPoint, 6, 4, 5)
	case 'm':
		c.parseParam(lastPoint, 2, 0, 1)
	case 'l':
		c.parseParam(lastPoint, 2, 0, 1)
	case 'h':
		c.parseParam(lastPoint, 1, -1, 0)
	case 'v':
		c.parseParam(lastPoint, 1, 0, -1)
	case 'z':
		c.StartPos = Point{lastPoint.X, lastPoint.Y}
		c.EndPos = Point{lastPoint.X, lastPoint.Y}
	//TODO do the things that are horrible
	/*
		case 's':
			c.parseS(lastPoint)
		case 'q':
			c.parseQ(lastPoint)
		case 't':
			c.parseT(lastPoint)
		case 'a':
			c.parseA(lastPoint)
	*/
	default:
		log.Printf("[Potrace] -> Format contains invalid command \"%c\"", c.Command)
	}
}

func (c *Command) parseParam(lastPoint *Point, size int, offsetX int, offsetY int) {
	lenParams := len(c.Parameters)
	if lenParams%size == 0 {
		c.StartPos = Point{lastPoint.X, lastPoint.Y}

		if unicode.IsUpper(c.Command) {
			//Just take the last position
			c.EndPos = Point{}

			if offsetX >= 0 {
				c.EndPos.X = c.Parameters[lenParams-size+offsetX]
			} else {
				c.EndPos.X = lastPoint.X
			}
			if offsetY >= 0 {
				c.EndPos.Y = c.Parameters[lenParams-size+offsetY]
			} else {
				c.EndPos.Y = lastPoint.Y
			}
		} else {
			//We need to compute every node to get the last point
			currentPoint := Point{X: lastPoint.X, Y: lastPoint.Y}
			for i := 0; i < lenParams; i += size {
				if offsetX >= 0 {
					currentPoint.X += c.Parameters[i+offsetX]
				}
				if offsetY >= 0 {
					currentPoint.Y += c.Parameters[i+offsetY]
				}
			}
			c.EndPos = currentPoint
		}
	} else {
		//TODO Error
	}
}

//Encode encodes the command to the new svg type
func (c *Command) Encode() string {
	builder := strings.Builder{}
	builder.Grow(32)
	if c.Command == 'm' {
		builder.WriteString("M ")

		builder.WriteString(strconv.FormatFloat(float64(c.EndPos.X), 'f', -1, 32))
		builder.WriteRune(' ')
		builder.WriteString(strconv.FormatFloat(float64(c.EndPos.Y), 'f', -1, 32))
		builder.WriteRune(' ')
		return builder.String()
	}
	builder.WriteRune(c.Command)
	builder.WriteRune(' ')
	for i := range c.Parameters {
		builder.WriteString(strconv.FormatFloat(float64(c.Parameters[i]), 'f', -1, 32))
		builder.WriteRune(' ')
	}
	return builder.String()

}
