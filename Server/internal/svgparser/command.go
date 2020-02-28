package svgparser

import (
	"gitlab.com/jigsawcorp/log3900/pkg/geometry"
	"log"
	"math"
	"strconv"
	"strings"
	"unicode"
)

//Command represents a command of the D string
type Command struct {
	Command    rune
	StartPos   geometry.Point
	EndPos     geometry.Point
	Parameters []float32
}

//Parse is used to validate if the command is ok and to keep track of the current point
func (c *Command) Parse(lastPoint *geometry.Point, transformations *[]TransformCommand) {
	c.transform(transformations)
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
		c.StartPos = geometry.Point{lastPoint.X, lastPoint.Y}
		c.EndPos = geometry.Point{lastPoint.X, lastPoint.Y}
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
func (c *Command) transform(transformations *[]TransformCommand) {
	commandLower := unicode.ToLower(c.Command)
	for i := range *transformations {
		for j := range c.Parameters {
			if commandLower == 'h' {
				c.Parameters[j] = (*transformations)[i].Apply(c.Command, c.Parameters[j], true)
			} else if commandLower == 'v' {
				c.Parameters[j] = (*transformations)[i].Apply(c.Command, c.Parameters[j], false)
			} else {
				if j%2 == 0 {
					c.Parameters[j] = (*transformations)[i].Apply(c.Command, c.Parameters[j], true)
				} else {
					c.Parameters[j] = (*transformations)[i].Apply(c.Command, c.Parameters[j], false)
				}
			}
		}

	}
}

func (c *Command) parseParam(lastPoint *geometry.Point, size int, offsetX int, offsetY int) {
	lenParams := len(c.Parameters)
	if lenParams%size == 0 {
		c.StartPos = geometry.Point{lastPoint.X, lastPoint.Y}

		if unicode.IsUpper(c.Command) {
			//Just take the last position
			c.EndPos = geometry.Point{}

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
			currentPoint := geometry.Point{X: lastPoint.X, Y: lastPoint.Y}
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

//ComputeLength calculates the length of path of the commands used by potrace only
func (c *Command) ComputeLength() float64 {
	cLowerCase := unicode.ToLower(c.Command)
	switch cLowerCase {
	case 'l':
		length := 0.0
		current := geometry.Point{}
		if unicode.IsLower(c.Command) {
			for i := 0; i < len(c.Parameters); i += 2 {
				length += geometry.EucledianDist(&current, &geometry.Point{X: c.Parameters[i], Y: c.Parameters[i+1]})
			}
			return length
		}

		current.X = c.StartPos.X
		current.Y = c.StartPos.Y
		for i := 0; i < len(c.Parameters); i += 2 {
			length += geometry.EucledianDist(&current, &geometry.Point{X: c.Parameters[i], Y: c.Parameters[i+1]})
			current.X = c.Parameters[i]
			current.X = c.Parameters[i+1]
		}
		return length
	case 'h':
		return math.Abs(float64(c.EndPos.X - c.StartPos.X))
	case 'v':
		return math.Abs(float64(c.EndPos.Y - c.StartPos.Y))
	case 'c':
		length := 0.0
		current := geometry.Point{}
		if unicode.IsLower(c.Command) {
			for i := 0; i < len(c.Parameters); i += 6 {
				length += geometry.BezierLength(&current,
					&geometry.Point{X: c.Parameters[i], Y: c.Parameters[i+1]},
					&geometry.Point{X: c.Parameters[i+2], Y: c.Parameters[i+3]},
					&geometry.Point{X: c.Parameters[i+4], Y: c.Parameters[i+5]},
				)
			}
			return length
		}

		current.X = c.StartPos.X
		current.Y = c.StartPos.Y
		for i := 0; i < len(c.Parameters); i += 6 {
			length += geometry.BezierLength(&current,
				&geometry.Point{X: c.Parameters[i], Y: c.Parameters[i+1]},
				&geometry.Point{X: c.Parameters[i+2], Y: c.Parameters[i+3]},
				&geometry.Point{X: c.Parameters[i+4], Y: c.Parameters[i+5]},
			)
			current.X = c.Parameters[i+4]
			current.X = c.Parameters[i+5]
		}
		return length
	}
	return 0
}
