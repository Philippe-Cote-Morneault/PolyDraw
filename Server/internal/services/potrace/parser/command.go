package parser

import "unicode"

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
	}
}

func (c *Command) parseParam(lastPoint *Point, size int, offsetX int, offsetY int) {
	//Check if multiple of 6
	if len(c.Parameters)%size == 0 {
		c.StartPos = Point{lastPoint.X, lastPoint.Y}

		if unicode.IsUpper(c.Command) {
			//Just take the last position
			c.EndPos = Point{}
			if offsetX >= 0 {
				c.EndPos.X = c.Parameters[offsetX]
			}
			if offsetY >= 0 {
				c.EndPos.Y = c.Parameters[offsetY]
			}
		} else {
			//We need to compute every node to get the last point
			currentPoint := Point{X: lastPoint.X, Y: lastPoint.Y}
			for i := range c.Parameters {
				if offsetX >= 0 {
					currentPoint.X += c.Parameters[i*size+offsetX]
				}
				if offsetY >= 0 {
					currentPoint.Y += c.Parameters[i*size+offsetY]
				}
			}
			c.EndPos = currentPoint
		}
	} else {
		//TODO Error
	}
}
