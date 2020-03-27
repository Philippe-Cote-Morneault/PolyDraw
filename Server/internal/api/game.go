package api

import (
	"encoding/json"
	"net/http"
	"strconv"
	"strings"

	"gitlab.com/jigsawcorp/log3900/internal/services/potrace"

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

type gameImageRequestUpdate struct {
	Mode       *int
	BrushSize  *int
	BlackLevel *float64
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
		rbody.JSONError(w, http.StatusBadRequest, language.MustGetRest("error.wordBlank", r))
		return
	}

	//Validate if the word is validate
	wordLower := strings.ToLower(request.Word)
	if wordvalidator.IsBlacklist(wordLower, language.EN) {
		rbody.JSONError(w, http.StatusBadRequest, language.MustGetRest("error.wordBlacklist", r))
		return
	}

	if !wordvalidator.IsWord(wordLower, language.EN) {
		rbody.JSONError(w, http.StatusBadRequest, language.MustGetRest("error.wordInvalid", r))
		return
	}

	if request.Difficulty < 0 || request.Difficulty > 3 {
		rbody.JSONError(w, http.StatusBadRequest, language.MustGetRest("error.difficultyRange", r))
		return
	}

	if len(request.Hints) < 1 || len(request.Hints) > 10 {
		rbody.JSONError(w, http.StatusBadRequest, language.MustGetRest("error.hintLimits", r))
		return
	}

	//Check for all the hints that they are valid.
	var hints []*model.GameHint
	for i := range request.Hints {
		if strings.TrimSpace(request.Hints[i]) == "" {
			rbody.JSONError(w, http.StatusBadRequest, language.MustGetRest("error.hintEmpty", r))
			return
		}
		//Check if the word is not in the string
		hintLower := strings.ToLower(request.Hints[i])
		if strings.Contains(hintLower, wordLower) {
			rbody.JSONError(w, http.StatusBadRequest, language.MustGetRest("error.hintWord", r))
			return
		}
		currentHint := strings.TrimSpace(hintLower)
		for j, hint := range request.Hints {
			hintLower := strings.TrimSpace(strings.ToLower(hint))
			if hintLower == currentHint && j != i {
				rbody.JSONError(w, http.StatusBadRequest, language.MustGetRest("error.hintDuplicate", r))
				return
			}
		}

		hints = append(hints, &model.GameHint{
			Hint: request.Hints[i],
		})
	}
	//TODO language from headers to include
	game := model.Game{
		Word:       wordLower,
		Difficulty: request.Difficulty,
		Language:   language.EN,
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
		rbody.JSONError(w, http.StatusBadRequest, "The mode is not set. Please set the number of the mode between 0-7.")
		return
	}

	modeInt, err := strconv.Atoi(mode)
	if err != nil {
		rbody.JSONError(w, http.StatusBadRequest, "The mode is not a number. The mode must be between 0-7.")
		return
	}

	if modeInt > 7 || modeInt < 0 {
		rbody.JSONError(w, http.StatusBadRequest, "The mode must be between 0-7.")
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

		brushsizeStr := r.FormValue("brushsize")
		brushsize, err := strconv.Atoi(brushsizeStr)
		if brushsizeStr == "" {
			rbody.JSONError(w, http.StatusBadRequest, "The brushsize must be set when uploading a non vector image.")
			return
		}
		if err != nil {
			rbody.JSONError(w, http.StatusBadRequest, "The brushsize must be a integer number.")
		}

		if brushsize > 100 || brushsize < 1 {
			rbody.JSONError(w, http.StatusBadRequest, "The brushsize must be between 1 and 100.")
			return
		}

		image.BrushSize = brushsize

		if mime == "text/xml; charset=utf-8" {
			//Load svg
			image.OriginalFile = keyFile
			newFile, err := datastore.Copy(keyFile)
			if err != nil {
				rbody.JSONError(w, http.StatusBadRequest, err.Error())
				return
			}
			image.SVGFile = newFile
		} else {
			//Load jpg
			image.ImageFile = keyFile

			if modeInt == 0 {
				rbody.JSONError(w, http.StatusBadRequest, "The mode can't be set to manual.")
				return
			}

			//Check if the blackness level is set
			blackLevelStr := r.FormValue("blacklevel")
			blackLevel, err := strconv.ParseFloat(blackLevelStr, 64)
			if blackLevelStr == "" {
				rbody.JSONError(w, http.StatusBadRequest, "The blacklevel must be set when uploading a non vector image.")
				return
			}
			if blackLevel > 1 || blackLevel < 0 {
				rbody.JSONError(w, http.StatusBadRequest, "The blacklevel must be between 0 and 1.")
				return
			}

			image.BlackLevel = blackLevel

			svgKey, err := potrace.Trace(keyFile, blackLevel)
			if err != nil {
				rbody.JSONError(w, http.StatusBadRequest, err.Error())
				return
			}
			image.OriginalFile = svgKey
			newFile, err := datastore.Copy(svgKey)
			if err != nil {
				rbody.JSONError(w, http.StatusBadRequest, err.Error())
				return
			}

			err = potrace.Translate(newFile, brushsize, modeInt, false)
			if err != nil {
				rbody.JSONError(w, http.StatusBadRequest, err.Error())
				return
			}
			image.SVGFile = newFile

		}
		game.Image = &image
		model.DB().Save(&game)
		rbody.JSON(w, http.StatusOK, "OK")
	default:
		rbody.JSONError(w, http.StatusBadRequest, "The file is not a valid type")
		return
	}

}

//DeleteGame used to remove a game from the list
func DeleteGame(w http.ResponseWriter, r *http.Request) {
	vars := mux.Vars(r)
	gameID, err := uuid.Parse(vars["id"])

	if err != nil {
		rbody.JSONError(w, http.StatusBadRequest, "A valid uuid must be set")
		return
	}

	//Check if the game exists
	game := model.Game{}
	model.DB().Preload("Image").Where("id = ?", gameID).First(&game)

	if game.ID == uuid.Nil {
		rbody.JSONError(w, http.StatusNotFound, "The game cannot be found. Please check if the id is valid.")
		return
	}

	model.DB().Delete(&game)
	rbody.JSON(w, http.StatusOK, "OK")

}

//PutGameImage used to update the mode of the picture
func PutGameImage(w http.ResponseWriter, r *http.Request) {
	vars := mux.Vars(r)
	gameID, err := uuid.Parse(vars["id"])
	if err != nil {
		rbody.JSONError(w, http.StatusBadRequest, "A valid uuid must be set")
		return
	}

	//Check if the game exists
	game := model.Game{}
	model.DB().Preload("Image").Where("id = ?", gameID).First(&game)

	if game.ID == uuid.Nil {
		rbody.JSONError(w, http.StatusNotFound, "The game cannot be found. Please check if the id is valid.")
		return
	}
	//Parse json request
	var request gameImageRequestUpdate
	decoder := json.NewDecoder(r.Body)
	err = decoder.Decode(&request)
	if err != nil {
		rbody.JSONError(w, http.StatusBadRequest, err.Error())
		return
	}

	xmlNeedsUpdating := false
	updateOnlySVG := true
	if request.Mode != nil {
		modeInt := *request.Mode
		if modeInt > 7 || modeInt < 0 {
			rbody.JSONError(w, http.StatusBadRequest, "The mode must be between 0-7.")
			return
		}
		if game.Image.ImageFile != "" && modeInt == 0 {
			rbody.JSONError(w, http.StatusBadRequest, "The mode can't be set to manual.")
			return
		}
		xmlNeedsUpdating = true
		game.Image.Mode = modeInt
	}

	if game.Image.ImageFile != "" {
		//Error validation for Blacklevel & BrushSize
		if request.BlackLevel != nil {
			if *request.BlackLevel > 1 || *request.BlackLevel < 0 {
				rbody.JSONError(w, http.StatusBadRequest, "The blacklevel must be between 0 and 1.")
				return
			}
			game.Image.BlackLevel = *request.BlackLevel
			xmlNeedsUpdating = true
		}
		if request.BrushSize != nil {
			if *request.BrushSize > 100 || *request.BrushSize < 1 {
				rbody.JSONError(w, http.StatusBadRequest, "The brushsize must be between 1 and 100.")
				return
			}
			game.Image.BrushSize = *request.BrushSize
			xmlNeedsUpdating = true
		}

		if request.BlackLevel != nil {
			svgKey, err := potrace.Trace(game.Image.ImageFile, game.Image.BlackLevel)
			if err != nil {
				rbody.JSONError(w, http.StatusBadRequest, err.Error())
				return
			}
			game.Image.OriginalFile = svgKey
			newFile, err := datastore.Copy(svgKey)
			if err != nil {
				rbody.JSONError(w, http.StatusBadRequest, err.Error())
				return
			}
			game.Image.SVGFile = newFile
			xmlNeedsUpdating = true
			updateOnlySVG = false
		}
	} else {
		game.Image.BrushSize = -1
	}
	if xmlNeedsUpdating {
		newFile, err := datastore.Copy(game.Image.OriginalFile)
		if err != nil {
			rbody.JSONError(w, http.StatusBadRequest, err.Error())
			return
		}
		game.Image.SVGFile = newFile
		err = potrace.Translate(game.Image.SVGFile, game.Image.BrushSize, game.Image.Mode, updateOnlySVG)
		if err != nil {
			rbody.JSONError(w, http.StatusBadRequest, err.Error())
			return
		}
	}
	model.DB().Save(&game)
	rbody.JSON(w, http.StatusOK, "OK")
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
