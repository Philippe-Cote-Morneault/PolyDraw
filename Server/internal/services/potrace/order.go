package potrace

import (
	"gitlab.com/jigsawcorp/log3900/internal/services/potrace/model"
	"math/rand"
	"time"
)

//ChangeOrder update the order of the svg file
func ChangeOrder(svg *[]model.XMLPath, mode int) {
	switch mode {
	case 1:
		Random(svg)
	}
}

//Random changes the order of the paths randomly
func Random(svg *[]model.XMLPath) {
	random := rand.New(rand.NewSource(time.Now().UnixNano()))
	pathLength := len(*svg)
	choices := make([]int, pathLength)
	for i := 0; i < pathLength; i++ {
		choices[i] = i
	}

	remaining := pathLength
	for i := 0; i < pathLength; i++ {
		pos := random.Intn(remaining)
		choice := pop(&choices, pos, remaining-1)

		(*svg)[i].Order = choice

		remaining--
	}
}

//pop remove an element from the array
func pop(a *[]int, i int, lastElement int) int {
	// Remove the element at index i from a.
	value := (*a)[i]
	(*a)[i] = (*a)[lastElement]
	(*a)[lastElement] = -1

	return value
}
