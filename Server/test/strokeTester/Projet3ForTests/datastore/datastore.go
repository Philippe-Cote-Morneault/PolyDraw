package datastore

import (
	"fmt"
	"os"
	"strings"
)

//CreatePythonFile used to post the file name
func CreatePythonFile(points string) int{
	points = strings.ReplaceAll(points, "} ", "},")
	errRemove := os.Remove("points.py")

	if errRemove != nil {
		fmt.Println(errRemove)
		return 0
	}
	file, err := os.OpenFile("points.py", os.O_WRONLY|os.O_CREATE, 0666)
	file.Seek(0,0)
	if err != nil {
		fmt.Println(err)
		return 0
	}
	points = "points = \"\"\"" + points+ "\"\"\""
	file.Write([]byte(points))
	file.Close()
	return 1
}

