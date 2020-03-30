package main

import (
	"Projet3ForTests/datastore"
	"Projet3ForTests/geometry"
	"Projet3ForTests/geometry/model"
	"Projet3ForTests/strokegenerator"
	"Projet3ForTests/svgparser"
	"bytes"
	"fmt"
	"log"
	"math/rand"
	"os/exec"
	"sort"
)

type points []model.Point

func (p points) Len() int           { return len(p) }
func (p points) Swap(i, j int)      { p[i], p[j] = p[j], p[i] }
func (p points) Less(i, j int) bool { return p[i].X < p[j].X }

func mainDrawingPython() {
	geometry.InitTable()
	commands := svgparser.ParseD(svgparser.SvgKey, nil)

	var points []model.Point
	points = strokegenerator.ExtractPointsStrokes(&commands)
	s := ""
	buf := bytes.NewBufferString(s)
	fmt.Fprint(buf, points)
	s = buf.String()
	if datastore.CreatePythonFile(s[1:len(s)-1]) != 0 {
		cmd := exec.Command("python3", "scatterer.py")
		err := cmd.Run()
		errW := cmd.Wait()
		output, errO := cmd.Output()
		if errO != nil {
			log.Fatal(err)
		}
		if errW != nil {
			log.Fatal(err)
		}
		if err != nil {
			log.Fatal(err)
		}

		fmt.Println(string(output))
	}
}

func main() {
	mainDrawingPython()

	//var a []int
	//	//
	//	//a = append(a, 1)
	//	//a = append(a, 2)
	//	//a = append(a, 3)
	//	//a = append(a, 4)
	//	//a = append(a, 5)
	//	//a = append(a, 6)
	//	//a = append(a, 7)
	//	//a = append(a, 8)
	//	//a = append(a, 9)
	//	//a = append(a, 10)
	//	//
	//	//sort.Sort(a)

	//var a []model.Point
	//
	//test(&a)
}

func test(a *[]model.Point) {
	*a = append(*a, model.Point{X: 4, Y: 0})
	*a = append(*a, model.Point{X: 2, Y: 0})
	*a = append(*a, model.Point{X: 3, Y: 0})
	*a = append(*a, model.Point{X: 1, Y: 0})
	*a = append(*a, model.Point{X: 5, Y: 0})
	*a = append(*a, model.Point{X: 7, Y: 0})
	*a = append(*a, model.Point{X: 6, Y: 0})

	fmt.Println(*a)

	sort.Sort(points(*a))
	fmt.Println(*a)

	sort.Sort(sort.Reverse(points(*a)))

	fmt.Println(*a)
	rand.Shuffle(len(*a), func(i, j int) {
		(*a)[i], (*a)[j] = (*a)[j], (*a)[i]
	})
	fmt.Println(*a)
}
