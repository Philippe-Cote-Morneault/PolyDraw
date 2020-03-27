package language

var strings = []byte(`
en:
	error:
		channelNotFound: "The specified channel cannot be found."
		channelInvalidStart: "Invalid parameters, start must be the lowest parameter."
		channelInvalidUrl: "Invalid parameters, the url parameters must be a number."
		channelInvalidUUID: "Invalid channel ID. It must respect the UUID format."
		wordBlank: "The word cannot be blank."
		wordBlacklist: "This word is not allowed!"
		wordInvalid: "This is not a word, please enter a valid word."
		difficultyRange: "The difficulty must be between 0 and 3."
		hintLimits: "The game must have at least 1 hint and not more than 10."
		hintEmpty: "The hints cannot be empty."
		hintWord: "The hint cannot contain the word."
		hintDuplicate: "The hint cannot be the same."
		invalidUUID: "A valid uuid must be set."
		gameNotFound: "The game cannot be found. Please check if the id is valid."
		gameFileInvalid: "The file is not valid"
		fileNotReadable: "The file cannot be read."
		fileInvalidType: "The file is not a valid type"
fr:
	error:
		channelNotFound: "Le canal spécifié n'a pu être trouvé."
		channelInvalidStart: "Paramètres invalides, start doit être celui le plus bas."
		channelInvalidUrl: "Paramètres invalides, les paramètres de l'url doivent être des nombres."
		channelInvalidUUID: "Identifiant de canal invalide. L'identifiant doit respecter le format UUID."
		wordBlank: "Le mot ne peut pas être vide."
		wordBlacklist: "Le mot n'est pas autorisé!"
		wordInvalid: "Ceci n'est pas un mot valide, veuillez entrer un mot valide."
		difficultyRange: "La difficulté doit être entre 0 et 3."
		hintLimits: "Le jeu doit avoir entre 1 et 10 indices maximums."
		hintEmpty: "Les indices ne peuvent être vides."
		hintWord: "L'indice ne peut pas contenir le mot à deviner."
		hintDuplicate: "L'indice ne peut être indentique aux autres."
		invalidUUID: "Un UUID valide doit être utilisé."
		gameNotFound: "Le jeu n'a pas été trouvé. Vérifiez si l'identifiant est valide."
		gameFileInvalid: "Le fichier n'est pas valide."
		fileNotReadable: "Le fichier ne peut être lu."
		fileInvalidType: "Le fichier n'est pas un type valide."
`)
