package language

import (
	"bytes"
	"github.com/spf13/viper"
	"log"
	"net/http"
)

const (
	//FR represent language
	FR = 1
	//EN represent language
	EN = 0
)

var viperInstance *viper.Viper

//Init load all the strings in memory
func Init() {
	log.Printf("[18n] Loading all the strings translation")
	viperInstance = viper.New()
	viperInstance.SetConfigType("yaml")
	err := viperInstance.ReadConfig(bytes.NewBuffer(strings))
	if err != nil {
		panic(err)
	}
}

//MustGet returns the translated string for a key
func MustGet(key string, lang int) string {
	var realKey string
	switch lang {
	case 0:
		realKey = "en." + key
	case 1:
		realKey = "fr." + key
	default:
		panic("[i18n] The language is not valid")
	}
	if viperInstance.IsSet(realKey) {
		return viperInstance.GetString(realKey)
	} else {
		panic("[i18n] The key " + realKey + " cannot be found")
	}
}

//MustGetRest returns the translated string with a request for a key
func MustGetRest(key string, r *http.Request) string {
	ctxLang := 1
	lang := r.Context().Value(ctxLang).(int)

	return MustGet(key, lang)
}
