package potrace

import (
	"gitlab.com/jigsawcorp/log3900/internal/services/potrace/model"
	"regexp"
	"strconv"
	"strings"
)

type transformCommand struct {
	Command string
	Attr1   float64
	Attr2   float64
}

var transformReg *regexp.Regexp

//Transform used to do the potrace transforms on the paths
func Transform(svg *model.XMLSvg) {
	if svg.G.Transform != "" {
		commands := transformParse(svg.G.Transform)
		for i := range commands {
			commands[i].Apply(svg)
		}
	}
}
func transformParse(transformStr string) []transformCommand {
	response := make([]transformCommand, 3)
	groups := transformReg.SubexpNames()
	results := transformReg.FindAllStringSubmatch(transformStr, -1)
	for i := range results {
		//Parse the transforms
		command := ""
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
		response = append(response, transformCommand{command, attr1, attr2})
	}
	return response
}

//Apply used to apply the current transformation to the svg
func (t *transformCommand) Apply(svg *model.XMLSvg) {
	command := strings.ToLower(t.Command)
	switch command {
	case "translate":
		t.applyTranslate(svg)
	case "rotate":
		t.applyRotate(svg)
	case "scale":
		t.applyScale(svg)
	}
}

func (t *transformCommand) applyTranslate(svg *model.XMLSvg) {

}

func (t *transformCommand) applyRotate(svg *model.XMLSvg) {

}
func (t *transformCommand) applyScale(svg *model.XMLSvg) {

}
