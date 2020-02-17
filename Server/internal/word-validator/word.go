package word_validator

import (
	"bufio"
	"gitlab.com/jigsawcorp/log3900/internal/language"
	"gitlab.com/jigsawcorp/log3900/model"
	"log"
	"os"
)

var notLoaded bool

func LoadList() {
	notLoaded = false
	result, err := model.Redis().Get("dict_loaded").Result()
	if err != nil || result == "false" {
		//Load the list
		loadFile("dict/fr_QC.txt", "dict_fr")
		loadFile("dict/en_US.txt", "dict_en")

		loadFile("dict/fr_QC_bad.txt", "dict_bad_fr")
		loadFile("dict/en_US_bad.txt", "dict_bad_en")
	}
	if !notLoaded {
		model.Redis().Set("dict_loaded", "true", 0)
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
