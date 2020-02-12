package api

import "regexp"

var regexUsername *regexp.Regexp
var regexEmail *regexp.Regexp

//Init function for the API
func Init() {
	regexUsername = regexp.MustCompile(`^[a-z0-9_]{4,12}$`)
	regexEmail = regexp.MustCompile(`^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\.[a-zA-Z0-9-.]+$`)
}
