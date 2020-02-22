package api

import (
	"encoding/json"
	"gitlab.com/jigsawcorp/log3900/internal/services/potrace"
	"net/http"
	"strconv"
	"strings"

	"github.com/google/uuid"
	"github.com/gorilla/mux"

	"gitlab.com/jigsawcorp/log3900/internal/datastore"
	"gitlab.com/jigsawcorp/log3900/internal/language"
	"gitlab.com/jigsawcorp/log3900/internal/wordvalidator"
	"gitlab.com/jigsawcorp/log3900/model"
	"gitlab.com/jigsawcorp/log3900/pkg/rbody"
)

const (
	//MB represents the value of a megabyte
	MB = 1 << 20
)

type gameResponse struct {
	ID         string
	Word       string
	Difficulty int
	Hints      []string
	ImageMode  int
}

type gameRequestCreation struct {
	Word       string
	Hints      []string
	Difficulty int
}

type gameResponseCreation struct {
	GameID string
}

//PostGame represent the game creation
func PostGame(w http.ResponseWriter, r *http.Request) {
	var request gameRequestCreation
	decoder := json.NewDecoder(r.Body)
	err := decoder.Decode(&request)

	if err != nil {
		rbody.JSONError(w, http.StatusBadRequest, err.Error())
		return
	}

	if strings.TrimSpace(request.Word) == "" {
		rbody.JSONError(w, http.StatusBadRequest, "The word cannot be blank.")
		return
	}

	//Validate if the word is validate
	wordLower := strings.ToLower(request.Word)
	if wordvalidator.IsBlacklist(wordLower, language.EN) {
		rbody.JSONError(w, http.StatusBadRequest, "This word is not allowed!")
		return
	}

	if !wordvalidator.IsWord(wordLower, language.EN) {
		rbody.JSONError(w, http.StatusBadRequest, "This is not a word, please enter a valid word.")
		return
	}

	if request.Difficulty < 0 || request.Difficulty > 3 {
		rbody.JSONError(w, http.StatusBadRequest, "The difficulty must be betwene 0 and 3.")
		return
	}

	if len(request.Hints) < 3 || len(request.Hints) > 10 {
		rbody.JSONError(w, http.StatusBadRequest, "The game must have at least 3 hints and not more than 10.")
		return
	}

	//Check for all the hints that they are valid.
	var hints []*model.GameHint
	for i := range request.Hints {
		if strings.TrimSpace(request.Hints[i]) == "" {
			rbody.JSONError(w, http.StatusBadRequest, "The hints cannot be empty.")
			return
		}
		hints = append(hints, &model.GameHint{
			Hint: request.Hints[i],
		})
	}
	game := model.Game{
		Word:       request.Word,
		Difficulty: request.Difficulty,
		Hints:      hints,
	}
	model.DB().Save(&game)
	rbody.JSON(w, http.StatusOK, &gameResponseCreation{GameID: game.ID.String()})

}

//PostGameImage used to create the images
func PostGameImage(w http.ResponseWriter, r *http.Request) {
	r.Body = http.MaxBytesReader(w, r.Body, 5*MB)

	vars := mux.Vars(r)
	gameID, err := uuid.Parse(vars["id"])
	if err != nil {
		rbody.JSONError(w, http.StatusBadRequest, "A valid uuid must be set")
		return
	}

	//Check if the game exists
	game := model.Game{}
	model.DB().Where("id = ?", gameID).First(&game)

	if game.ID == uuid.Nil {
		rbody.JSONError(w, http.StatusNotFound, "The game cannot be found. Please check if the id is valid.")
		return
	}
	//Check for the fields
	mode := r.FormValue("mode")
	if mode == "" {
		rbody.JSONError(w, http.StatusBadRequest, "The mode is not set. Please set the number of the mode between 0-3.")
		return
	}

	modeInt, err := strconv.Atoi(mode)
	if err != nil {
		rbody.JSONError(w, http.StatusBadRequest, "The mode is not a number. The mode must be between 0-3.")
		return
	}

	if modeInt > 3 || modeInt < 0 {
		rbody.JSONError(w, http.StatusBadRequest, "The mode must be between 0-3.")
		return
	}

	file, _, err := r.FormFile("file")
	if err != nil {
		rbody.JSONError(w, http.StatusBadRequest, "The file is not valid")
		return
	}

	fileHeader := make([]byte, 512)

	if _, err := file.Read(fileHeader); err != nil {
		rbody.JSONError(w, http.StatusBadRequest, "The file cannot be read.")
		return
	}

	if _, err := file.Seek(0, 0); err != nil {
		rbody.JSONError(w, http.StatusBadRequest, "The file cannot be read.")
		return
	}

	mime := http.DetectContentType(fileHeader)
	switch mime {
	case "text/xml; charset=utf-8", "image/png", "image/jpeg", "image/bmp":
		var keyFile string
		keyFile, err = datastore.PostFile(file)

		if err != nil {
			rbody.JSONError(w, http.StatusInternalServerError, "The file cannot be saved. "+err.Error())
			return
		}
		defer file.Close()

		image := model.GameImage{}
		image.Mode = modeInt
		if mime == "text/xml; charset=utf-8" {
			//Load svg
			image.SVGFile = keyFile
			//TODO validate the SVG
		} else {
			//Load jpg
			image.ImageFile = keyFile

			//Check if the blackness level is set
			blackLevelStr := r.FormValue("blacklevel")
			blackLevel, err := strconv.ParseFloat(blackLevelStr, 32)
			if blackLevelStr == "" {
				rbody.JSONError(w, http.StatusBadRequest, "The blacklevel must be set when uploading a non vector image.")
				return
			}
			if err != nil {
				rbody.JSONError(w, http.StatusBadRequest, "The blacklevel must be a decimal number.")
				return
			}
			if blackLevel > 1 || blackLevel < 0 {
				rbody.JSONError(w, http.StatusBadRequest, "The blacklevel must be between 0 and 1.")
				return
			}
			svgKey, err := potrace.Trace(keyFile, blackLevel)
			if err != nil {
				rbody.JSONError(w, http.StatusBadRequest, err.Error())
				return
			}
			image.SVGFile = svgKey
		}
		game.Image = &image
		model.DB().Save(&game)
		rbody.JSON(w, http.StatusOK, "OK")
	default:
		rbody.JSONError(w, http.StatusBadRequest, "The file is not a valid type")
		return
	}

}

//GetGame returns a game
func GetGame(w http.ResponseWriter, r *http.Request) {
	vars := mux.Vars(r)
	gameID, err := uuid.Parse(vars["id"])
	if err != nil {
		rbody.JSONError(w, http.StatusBadRequest, "A valid uuid must be set")
		return
	}

	//Check if the game exists
	game := model.Game{}
	model.DB().Preload("Hints").Preload("Image").Where("id = ?", gameID).First(&game)

	if game.ID == uuid.Nil {
		rbody.JSONError(w, http.StatusNotFound, "The game cannot be found. Please check if the id is valid.")
		return
	}
	var hints []string
	for i := range game.Hints {
		hints = append(hints, game.Hints[i].Hint)
	}
	imageMode := 0
	if game.Image != nil {
		imageMode = game.Image.Mode
	}
	response := gameResponse{
		ID:         game.ID.String(),
		Word:       game.Word,
		Difficulty: game.Difficulty,
		ImageMode:  imageMode,
		Hints:      hints,
	}
	rbody.JSON(w, http.StatusOK, &response)
}
