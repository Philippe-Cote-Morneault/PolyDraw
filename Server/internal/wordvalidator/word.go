package wordvalidator

import (
	"bufio"
	"log"
	"os"
	"sync"

	"gitlab.com/jigsawcorp/log3900/internal/language"
	"gitlab.com/jigsawcorp/log3900/model"
)

var notLoaded bool
var wg sync.WaitGroup
var files map[string]string

//LoadList loads all the word list in memory.
func LoadList() {
	files = map[string]string{
		"dict_fr":       "dict/fr_QC.txt",
		"dict_en":       "dict/en_US.txt",
		"dict_bad_fr":   "dict/fr_QC_bad.txt",
		"dict_bad_en":   "dict/en_US_bad.txt",
		"dict_words_fr": "dict/fr_QC_words.txt",
		"dict_words_en": "dict/en_US_words.txt",
	}
	notLoaded = false
	result, err := model.Redis().Get("dict_loaded").Result()
	if err != nil || result == "false" {
		loadAllLists()
	}

	//Check if all the other keys are present if not delete and reload all
	for key := range files {
		go checkKey(key)
	}
	wg.Wait()
}

func checkKey(key string) {
	result, err := model.Redis().Exists(key).Result()
	if err != nil || result == 0 {
		loadFile(key)
	}
}

func loadAllLists() {
	log.Printf("[Word] -> Starting the import process in Redis this may take a while!")
	model.Redis().Set("dict_loaded", "false", 0)

	//Load all the lists
	for key := range files {
		//Attempt to delete the key if it is already present in the redis database
		model.Redis().Del(key)

		go loadFile(key)
	}
	wg.Wait()

	if !notLoaded {
		model.Redis().Set("dict_loaded", "true", 0)
		log.Printf("[Word] -> All word lists were added to Redis!")
	} else {
		log.Printf("[Word] -> All or some files could not be loaded as words perhaps you need to download the files. Validation may not work!")
	}
}

func loadFile(key string) {
	defer wg.Done()
	wg.Add(1)

	log.Printf("[Word] -> Loading the list %s in Redis", files[key])
	file, err := os.Open(files[key])
	if err != nil {
		log.Printf("[Word] -> File %s can't be loaded | %s", files[key], err.Error())
		notLoaded = true
		return
	}
	defer file.Close()

	scanner := bufio.NewScanner(file)
	i := 0
	for scanner.Scan() {
		text := scanner.Text()
		model.Redis().SAdd(key, text)
		i++
		//Every 10 000 word print the word we are currently adding
		if i%10000 == 0 {
			log.Printf("[Word] -> Loading file %s | Current word: %s", files[key], text)
		}
	}

	if err := scanner.Err(); err != nil {
		log.Printf("[Word] -> Can't read the file %s properly | %s", files[key], err.Error())
		notLoaded = true
		return
	}
	log.Printf("[Word] -> Done loading the list %s in Redis!", files[key])
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
