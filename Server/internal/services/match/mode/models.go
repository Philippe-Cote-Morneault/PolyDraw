package mode

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

//PlayerDrawThis used for message 69
type PlayerDrawThis struct {
	Word      string
	Time      int
	DrawingID string
}

//PlayerTurnDraw used for message 57
type PlayerTurnDraw struct {
	UserID    string
	Username  string
	Time      int
	DrawingID string
	Length    int
}

//PlayersDataPoint represent player and its data
type PlayersDataPoint struct {
	Username string
	UserID   string
	Point    int
}

//GameEnded message used when a game ends
type GameEnded struct {
	Players    []PlayersDataPoint
	Winner     string
	WinnerName string
	Time       int
}

//GuessResponse used when the player tries to guess a word
type GuessResponse struct {
	Valid       bool
	Point       int
	PointsTotal int
}

//WordFound used to broadcast to all other players that the word was discovered
type WordFound struct {
	Username    string
	UserID      string
	Point       int
	PointsTotal int
}

//TimeUp message used to broadcast to other players that the time is up for finding the name
type TimeUp struct {
	Type int
	Word string
}
