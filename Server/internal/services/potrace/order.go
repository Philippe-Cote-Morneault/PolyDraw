package potrace

import (
	"gitlab.com/jigsawcorp/log3900/internal/services/potrace/model"
	"gitlab.com/jigsawcorp/log3900/internal/svgparser"
	"math"
	"math/rand"
	"sort"
	"time"
)

//ChangeOrder update the order of the svg file
func ChangeOrder(paths *[]model.XMLPath, mode int) {
	switch mode {
	case 1:
		random(paths)
	case 2, 3, 4, 5:
		pano(paths, mode)
	case 6, 7:
		centered(paths, mode)
	}
}

//random changes the order of the paths randomly
func random(paths *[]model.XMLPath) {
	random := rand.New(rand.NewSource(time.Now().UnixNano()))
	pathLength := len(*paths)
	choices := make([]int, pathLength)
	for i := 0; i < pathLength; i++ {
		choices[i] = i
	}

	remaining := pathLength
	for i := 0; i < pathLength; i++ {
		pos := random.Intn(remaining)
		choice := pop(&choices, pos, remaining-1)

		(*paths)[i].Order = choice

		remaining--
	}
}

//pano changes the order according to the panoramic order
func pano(paths *[]model.XMLPath, mode int) {
	switch mode {
	case 2:
		sort.Slice(paths, func(i, j int) bool {
			return (*paths)[i].FirstCommand.EndPos.X > (*paths)[j].FirstCommand.EndPos.X
		})
	case 3:
		sort.Slice(paths, func(i, j int) bool {
			return (*paths)[i].FirstCommand.EndPos.X < (*paths)[j].FirstCommand.EndPos.X
		})
	case 4:
		sort.Slice(paths, func(i, j int) bool {
			return (*paths)[i].FirstCommand.EndPos.Y > (*paths)[j].FirstCommand.EndPos.Y
		})
	case 5:
		sort.Slice(paths, func(i, j int) bool {
			return (*paths)[i].FirstCommand.EndPos.Y < (*paths)[j].FirstCommand.EndPos.Y
		})
	}
	applyOrder(paths)
}

func centered(paths *[]model.XMLPath, mode int) {
	center := svgparser.Point{}
	for i := range *paths {
		center.X += (*paths)[i].FirstCommand.EndPos.X
		center.Y += (*paths)[i].FirstCommand.EndPos.Y
	}
	length := len(*paths)
	center.X = (1 / float32(length)) * center.X
	center.Y = (1 / float32(length)) * center.Y

	//Order by distance
	switch mode {
	case 6:
		sort.Slice(paths, func(i, j int) bool {
			return distNonSquared(&(*paths)[i], &center) > distNonSquared(&(*paths)[j], &center)
		})
	case 7:
		sort.Slice(paths, func(i, j int) bool {
			return distNonSquared(&(*paths)[i], &center) < distNonSquared(&(*paths)[j], &center)
		})
	}
	applyOrder(paths)
}

//pop remove an element from the array
func pop(a *[]int, i int, lastElement int) int {
	// Remove the element at index i from a.
	value := (*a)[i]
	(*a)[i] = (*a)[lastElement]
	(*a)[lastElement] = -1

	return value
}

func distNonSquared(a *model.XMLPath, b *svgparser.Point) float64 {
	return math.Pow(float64(a.FirstCommand.EndPos.X-b.X), 2) + math.Pow(float64(a.FirstCommand.EndPos.Y-b.Y), 2)
}

func applyOrder(paths *[]model.XMLPath) {
	//Iterate over the paths and set their number
	for i := range *paths {
		(*paths)[i].Order = i
	}
}
