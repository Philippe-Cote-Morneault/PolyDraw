package word_validator

import (
	"bufio"
	"log"
	"os"

	"gitlab.com/jigsawcorp/log3900/internal/language"
	"gitlab.com/jigsawcorp/log3900/model"
)

var notLoaded bool

//LoadList loads all the word list in memory.
func LoadList() {
	notLoaded = false
	result, err := model.Redis().Get("dict_loaded").Result()
	if err != nil || result == "false" {
		//Load the list
		loadFile("dict/fr_QC.txt", "dict_fr")
		loadFile("dict/en_US.txt", "dict_en")

		loadFile("dict/fr_QC_bad.txt", "dict_bad_fr")
		loadFile("dict/en_US_bad.txt", "dict_bad_en")

		if !notLoaded {
			model.Redis().Set("dict_loaded", "true", 0)
		} else {
			log.Printf("[Word] -> All or some files could not be loaded as words perhaps you need to download the files. Validation may not work!")
		}
	}

}

func loadFile(filename string, key string) {
	log.Printf("[Word] -> Loading the list %s in Redis", filename)
	file, err := os.Open(filename)
	if err != nil {
		log.Printf("[Word] -> File %s can't be loaded | %s", filename, err.Error())
		notLoaded = true
		return
	}
	defer file.Close()

	scanner := bufio.NewScanner(file)
	for scanner.Scan() {
		model.Redis().SAdd(key, scanner.Text())
	}

	if err := scanner.Err(); err != nil {
		log.Printf("[Word] -> Can't read the file %s properly | %s", filename, err.Error())
		notLoaded = true
		return
	}
	log.Printf("[Word] -> Done loading the list %s in Redis!", filename)
}

//IsWord check if the string is a word
func IsWord(word string, lang int) bool {
	var key string
	if lang == language.EN {
		key = "dict_en"
	} else if lang == language.FR {
		key = "dict_fr"
	}

	response, err := model.Redis().SIsMember(key, word).Result()
	if err != nil {
		return false
	}
	return response
}

//IsBlacklist checks if the word is present in a blacklist
func IsBlacklist(word string, lang int) bool {
	var key string
	if lang == language.EN {
		key = "dict_bad_en"
	} else if lang == language.FR {
		key = "dict_bad_fr"
	}

	response, err := model.Redis().SIsMember(key, word).Result()
	if err != nil {
		return false
	}
	return response
}
