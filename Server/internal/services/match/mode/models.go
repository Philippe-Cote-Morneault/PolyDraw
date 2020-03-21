package mode

//PlayersData represents usefull informations for a players
type PlayersData struct {
	UserID   string
	Username string
	IsCPU    bool
	Points   int
}

//PlayersRoundSum represent players with all the information for the round
type PlayersRoundSum struct {
	PlayersData
	PointsTotal int
}

//ResponseGameInfo represents the message for type 61
type ResponseGameInfo struct {
	Players   []PlayersData
	GameType  int
	TimeImage int64
	Laps      int
	TotalTime int
}

//PlayerDrawThis used for message 69
type PlayerDrawThis struct {
	Word      string
	Time      int64
	DrawingID string
}

//PlayerTurnDraw used for message 57
type PlayerTurnDraw struct {
	UserID    string
	Username  string
	Time      int64
	DrawingID string
	Length    int
}

//GameEnded message used when a game ends
type GameEnded struct {
	Players    []PlayersData
	Winner     string
	WinnerName string
	Time       int64
}

//GuessResponse used when the player tries to guess a word
type GuessResponse struct {
	Valid       bool
	Points      int
	PointsTotal int
}

//WordFound used to broadcast to all other players that the word was discovered
type WordFound struct {
	Username    string
	UserID      string
	Points      int
	PointsTotal int
}

//TimeUp message used to broadcast to other players that the time is up for finding the name
type TimeUp struct {
	Type int
	Word string
}

//HintResponse represents the response to a hint requested by a player
type HintResponse struct {
	Hint  string
	Error string
}

//PlayerSync used to sync all the players with a unique trusted time source
type PlayerSync struct {
	Players  []PlayersData
	Laps     int
	Time     int64
	GameTime int
	LapTotal int
}

//PlayerHasLeft used to notify all the clients that a player has left a game
type PlayerHasLeft struct {
	UserID   string
	Username string
}

//AchievementData represents data for an achievement
type AchievementData struct {
	Name   string
	UserID string
}

//RoundSummary used to tell the details about a round
type RoundSummary struct {
	Players      []PlayersRoundSum
	Achievements []AchievementData
	Word         string
}
