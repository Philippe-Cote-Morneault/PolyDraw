package api

import (
	"encoding/json"
	"net/http"
	"strings"

	"gitlab.com/jigsawcorp/log3900/internal/language"
	"gitlab.com/jigsawcorp/log3900/internal/wordvalidator"
	"gitlab.com/jigsawcorp/log3900/model"
	"gitlab.com/jigsawcorp/log3900/pkg/rbody"
)

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
		File:       "None",
	}
	model.DB().Save(&game)
	rbody.JSON(w, http.StatusOK, &gameResponseCreation{GameID: game.ID.String()})

}
