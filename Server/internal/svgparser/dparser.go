package svgparser

import (
	"log"
	"strconv"
	"strings"
)

type dTest struct {
	D string
}

func findCommand(str string) string {
	liste := [10]string{"z", "m", "l", "h", "v", "c", "s", "q", "t", "a"}

	for i := 0; i < len(liste); i++ {
		index := strings.Index(str, liste[i])
		index2 := strings.Index(str, strings.ToUpper(liste[i]))
		if index != -1 {
			return liste[i]

		} else if index2 != -1 {
			return strings.ToUpper(liste[i])
		}
	}
	return ""
}

func getXY(str string) (int, int, bool) {
	cut := strings.Index(str, ",")
	if cut == -1 {
		x, errX := strconv.Atoi(str)
		if errX != nil {
			log.Printf("ERROR while parsing d : 'd' format is incorrect")
			return 0, 0, false
		}

		return x, 0, false
	}

	x, errX := strconv.Atoi(str[1:cut])
	y, errY := strconv.Atoi(str[cut+1:])

	if errX != nil || errY != nil {
		log.Panicln("ERROR while parsing d : 'd' format is incorrect")
		return 0, 0, false
	}

	return x, y, true
}

func cleanD(dSplit []string) []string {
	var newdSplit []string
	for i := 0; i < len(dSplit); i++ {
		if dSplit[i] != "" {
			newdSplit = append(newdSplit, dSplit[i])
		}
	}
	return newdSplit
}

// ParseD parses the elements in d
func ParseD(d string) []DElement {

	var dElements []DElement

	dSplit := cleanD(strings.Split(d, " "))

	indexdElement := -1
	for i := 0; i < len(dSplit); i++ {
		command := findCommand(dSplit[i])
		if command != "" {
			dElements = append(dElements, DElement{Command: command})
			indexdElement++
		}
		x, y, isPoint := getXY(dSplit[i])

		if isPoint {
			dElements[indexdElement].Values = append(dElements[indexdElement].Values, Point{X: x, Y: y})
		} else {
			dElements[indexdElement].Values = append(dElements[indexdElement].Values, x)
		}
	}

	return dElements
}
