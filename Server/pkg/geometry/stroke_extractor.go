package geometry

import (
	"fmt"
	"log"
	"unicode"
)

type fsm struct {
	Commands []Command
}

//Command css
type Command struct {
	C          rune
	StartPos   Point
	Parameters []float32
}

// Stroke dd
type Stroke struct {
	Points []Point
}

//BezierParams f
type BezierParams struct {
	InfluencePoint1 Point
	InfluencePoint2 Point
	EndPoint        Point
}

func getNbParameters(command rune) int {
	switch command {
	case 'm', 'l':
		return 2
	case 'c':
		return 6
	default:
		log.Printf("[Potrace] -> Format contains invalid command \"%c\"", command)
		return 0
	}
}

func extractPointsStrokes(f fsm) []Stroke {
	var strokes []Stroke
	for iStroke, command := range f.Commands {
		strokes = append(strokes, Stroke{})
		currentPoint := command.StartPos
		commandLower := unicode.ToLower(command.C)
		if len(command.Parameters)%getNbParameters(commandLower) == 0 {
			for i := 0; i < len(command.Parameters); i++ {
				offsetX := float32(0)
				offsetY := float32(0)
				if unicode.IsLower(command.C) {
					offsetX = currentPoint.X
					offsetY = currentPoint.Y
				}
				switch commandLower {
				case 'm', 'l':
					lastPoint := Point{X: command.Parameters[i] + offsetX, Y: command.Parameters[i+1] + offsetY}
					generateForLinear(&strokes[iStroke].Points, &currentPoint, &lastPoint)
					currentPoint = lastPoint
					i++
				case 'c':
					infl2 := Point{X: command.Parameters[i] + offsetX, Y: command.Parameters[i+1] + offsetY}
					infl1 := Point{X: command.Parameters[i+2] + offsetX, Y: command.Parameters[i+3] + offsetY}
					endPoint := Point{X: command.Parameters[i+4] + offsetX, Y: command.Parameters[i+5] + offsetY}
					i += 5
					generateForBezier(&strokes[iStroke].Points, &currentPoint, &BezierParams{InfluencePoint1: infl1, InfluencePoint2: infl2, EndPoint: endPoint})
					currentPoint = endPoint
				default:
					log.Printf("[Potrace] -> Format contains invalid command \"%c\"", command.C)
				}

			}
		} else {
			//TODO Error
		}
	}
	return strokes
}

func generateForLinear(points *[]Point, start *Point, end *Point) {
	fmt.Printf("[Linear] start(%f,%f) et end(%f,%f) : \n", start.X, start.Y, end.X, end.Y)

	grad := calculateGradient(start, end)
	fmt.Printf("Grad : %f\n", grad)
	//TODO choose number of points to divide line according to constant time
	offset := float32((end.X - start.X) / 49.0)
	if offset == 0 {
		return
	}

	fmt.Printf("Offset : %f\n", offset)
	for t := start.X; allPointsGenerated(t, end.X, start.X < end.X); t += offset {
		y := grad*t + (start.Y - grad*start.X)
		*points = append(*points, Point{X: t, Y: y})

	}
	*points = append(*points, *end)

}

func allPointsGenerated(t, end float32, isIncreasing bool) bool {
	if isIncreasing {
		return t <= end
	}
	return t >= end
}

func calculateGradient(start *Point, end *Point) float32 {
	return (start.Y - end.Y) / (start.X - end.X)
}

func generateForBezier(points *[]Point, start *Point, params *BezierParams) {
	for t := float32(0); t < 1; t += 0.02 {
		*points = append(*points, Point{X: evaluateBezier(t, start.X, params.InfluencePoint2.X, params.InfluencePoint1.X, params.EndPoint.X),
			Y: evaluateBezier(t, start.Y, params.InfluencePoint2.Y, params.InfluencePoint1.Y, params.EndPoint.Y)})
	}
}

func evaluateBezier(t, start, infl2, infl1, end float32) float32 {
	return (1-t)*(1-t)*(1-t)*start + 3*t*(1-t)*(1-t)*infl2 + 3*t*t*(1-t)*infl1 + t*t*t*end
}

//GetPointsFromStrokes gives strokes with points generated based on lengths
func GetPointsFromStrokes() []Stroke {
	var commands []Command
	commands = append(commands, Command{C: 'C', StartPos: Point{100, 100}})
	commands[0].Parameters = append(commands[0].Parameters, 75)
	commands[0].Parameters = append(commands[0].Parameters, 80)
	commands[0].Parameters = append(commands[0].Parameters, 125)
	commands[0].Parameters = append(commands[0].Parameters, 20)
	commands[0].Parameters = append(commands[0].Parameters, 150)
	commands[0].Parameters = append(commands[0].Parameters, 150)

	commands[0].Parameters = append(commands[0].Parameters, 75)
	commands[0].Parameters = append(commands[0].Parameters, 80)
	commands[0].Parameters = append(commands[0].Parameters, 125)
	commands[0].Parameters = append(commands[0].Parameters, 20)
	commands[0].Parameters = append(commands[0].Parameters, 150)
	commands[0].Parameters = append(commands[0].Parameters, 50)

	d := fsm{Commands: commands}

	strokes := extractPointsStrokes(d)
	s := strokes[0].Points
	for _, pp := range s {
		fmt.Println(pp)
	}

	return strokes
}
