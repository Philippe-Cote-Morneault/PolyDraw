package parser

import (
	"log"
	"strconv"
	"strings"
	"unicode"
)

const (
	NUMBUFF = 32
)

type fsm struct {
	state      int
	number     strings.Builder
	numbers    []float32
	curCommand rune
	Commands   []Command
}

func (f *fsm) Init() {
	f.state = 0
	f.curCommand = ' '
	f.number.Grow(NUMBUFF)
	f.numbers = make([]float32, 0, 16)
	f.Commands = make([]Command, 0, 16)
}

//StateMachine used for the lexical analysis of the d string
func (f *fsm) StateMachine(char rune) {
	switch f.state {
	case 0:
		if unicode.IsLetter(char) {
			f.state = 1
			f.parseLetter(char)
		}
	case 1:
		if f.isNumber(char) {
			f.state = 2
			f.parseNumber(char)
		} else if unicode.IsSpace(char) || char == ',' {
			f.state = 3
		} else {
			f.parseLetter(char)
		}
	case 2:
		if unicode.IsLetter(char) {
			f.state = 1
			f.endNumber()

			f.endCommand()
			f.parseLetter(char)
		} else if unicode.IsSpace(char) || char == ',' {
			f.endNumber()
			f.state = 3
		} else {
			f.parseNumber(char)
		}
	case 3:
		if unicode.IsLetter(char) {
			f.state = 1

			f.endCommand()
			f.parseLetter(char)
		} else if f.isNumber(char) {
			f.state = 2
			f.number.WriteRune(char)
		}
	}
}

func (f *fsm) isNumber(char rune) bool {
	return unicode.IsDigit(char) || char == '.' || char == '-'
}

//parseLetter add the letter to the state
func (f *fsm) parseLetter(char rune) {
	f.curCommand = char
}

func (f *fsm) parseNumber(char rune) {
	f.number.WriteRune(char)
}

//parseCommand of the command add it to the array
func (f *fsm) endCommand() {
	if f.curCommand != ' ' {
		//TODO command parse for the point relativeness
		f.Commands = append(f.Commands, Command{
			Command:    f.curCommand,
			Parameters: f.numbers,
		})

		f.curCommand = ' '
		f.numbers = make([]float32, 0, 16)
	} else {
		log.Printf("[Potrace] -> Invalid command \"%c\" in d attribute.", f.curCommand)
	}
}

func (f *fsm) endNumber() {
	//Try to parse the number and reset the buffer
	numStr := f.number.String()
	number, err := strconv.ParseFloat(numStr, 32)
	if err != nil {
		log.Printf("[Potrace] -> Invalid number \"%s\" in d attribute.", numStr)
	}
	f.numbers = append(f.numbers, float32(number))

	f.number.Reset()
	f.number.Grow(NUMBUFF)
}

//end of the state machine
func (f *fsm) End() {
	if f.state == 1 || f.state == 2 {
		f.endCommand()
	}
}
