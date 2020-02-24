package svgparser

// Point represents a 2 dimensionnal points with X,Y coordinates
type Point struct {
	X int
	Y int
}

// DElement represents an element in d value with a command and all the associated values
type DElement struct {
	Command string
	Values  []interface{}
}
