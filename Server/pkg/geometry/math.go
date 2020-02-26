package geometry

import "math"

//EucledianDist calculates the distance between two points
func EucledianDist(a *Point, b *Point) float64 {
	return math.Sqrt(math.Pow(float64(a.X-b.X), 2) + math.Pow(float64(a.Y-b.Y), 2))
}

//BezierLength calculates the length of a bezier curve
func BezierLength(start *Point, c1 *Point, c2 *Point, end *Point) float64 {

}
