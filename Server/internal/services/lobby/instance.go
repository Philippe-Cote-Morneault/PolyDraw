package lobby

var instance *Lobby

//Instance access the service instance
func Instance() *Lobby {
	return instance
}
