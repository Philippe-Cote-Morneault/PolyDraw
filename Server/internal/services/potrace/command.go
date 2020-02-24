package potrace

import (
	"encoding/xml"
	"fmt"
	"github.com/google/uuid"
	"gitlab.com/jigsawcorp/log3900/internal/datastore"
	"gitlab.com/jigsawcorp/log3900/internal/services/potrace/model"
	"gitlab.com/jigsawcorp/log3900/internal/services/potrace/parser"
	"io/ioutil"
	"os/exec"
	"strings"
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

	//Check all the paths
	for i := range xmlSvg.G.XMLPaths {
		var path *model.XMLPath
		path = &xmlSvg.G.XMLPaths[i]
		if path.ID == uuid.Nil {
			path.ID = uuid.New()
			path.Brush = "circle"
			path.BrushSize = brushSize
		}
		path.D = strings.Replace(path.D, "\n", " ", -1)
	}
	xmlSvg.XmlnsPolydraw = "http://polydraw"
	splitPath(&xmlSvg.G.XMLPaths)
	ChangeOrder(&xmlSvg.G.XMLPaths, mode)
	data, err := xml.Marshal(&xmlSvg)
	err = datastore.PutFile(&data, svgKey)
	if err != nil {
		return err
	}
	return nil
}

//splitPath export paths
func splitPath(paths *[]model.XMLPath) {
	for i := range *paths {
		parser.ParseD((*paths)[i].D)
	}
}
