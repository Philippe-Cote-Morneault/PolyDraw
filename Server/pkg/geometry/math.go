package geometry

import "math"

type legendreGauss struct {
	w float64
	x float64
}

var quadratureTable map[int]legendreGauss

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
func EucledianDist(a *Point, b *Point) float64 {
	return math.Sqrt(math.Pow(float64(a.X-b.X), 2) + math.Pow(float64(a.Y-b.Y), 2))
}

//BezierLength calculates the length of a bezier curve
func BezierLength(start *Point, c1 *Point, c2 *Point, end *Point) float64 {
	intShift := 1 / 2.0
	sum := 0.0
	for _, v := range quadratureTable {
		sum += v.w * bezierF(intShift*v.x+intShift, start, c1, c2, end)
	}
	return intShift * sum
}

func bezierF(t float64, start *Point, c1 *Point, c2 *Point, end *Point) float64 {
	result := 3 * math.Sqrt(
		math.Pow(math.Pow(-1+t, 2)*float64(start.X)+(-1+(4-3*t)*t)*float64(c1.X)+
			t*((-2+3*t)*float64(c2.X)-t*float64(end.X)), 2)+

			math.Pow(math.Pow(-1+t, 2)*float64(start.Y)+(-1+(4-3*t)*t)*float64(c1.Y)+
				t*((-2+3*t)*float64(c2.Y)-t*float64(end.Y)), 2),
	)
	return result
}
