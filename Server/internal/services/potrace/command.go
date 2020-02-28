package potrace

import (
	"encoding/xml"
	"fmt"
	"github.com/google/uuid"
	"gitlab.com/jigsawcorp/log3900/internal/datastore"
	"gitlab.com/jigsawcorp/log3900/internal/services/potrace/model"
	"gitlab.com/jigsawcorp/log3900/internal/svgparser"
	"gitlab.com/jigsawcorp/log3900/pkg/strbuilder"
	"io/ioutil"
	"os/exec"
)

func checkCommand(command string) bool {
	_, err := exec.LookPath(command)
	if err != nil {
		return false
	}
	return true
}

//Trace trace the image to the svg returns the key of the svg file generated
func Trace(imageKey string, blacklevel float64) (string, error) {
	svgKey := datastore.GenerateFileKey()
	svgPath := datastore.GetPath(svgKey)
	imagePath := datastore.GetPath(imageKey)

	cmd := fmt.Sprintf("convert -flatten %s bmp:- | potrace - -s -k %f -o %s", imagePath, blacklevel, svgPath)
	_, err := exec.Command("sh", "-c", cmd).Output()
	if err != nil {
		return "", fmt.Errorf("Failed to execute the conversion")
	}

	return svgKey, nil
}

//Translate changes the potrace svg to be compatible with our custom svg format
func Translate(svgKey string, brushSize int, mode int) error {
	file, err := datastore.GetFile(svgKey)
	if err != nil {
		return err
	}
	byteValue, _ := ioutil.ReadAll(file)
	var xmlSvg model.XMLSvg

	err = xml.Unmarshal(byteValue, &xmlSvg)
	if err != nil {
		return err
	}

	xmlSvg.G.Style = "fill:none;stroke:black"

	//Check all the paths
	for i := range xmlSvg.G.XMLPaths {
		var path *model.XMLPath
		path = &xmlSvg.G.XMLPaths[i]
		if path.ID == uuid.Nil {
			path.ID = uuid.New()
			path.Brush = "circle"
		}
		path.BrushSize = brushSize
	}
	splitPath(&xmlSvg.G.XMLPaths, xmlSvg.G.Transform)
	ChangeOrder(&xmlSvg.G.XMLPaths, mode)
	data, err := xml.Marshal(&xmlSvg)
	err = datastore.PutFile(&data, svgKey)
	if err != nil {
		return err
	}
	return nil
}

//splitPath export paths
func splitPath(paths *[]model.XMLPath, transform string) {
	builder := strbuilder.StrBuilder{}
	builder.Grow(128)
	newPaths := make([]model.XMLPath, 0, 20)
	transformCommands := svgparser.TransformParse(transform)

	for i := range *paths {
		commands := svgparser.ParseD((*paths)[i].D, transformCommands)

		lastChunk := 0
		subPathCount := 0

		for j := range commands {
			char := commands[j].Command
			if (char == 'm' || char == 'M') && j != 0 {
				singleCommands := commands[lastChunk:j]
				processChunk(&singleCommands, &(*paths)[i], &newPaths, &builder, lastChunk)

				lastChunk = j
				subPathCount++
			} else if j == 0 {
				if char == 'm' || char == 'M' {
					subPathCount++
				}
			}
		}

		if subPathCount >= 1 {
			singleCommands := commands[lastChunk:]
			processChunk(&singleCommands, &(*paths)[i], &newPaths, &builder, lastChunk)
		}
	}
	*paths = newPaths
}

func processChunk(commands *[]svgparser.Command, currentPath *model.XMLPath, paths *[]model.XMLPath, builder *strbuilder.StrBuilder, j int) {
	builder.Reset()

	singlePath := model.XMLPath{
		ID:        uuid.New(),
		Eraser:    currentPath.Eraser,
		Brush:     currentPath.Brush,
		BrushSize: currentPath.BrushSize,
		Color:     currentPath.Color,
	}
	length := 0.0
	for i := range *commands {
		if i == 0 {
			singlePath.FirstCommand = (*commands)[i]
		}
		builder.WriteString((*commands)[i].Encode())
		length += (*commands)[i].ComputeLength()
	}
	singlePath.Length = length
	singlePath.D = builder.StringVal()
	*paths = append(*paths, singlePath)
}
