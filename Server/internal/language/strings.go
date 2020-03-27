package language

var strings = []byte(`
en:
	error:
		channelNotFound: "The specified channel cannot be found."
		channelInvalidStart: "Invalid parameters, start must be the lowest parameter."
		channelInvalidUrl: "Invalid parameters, the url parameters must be a number."
		channelInvalidUUID: "Invalid channel ID. It must respect the UUID format."

fr:
	error:
		channelNotFound: "Le canal spécifié n'a pu être trouvé."
		channelInvalidStart: "Paramètres invalides, start doit être celui le plus bas."
		channelInvalidUrl: "Paramètres invalides, les paramètres de l'url doivent être des nombres."
		channelInvalidUUID: "Identifiant de canal invalide. L'identifiant doit respecter le format UUID."
`)
