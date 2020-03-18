package mode

type score struct {
	total   int
	current int
}

func (s *score) init() {
	s.total = 0
	s.current = 0
}

//commit add the current score to the total
func (s *score) commit(ptsToAdd int) {
	s.current += ptsToAdd
	s.total += ptsToAdd
}

//Reset the current score
func (s *score) reset() {
	s.current = 0
}
