package potrace

import (
	"fmt"
	"gitlab.com/jigsawcorp/log3900/internal/datastore"
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

	cmd := fmt.Sprintf("convert %s bmp:- | potrace - -s -k %f -o %s", imagePath, blacklevel, svgPath)
	_, err := exec.Command("sh", "-c", cmd).Output()
	if err != nil {
		return "", fmt.Errorf("Failed to execute the conversion")
	}

	return svgKey, nil
}
