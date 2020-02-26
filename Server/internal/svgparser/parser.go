package svgparser

//ParseD  parses d string in svg
func ParseD(input string) []Command {
	fsm := fsm{}
	fsm.Init()
	for _, char := range input {
		fsm.StateMachine(char)
	}
	fsm.End()
	return fsm.Commands
}
