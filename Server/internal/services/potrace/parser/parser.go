package parser

//Point represents a point
type Point struct {
	X int
	Y int
}

//Command represents a command of the D string
type Command struct {
	Command    rune
	InitialPos Point
	Parameters []float32
}

func ParseD(input string) []Command {
	fsm := fsm{}
	fsm.Init()
	for _, char := range input {
		fsm.StateMachine(char)
	}
	fsm.End()
	return fsm.Commands
}
