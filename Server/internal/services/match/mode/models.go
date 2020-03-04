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

//PlyaerTurnDraw used for message 57
type PlayerTurnDraw struct {
	UserID    string
	Username  string
	Time      int
	DrawingID string
	Length    int
}

type PlayersDataPoint struct {
	Username string
	UserID   string
	Point    int
}

type GameEnded struct {
	Players    []PlayersDataPoint
	Winner     string
	WinnerName string
	Time       int
}
