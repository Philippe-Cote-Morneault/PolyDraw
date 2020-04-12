package svgparser

//ParseD  parses d string in svg
func ParseD(input string, transformations []TransformCommand) []Command {
	fsm := fsm{}
	fsm.Init(&transformations)
	for _, char := range input {
		fsm.StateMachine(char)
	}
	fsm.End()
	return fsm.Commands
}
