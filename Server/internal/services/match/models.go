package match

//PlayersData represents usefull informations for a players
type PlayersData struct {
	UserID   string
	Username string
	IsCPU    bool
}

//ResponseGameInfo represents the message for type 61
type ResponseGameInfo struct {
	Players   []PlayersData
	GameType  int
	TimeImage int
	Laps      int
	TotalTime int
}
