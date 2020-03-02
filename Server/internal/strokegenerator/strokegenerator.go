package strokegenerator

import (
	"log"
	"math"
	"unicode"

	"gitlab.com/jigsawcorp/log3900/internal/svgparser"
	"gitlab.com/jigsawcorp/log3900/pkg/geometry"
	"gitlab.com/jigsawcorp/log3900/pkg/geometry/model"
)

//BezierParams f
type BezierParams struct {
	StartPoint      model.Point
	InfluencePoint1 model.Point
	InfluencePoint2 model.Point
	EndPoint        model.Point
}

var gapPoints float32 = 0.6

func getNbParameters(command rune) int {
	switch command {
	case 'm', 'l':
		return 2
	case 'c':
		return 6
	case 'z':
		return 0
	default:
		log.Printf("(strokegenerator): Format contains invalid command \"%c\"", command)
		return -1
	}
}

func getStartPoint(commands *[]svgparser.Command) model.Point {
	if unicode.ToLower((*commands)[0].Command) != 'm' {
		// TODO : Erreur
		return model.Point{X: 0, Y: 0}
	}
	return model.Point{X: (*commands)[0].Parameters[0], Y: (*commands)[0].Parameters[1]}
}

func generateForLinear(points *[]model.Point, start *model.Point, end *model.Point) {

	if (end.X-start.X) == 0 && (end.Y-start.Y) == 0 {
		return
	}

	slope := calculateSlope(start, end)
	currentX := start.X
	lastX := end.X

	if end.X < currentX {
		currentX = end.X
		lastX = start.X
	}

	if slope == 0 {
		x := currentX
		nbPoints := int(float32(geometry.EucledianDist(start, end)) / gapPoints)

		for i := 0; i < nbPoints; i++ {
			x += gapPoints
			*points = append(*points, model.Point{X: x, Y: start.Y})
		}
		return

	} else if math.IsInf(float64(slope), 0) {
		y := start.Y
		if end.Y < y {
			y = end.Y
		}
		nbPoints := int(float32(geometry.EucledianDist(start, end)) / gapPoints)

		for i := 0; i < nbPoints; i++ {
			y += gapPoints
			*points = append(*points, model.Point{X: start.X, Y: y})
		}
		return
	}

	intercept := start.Y - slope*start.X
	offsetX := float32(math.Cos(math.Atan(float64(slope)))) * gapPoints
	for x := currentX; x < lastX; x += offsetX {
		y := slope*x + intercept
		*points = append(*points, model.Point{X: x, Y: y})
	}
}

func calculateSlope(start *model.Point, end *model.Point) float32 {
	return (start.Y - end.Y) / (start.X - end.X)
}

func generateForBezier(points *[]model.Point, params *BezierParams) {
	interval := getIntervalBezier(params)
	for t := float32(0); t < 1; t += interval {
		*points = append(*points, model.Point{X: evaluateBezier(t, params.StartPoint.X, params.InfluencePoint1.X, params.InfluencePoint2.X, params.EndPoint.X),
			Y: evaluateBezier(t, params.StartPoint.Y, params.InfluencePoint1.Y, params.InfluencePoint2.Y, params.EndPoint.Y)})
	}
}

func evaluateBezier(t, start, infl1, infl2, end float32) float32 {
	return (1-t)*(1-t)*(1-t)*start + 3*t*(1-t)*(1-t)*infl1 + 3*t*t*(1-t)*infl2 + t*t*t*end
}

func getIntervalBezier(params *BezierParams) float32 {
	return gapPoints / float32(geometry.BezierLength(&params.StartPoint, &params.InfluencePoint1, &params.InfluencePoint2, &params.EndPoint))
}

//ExtractPointsStrokes gives stroke containing all points generated
func ExtractPointsStrokes(commands *[]svgparser.Command) []model.Point {
	var points []model.Point
	currentPoint := getStartPoint(commands)
	beginPoint := currentPoint
	isSuccessiveM := false
	for _, command := range *commands {
		commandLower := unicode.ToLower(command.Command)
		if commandLower == 'z' {
			generateForLinear(&points, &currentPoint, &beginPoint)
			currentPoint = beginPoint
			continue
		}
		if len(command.Parameters)%getNbParameters(commandLower) == 0 {
			for i := 0; i < len(command.Parameters); i++ {
				offsetX := float32(0)
				offsetY := float32(0)
				if unicode.IsLower(command.Command) {
					offsetX = currentPoint.X
					offsetY = currentPoint.Y
				}
				switch commandLower {

				case 'm':
					beginPoint = currentPoint
					if isSuccessiveM {
						lastPoint := model.Point{X: command.Parameters[i] + offsetX, Y: command.Parameters[i+1] + offsetY}
						generateForLinear(&points, &currentPoint, &lastPoint)
						currentPoint = lastPoint
					} else {
						currentPoint = model.Point{X: command.Parameters[i] + offsetX, Y: command.Parameters[i+1] + offsetY}
					}
					isSuccessiveM = true
					i++
				case 'l':
					isSuccessiveM = false
					beginPoint = currentPoint
					lastPoint := model.Point{X: command.Parameters[i] + offsetX, Y: command.Parameters[i+1] + offsetY}
					generateForLinear(&points, &currentPoint, &lastPoint)
					currentPoint = lastPoint
					i++
				case 'c':
					isSuccessiveM = false
					beginPoint = currentPoint
					infl1 := model.Point{X: command.Parameters[i] + offsetX, Y: command.Parameters[i+1] + offsetY}
					infl2 := model.Point{X: command.Parameters[i+2] + offsetX, Y: command.Parameters[i+3] + offsetY}
					endPoint := model.Point{X: command.Parameters[i+4] + offsetX, Y: command.Parameters[i+5] + offsetY}
					i += 5
					generateForBezier(&points, &BezierParams{StartPoint: currentPoint, InfluencePoint1: infl1, InfluencePoint2: infl2, EndPoint: endPoint})
					currentPoint = endPoint
				default:
					log.Printf("Format contains invalid command \"%c\"", command.Command)
				}
			}
		} else {
			//TODO Error
		}
	}
	return points
}
