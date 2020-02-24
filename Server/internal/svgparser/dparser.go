package svgparser

import (
	"log"
	"strconv"
	"strings"
)

// Finds first command in str
// Returns command, index found
func findCommand(str string) (string, int) {
	commands := [10]string{"z", "m", "l", "h", "v", "c", "s", "q", "t", "a"}
	minIndex := len(str)
	command := ""

	for i := 0; i < len(commands); i++ {
		index := strings.Index(str, commands[i])                   // index for lowerCase command
		index2 := strings.Index(str, strings.ToUpper(commands[i])) // index for upperCase command
		if index != -1 && index < minIndex {
			minIndex = index

		}
		if index2 != -1 && index2 < minIndex {
			minIndex = index2
		}
	}

	if minIndex == len(str) { // If no command found
		return "", -1
	}
	return command, minIndex
}

// Removes all empty strings in d
func clean(d []string) []string {
	var newD []string
	for i := 0; i < len(d); i++ {
		if d[i] != "" {
			newD = append(newD, d[i])
		}
	}
	return newD
}

// Finds next block in str
// Returns next block, current index
func getNextBlock(str string) (string, int) {
	_, start := findCommand(str)
	if start != -1 {
		_, end := findCommand(str[start+1:])

		if end == -1 {
			return str[start:], -1 // Last block
		}
		return str[start : end+len(str[:start])+1], end + len(str[:start]) + 1
	}

	return "", -1 // No command found, returns empty block

}

// Parse and extract all values from block in dElement
//Returns new dElement with commands appended
func parseBlock(d []DElement, block string) []DElement {

	dElements := d

	dElements = append(dElements, DElement{Command: string(block[0])})
	indexD := len(dElements) - 1

	values := clean(strings.Split(block[1:], "*"))

	switch strings.ToLower(string(block[0])) {
	case "h", "v":
		extractValues(&dElements[indexD].Values, values, false)

	case "m", "l", "t", "s", "q", "c":
		extractValues(&dElements[indexD].Values, values, true)
	case "a":
		count := 0
		for i := 0; i < len(values); i++ {
			x, err := strconv.ParseFloat(values[i], 64)
			if err != nil {
				log.Printf("ERROR while parsing d : 'd' format is incorrect")
				return nil
			}
			if count != 1 { // if point, we add a point with Y coordinates
				i++
				if i >= len(values) {
					log.Printf("ERROR while parsing d : 'd' format is incorrect")
					return nil
				}

				y, err2 := strconv.ParseFloat(values[i], 64)
				if err2 != nil {
					log.Printf("ERROR while parsing d : 'd' format is incorrect")
					return nil
				}

				dElements[indexD].Values = append(dElements[indexD].Values, Point{X: x, Y: y})
			} else { // else we add single value
				dElements[indexD].Values = append(dElements[indexD].Values, x)
			}
			count++
			if count == 4 {
				count = 0
			}
		}
	}

	return dElements
}

// Extract all values and add it in dValues
func extractValues(dValues *[]interface{}, values []string, isPoint bool) {
	for i := 0; i < len(values); i++ {
		x, err := strconv.ParseFloat(values[i], 64)
		if err != nil {
			log.Printf("ERROR while parsing d : 'd' format is incorrect")
			return
		}
		if isPoint { // if point, we add a point with Y coordinates
			i++
			if i >= len(values) {
				log.Printf("ERROR while parsing d : 'd' format is incorrect")
				return
			}

			y, err2 := strconv.ParseFloat(values[i], 64)
			if err2 != nil {
				log.Printf("ERROR while parsing d : 'd' format is incorrect")
				return
			}

			*dValues = append(*dValues, Point{X: x, Y: y})
		} else { // else we add single value
			*dValues = append(*dValues, x)
		}
	}
}

// ParseD parses the elements in d
func ParseD(d string) []DElement {

	d = strings.ReplaceAll(strings.ReplaceAll(d, " ", "*"), ",", "*")

	dCleaned := strings.Join(clean(strings.Split(d, "*")), "*")
	var dElements []DElement

	index := 0
	newD := dCleaned
	for {
		block, i := getNextBlock(newD[index:])
		dElements = parseBlock(dElements, block)
		if i == -1 {
			break
		}
		index += i
	}
	return dElements

}
