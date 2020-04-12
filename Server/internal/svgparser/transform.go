package svgparser

import (
	"regexp"
	"strconv"
	"unicode"
)

const (
	scale = iota
	translate
	rotate
)

//TransformCommand represent a command
type TransformCommand struct {
	Command   string
	CommandID int
	Attr1     float64
	Attr2     float64
	Width     float32
	Height    float32
}

var transformReg *regexp.Regexp
var numberReg *regexp.Regexp

//TransformInit compiles regex for the transform string
func TransformInit() {
	transformReg = regexp.MustCompile(`(?P<command>\w*)\((?P<attr1>[-\d.e]+)(,(?P<attr2>[-\d.e]+))?\)`)
	numberReg = regexp.MustCompile(`[\d.\- e]*`)
}

//TransformParse parses the transform string and returns a transforms commands
func TransformParse(transformStr string, width string, height string) []TransformCommand {
	widthf, _ := strconv.ParseFloat(numberReg.FindString(width), 64)
	heightf, _ := strconv.ParseFloat(numberReg.FindString(height), 64)
	response := make([]TransformCommand, 0, 3)
	if transformStr == "" {
		return response
	}

	groups := transformReg.SubexpNames()
	results := transformReg.FindAllStringSubmatch(transformStr, -1)
	for i := range results {
		//Parse the transforms
		command := ""
		commandID := -1
		attr1 := 0.0
		attr2 := 0.0
		for j, v := range groups {
			subMatch := results[i][j]
			switch v {
			case "command":
				command = subMatch
			case "attr1":
				attr1, _ = strconv.ParseFloat(subMatch, 64)
			case "attr2":
				attr2, _ = strconv.ParseFloat(subMatch, 64)
			}
		}
		switch command {
		case "translate":
			commandID = translate
		case "rotate":
			commandID = rotate
		case "scale":
			commandID = scale
		}
		response = append(response, TransformCommand{command, commandID, attr1, attr2, float32(widthf), float32(heightf)})
	}
	return response
}

//Apply used to apply the current transformation to the svg
func (t *TransformCommand) Apply(command rune, point float32, isX bool) float32 {
	switch t.CommandID {
	case translate:
		//return t.applyTranslate(command, point, isX)
	case rotate:
		return t.applyRotate(command, point, isX)
	case scale:
		return t.applyScale(command, point, isX)
	}
	return point
}

func (t *TransformCommand) applyTranslate(command rune, point float32, isX bool) float32 {
	if unicode.IsUpper(command) {
		if isX {
			return point + (float32(t.Attr1) / 2)
		}
		return point + (float32(t.Attr2) / 2)
	}
	return point

}

func (t *TransformCommand) applyRotate(command rune, point float32, isX bool) float32 {
	return point
}
func (t *TransformCommand) applyScale(command rune, point float32, isX bool) float32 {
	response := float32(0.0)
	if isX {
		response = point * float32(t.Attr1)
	} else {
		response = point * float32(t.Attr2)
	}

	if response < 0 && unicode.IsUpper(command) {
		if isX {
			return t.Width + response
		}
		return t.Height + response
	}
	return response
}
