package drawing

import (
	"math/rand"
	"sort"

	geometry "gitlab.com/jigsawcorp/log3900/pkg/geometry/model"
)

type xSort []geometry.Point

func (p xSort) Len() int           { return len(p) }
func (p xSort) Swap(i, j int)      { p[i], p[j] = p[j], p[i] }
func (p xSort) Less(i, j int) bool { return p[i].X < p[j].X }

type ySort []geometry.Point

func (p ySort) Len() int           { return len(p) }
func (p ySort) Swap(i, j int)      { p[i], p[j] = p[j], p[i] }
func (p ySort) Less(i, j int) bool { return p[i].Y < p[j].Y }

type centerSort []geometry.Point

func (p centerSort) Len() int           { return len(p) }
func (p centerSort) Swap(i, j int)      { p[i], p[j] = p[j], p[i] }
func (p centerSort) Less(i, j int) bool { return distNonSquared(&p[i]) < distNonSquared(&p[j]) }

var barycenter geometry.Point

// OrderPoints according to the mode
func OrderPoints(points *[]geometry.Point, mode int) {
	switch mode {
	case 1:
		random(points)
	case 2, 3, 4, 5:
		panoramic(points, mode)
	case 6, 7:
		centered(points, mode)
	default:
		return
	}

}

func random(points *[]geometry.Point) {
	rand.Shuffle(len(*points), func(i, j int) {
		(*points)[i], (*points)[j] = (*points)[j], (*points)[i]
	})
}
func panoramic(points *[]geometry.Point, mode int) {
	switch mode {
	case 2: //droite a gauche
		sort.Sort(sort.Reverse(xSort(*points)))
	case 3: //gauche a droite
		sort.Sort(xSort(*points))
	case 4: //Haut a bas
		sort.Sort(sort.Reverse(ySort(*points)))
	case 5: //Bas a haut
		sort.Sort(ySort(*points))

	}
}
func centered(points *[]geometry.Point, mode int) {
	barycenter = calculateBarycenter(points)
	switch mode {
	case 6:
		sort.Sort(centerSort(*points))
	case 7:
		sort.Sort(sort.Reverse(centerSort(*points)))
	}
}

func calculateBarycenter(points *[]geometry.Point) geometry.Point {
	totalX := float32(0)
	totalY := float32(0)

	for _, point := range *points {
		totalX += point.X
		totalY += point.Y
	}
	return geometry.Point{X: totalX / float32(len(*points)), Y: totalY / float32(len(*points))}
}

func distNonSquared(p *geometry.Point) float64 {
	return pow2(float64(p.X-barycenter.X)) + pow2(float64(p.Y-barycenter.Y))
}

func pow2(num float64) float64 {
	return num * num
}
