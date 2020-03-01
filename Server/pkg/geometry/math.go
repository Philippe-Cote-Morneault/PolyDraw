package geometry

import (
	"math"

	"gitlab.com/jigsawcorp/log3900/pkg/geometry/model"
)

type legendreGauss struct {
	w float64
	x float64
}

var quadratureTable map[int]legendreGauss

//InitTable of Legendre-Gauss in memory
func InitTable() {
	quadratureTable = map[int]legendreGauss{
		1: {0.5688888888888889, 0.0000000000000000},
		2: {0.4786286704993665, -0.5384693101056831},
		3: {0.4786286704993665, 0.5384693101056831},
		4: {0.2369268850561891, -0.9061798459386640},
		5: {0.2369268850561891, 0.9061798459386640},
	}
}

//EucledianDist calculates the distance between two points
func EucledianDist(a *model.Point, b *model.Point) float64 {
	return math.Sqrt(math.Pow(float64(a.X-b.X), 2) + math.Pow(float64(a.Y-b.Y), 2))
}

//BezierLength calculates the length of a bezier curve
func BezierLength(start *model.Point, c1 *model.Point, c2 *model.Point, end *model.Point) float64 {
	intShift := 1 / 2.0
	sum := 0.0
	for _, v := range quadratureTable {
		sum += v.w * bezierF(intShift*v.x+intShift, start, c1, c2, end)
	}
	return intShift * sum
}

func bezierF(t float64, start *model.Point, c1 *model.Point, c2 *model.Point, end *model.Point) float64 {
	result := 3 * math.Sqrt(
		pow2(pow2(-1+t)*float64(start.X)+(-1+(4-3*t)*t)*float64(c1.X)+
			t*((-2+3*t)*float64(c2.X)-t*float64(end.X)))+

			pow2(pow2(-1+t)*float64(start.Y)+(-1+(4-3*t)*t)*float64(c1.Y)+
				t*((-2+3*t)*float64(c2.Y)-t*float64(end.Y))),
	)
	return result
}

func pow2(a float64) float64 {
	return a * a
}
