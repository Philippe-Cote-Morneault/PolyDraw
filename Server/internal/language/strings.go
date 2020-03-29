package language

var strings = []byte(`
en:
  error:
    channelNotFound: "The specified channel cannot be found."
    channelInvalidStart: "Invalid parameters, start must be the lowest parameter."
    channelInvalidUrl: "Invalid parameters, the url parameters must be a number."
    channelInvalidUUID: "Invalid channel ID. It must respect the UUID format."
    channelExists: "Channel already exists."
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
    ffaRound: "The number of round must be between 1 and 5 for the free for all game mode."
    soloCount: "The number of players must be one for the game mode solo."
    playerCount: "The number of players must be between 1 and %d."
    playerMax: "You cannot have more than %d players in a game."
    groupSingle: "You already have a group created you cannot create multiple groups."
    groupNotFound: "The group could not be found."
    usernameInvalid: "The username must be between 4 and 12 characters."
    usernameFail: "The user could not be created!"
    usernameExists: "The username already exists. Please choose a different username."
    firstNameInvalid: "Invalid first name or last name, it should not be empty."
    emailInvalid: "Invalid email, it must respect the typical email format."
    passwordInvalid: "Invalid password, it must have a minimum of 8 characters."
    sessionExists: "The user already has an other session tied to this account. Please disconnect the other session before connecting."
    userNotFound: "The specified user cannot be found."
    userUpdate: "The user could not be updated."
    modifications: "No modifications are found."
    groupOwner: "Only the group owner can kick people out."
    groupMembership: "The user does not belong to a group."
    userLeaveChannel: "User is not in the channel."
    userJoinChannel: "User is already joined to the channel."
    userJoinFirst: "The user needs to join the channel first."
    channelInvalidName: "The channel name is invalid."
    groupIsFull: "The group is full."
    userSingleGroup: "The user can only join one group."
    gameMinimum: "There are only %d game(s). There needs to be a minimum of 10 games before it can start."
    notEnough: "There are not enough users to start the game."
    passwordWrong: "The password is incorrect."
    loginBearer: "The username and the bearer must be set."
    hintInvalid: "Hints are not available for this player. The drawing player needs to be a virtual player."
    hintScore: "You need at least 50 points for a hint."
    hintTime: "There needs to be at least 10 seconds for a hint to be requested."

  botlines:
    startgameAngry1: "I hate this game and I do not like the players in the group. Bad game at all!"
    startgameAngry2: "Sick of this game, but well I'm going to play since we're confined."
    startgameAngry3: "The game will start, I hope I have fun because it usually does not amuse me."

    startgameFunny1: "The game will start, fasten your seatbelts ... Or should I say your keyboards and mouses!"
    startgameFunny2: "Well, I took a new resolution before the game begins: 1080p xD. Enjoy the game ;) !"
    startgameFunny3: "The game will begin. Protect your screen, there are cheaters who want to raise their stats!"

    startgameMean1: "Before the game begins, I wish you all to lose."
    startgameMean2: "The game will start, I hope your machine will run out of battery before the end!"
    startgameMean3: "I want to warn you before the beginning, I will do everything to make you lose."

    startgameNice1: "Before the game begins, I wish you all good luck!"
    startgameNice2: "The game will start, have fun and may the best win!"
    startgameNice3: "I feel we will have fun! Let's go !"

    startgameSupportive1: "Before the game begins, remember that the important thing is to participate. Everyone is a winner in the end !"
    startgameSupportive2: "Making mistakes is worth trying! So give everything for this game!"
    startgameSupportive3: "If you feel like giving up, act like that the stats will count in your GPA. It'll give you a boost!"


    endRoundAngry1: "I still do not like this game !!!!"
    endRoundAngry2: "Still confined even after the round. I guess that we have to continue ..."
    endRoundAngry3: "The round was bad. But I feel that the next will be worse."

    endRoundFunny1: "The round is finished. No one is yet ko I hope?"
    endRoundFunny2: "Before the next round begins, may I go to the toilet please? I have to empty my RAM."
    endRoundFunny3: "Wow was this round physical. Looks like I've moved mountains. And yes, you will be surprised what a virtual bot can do."

    endRoundMean1: "The round is finished and for me, you all lost because you are all bad."
    endRoundMean2: "Your battery made it for this round, but it will crash the next one (at least I hope)."
    endRoundMean3: "What was this round guys? I have rarely seen players that bad."

    endRoundNice1: "This round was so cool. Let's go to the next aren't we ?"
    endRoundNice2: "Wow what round. I really took pleasure."
    endRoundNice3: "This round was good, but I'm sure the next one will be even better!"

    endRoundSupportive1: "Everyone did very well in this round. Congratulations and let's go to the next!"
    endRoundSupportive2: "Even if you make mistakes, do not worry. You will do better the next time."
    endRoundSupportive3: "Come on do not give up ! Your GPA is still in play!"


    playerRefAngry1: "I do not know why, but I do not like {}."
    playerRefAngry2: "It annoys me even confined, {} always plays."
    playerRefAngry3: "Please someone to kick-out {}? He annoys me."

    playerRefFunny1: "Hey {} ! Always up for a bowling game after the game?"
    playerRefFunny2: "Hey {} !  Can you lend me your body please? I want to go swimming but my motherboard is not water-resistant."
    playerRefFunny3: "I had a joke on {}. But if I say it, then you will say that I am a racist ..."

    playerRefMean1: "It's hopeless for {} ..."
    playerRefMean2: "I'll hack you {} and retrieve your personal information!"
    playerRefMean3: "Well we can pretend that {} is not playing. It wouldn't change much ..."

    playerRefNice1: "I love to play with {}!"
    playerRefNice2: "What a pleasure to play in the presence of {}"
    playerRefNice3: "Oh so cool that {} is with us in this game!"

    playerRefSupportive1: "Well done {}, keep it!"
    playerRefSupportive2: "Courage {}! Do not give up !"
    playerRefSupportive3: "Follow your efforts {} it will pay off, you'll see!"


    hintRequestAngry1: "A clue ?!! It annoys me !!"
    hintRequestAngry2: "I hope that you'll find it with this clue, instead I'm going to get angry"
    hintRequestAngry3: "The hint is useless! It annoys me !!"

    hintRequestFunny1: "Someone ordered a not overcooked clue?"
    hintRequestFunny2: "Oh a clue! Or should I say, January 10? Damn this joke would have worked in french."
    hintRequestFunny3: "Wild hint approaching. I repeat, wild hint approaching."

    hintRequestMean1: "Tssss you are desperate enough to ask for a hint?"
    hintRequestMean2: "Let's be honest, the hint will not change anything in your incompetence of finding the word."
    hintRequestMean3: "Someone asked for a clue? Ahaha I was convinced that you were not up to play this game."

    hintRequestNice1: "I hope that this hint will be able to help you."
    hintRequestNice2: "Oh cool, a new clue!"
    hintRequestNice3: "This hint will surely be useful to you!"

    hintRequestSupportive1: "Good initiative for asking a clue! Keep it up!"
    hintRequestSupportive2: "This hint will help you to find the word! Keep looking!"
    hintRequestSupportive3: "Do not lose hope, that clue might help you!"

    winRatioAngry1: "Hey {} ! You think you are the strongest because you won the most games? You are getting on my nerves !!!"
    winRatioAngry2: "I do not even know why I'm playing, {} who will win !!!"
    winRatioAngry3: "Someone to kick-out {}? He has already won enough games like that !!"

    winRatioFunny1: "{} Is the MVP of the game! (MVP is not the concept of Dropbox obviously)"
    winRatioFunny2: "This is the story of {} that plays a game. And who wins. (My jokes are automated, it's not my fault ...)"
    winRatioFunny3: "When I see the number of game {} won, I lose track. (I mean Threads of course. We do not forget that I am a only a bot)"

    winRatioMean1: "Hey {} ! It is not because you won the most games, that you're going to win that one."
    winRatioMean2: "The goal is to make lose {}, he earned enough games like that !!"
    winRatioMean3: "{} is the player to shoot! We must bring down his winning ratio."

    winRatioNice1: "{} is the best player in the game!"
    winRatioNice2: "Hey {} ! According to your win ratio you're on track to win this game too."
    winRatioNice3: "We can all applaud {}! He has the better win ratio among us!"

    winRatioSupportive1: "Hey {} ! You have to let the other win in order to encourage them!"
    winRatioSupportive2: "Courage {}! Do not give up !"
    winRatioSupportive3: "Follow your efforts {} it will pay off, you'll see!"

    
fr:
  error:
    channelNotFound: "Le canal spécifié n'a pu être trouvé."
    channelInvalidStart: "Paramètres invalides, start doit être celui le plus bas."
    channelInvalidUrl: "Paramètres invalides, les paramètres de l'url doivent être des nombres."
    channelInvalidUUID: "Identifiant de canal invalide. L'identifiant doit respecter le format UUID."
    channelExists: "Le canal existe déjà."
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
    ffaRound: "Le nombre de tours doit être entre 1 et 5 pour le type mêlée générale."
    soloCount: "Le nombre de joueurs doit être de un pour le mode de jeu solo."
    playerCount: "Le nombre de joueurs doit être entre 1 et %d."
    playerMax: "Vous ne pouvez pas avoir plus de %d joueurs dans une partie."
    groupSingle: "Vous avez déjà créé un groupe. Vous ne pouvez pas avoir plusieurs groupes à votre nom."
    groupNotFound: "Le groupe ne peut pas être trouvé."
    usernameInvalid: "Le nom d'utilisateur est doit être entre 4 et 12 caractères."
    usernameFail: "Le nom d'utilisateur n'a pu être créé!"
    usernameExists: "Le nom d'utilisateur existe déjà. Veuillez en choisir un autre."
    firstNameInvalid: "Prénom ou le nom est invalide. Il ne doit pas être vide."
    emailInvalid: "Courriel invalide, il doit respecter le format d'une adresse courriel."
    passwordInvalid: "Mot de passe invalide. Le mot de passe doit avoir un minimum de 8 caractères."
    sessionExists: "L'utilisateur possède déjà une session. Veuillez déconnecter l'autre session avant de réessayer."
    userNotFound: "L'utilisateur n'est pas trouvable."
    userUpdate: "L'utilisateur n'a pas été mis à jour."
    modifications: "Les modifications n'ont pas été trouvés."
    groupOwner: "Seulement le propriétaire du groupe peut retirer des joueurs."
    groupMembership: "L'utilisateur n'appartient pas à un groupe."
    userLeaveChannel: "L'utilisateur n'est pas dans le canal."
    userJoinChannel: "L'utilisateur est déjà présent dans le canal."
    userJoinFirst: "L'utilisateur doit rejoindre le canal avant."
    channelInvalidName: "Le nom du canal est invalide."
    groupIsFull: "Le groupe est plein."
    userSingleGroup: "L'utilisateur ne peut joindre qu'un seul groupe."
    gameMinimum: "Il n'a seulement que %d jeu(x). Un minimum de 10 jeux doivent être présents pour débuter la partie."
    notEnough: "Il n'a pas assez d'utilisateurs pour démarrer la partie."
    passwordWrong: "Le mot de passe est erroné."
    loginBearer: "Le nom d'utilisateur et le bearer doivent être dans la requête."
    hintInvalid: "Les indices ne sont pas disponible pour ce joueur. Ils ne sont disponibles que pour les joueurs virtuels."
    hintScore: "Vous avez besoin de 50 pts pour demander un indice. Vous n'avez pas assez de points."
    hintTime: "Il doit vous rester au minimum 10 secondes pour demander un indice."

  botlines:
    startgameAngry1: "J'aime pas ce jeu et je n'aime pas les joueurs du groupe. Mauvais jeu à tous !"
    startgameAngry2: "Ras le bol de ce jeu, mais bon je vais quand même jouer vu qu'on est confiné."
    startgameAngry3: "La partie va commencer, j'espère que je vais prendre du plaisir car d'habitude je ne m'amuse pas."
    
    startgameFunny1: "La partie va commencer, accrochez vos ceintures ... Ou devrais-je dire à votre clavier et souris !"
    startgameFunny2: "Bon j'ai pris une nouvelle résolution pour la partie qui va commencer : Du 1080p xD. Bonne partie ;) !"
    startgameFunny3: "La partie va commencer. Protège ton écran des tricheurs qui veulent faire monter leur stats !"
    
    startgameMean1: "Avant que la partie commence, je vous souhaite tous de perdre."
    startgameMean2: "La partie va commencer, j'espère que ton appareil va manquer de batterie avant la fin !"
    startgameMean3: "Je tiens à vous prévenir avant le commencement, je vais tout faire pour vous faire perdre."
    
    startgameNice1: "Avant que la partie commence, je vous souhaite à tous bonne chance !"
    startgameNice2: "La partie va commencer, prenez du plaisir et que le meilleur gagne !"
    startgameNice3: "Je sens qu'on va bien s'amuser ! C'est parti !"
    
    startgameSupportive1: "Avant que la partie commence, n'oubliez pas que l'important c'est de participer. Tout le monde sort gagnant de la partie !"
    startgameSupportive2: "Faire des erreurs, c'est la preuve d’avoir essayé ! Alors donne tout pour cette partie !"
    startgameSupportive3: "Si tu as envie d'abandonner, dis toi que les stats vont compter dans ton GPA. Ça va te donner un boost !"
    
    
    endRoundAngry1: "J'aime toujours pas ce jeu !!!!"
    endRoundAngry2: "Toujours confiné, même après le round. I guess qu'il faut qu'on continue..."
    endRoundAngry3: "Le round était nul. Mais je sens que le prochain va être pire."
    
    endRoundFunny1: "Le round est fini. Personne n'est encore ko j'espère ?"
    endRoundFunny2: "Avant que le prochain round commence, je peux aller aux toilettes svp ? Je dois vider ma RAM."
    endRoundFunny3: "Wow c'était physique ce round. On dirait que j'ai soulevé des montagnes. Et oui, vous serez surpris de ce qu'un bot virtuel peut faire."
    
    endRoundMean1: "Le round est fini, pour moi vous avez tous perdu car vous êtes tous nuls."
    endRoundMean2: "Ta batterie a tenu pour ce round là, mais elle va lâcher au prochain (du moins je le souhaite)."
    endRoundMean3: "C'était quoi ce round guys ? J'ai rarement vu des joueurs aussi mauvais."
    
    endRoundNice1: "Ce round était trop cool. On passe au prochain ?"
    endRoundNice2: "Wow quel round. Je prends vraiment du plaisir."
    endRoundNice3: "Ce round était bon, mais je suis sûr que le prochain sera encore mieux !"
    
    endRoundSupportive1: "Tout le monde s'est très bien débrouillé dans ce round. Bravo et passons au prochain !"
    endRoundSupportive2: "Même si tu as fait des erreurs, ne t'inquiète pas. Tu feras mieux au prochain."
    endRoundSupportive3: "Aller on lâche rien ! Ton GPA est toujours en jeu !"
    
    
    playerRefAngry1: "Je sais pas pourquoi mais {}, je ne l'aime pas."
    playerRefAngry2: "Ça m'énerve même confiné {} joue toujours."
    playerRefAngry3: "Bon on peut kick-out {} svp ? Il m'enerve."
    
    playerRefFunny1: "Hey {} ! Toujours partant pour faire un bowling après la partie ?"
    playerRefFunny2: "{} peux-tu me preter ton corps stp ? Je veux aller à la piscine mais ma carte mère est pas resistante à l'eau."
    playerRefFunny3: "J'avais une blague sur {} mais après on va dire que je suis raciste ..."
    
    playerRefMean1: "Hey {} ! Tu es nul !"
    playerRefMean2: "Je vais hack {} et récupèrer tes informations personnelles !"
    playerRefMean3: "Bon on peut faire comme si que {} jouait pas. Ça serait pareil ..."
    
    playerRefNice1: "J'adore faire des parties avec {} !"
    playerRefNice2: "Quel plaisir de jouer en présence de {}"
    playerRefNice3: "Oh trop cool que {} soit avec nous dans cette partie !"
    
    playerRefSupportive1: "Bien joué {}, continue comme ça !"
    playerRefSupportive2: "Courage {} ! Ne lâche rien !"
    playerRefSupportive3: "Poursuis tes efforts {} ça va payer, tu vas voir !"
    
    
    hintRequestAngry1: "Un indice ?!! Ça m'énerve !!"
    hintRequestAngry2: "J'espère qu'avec cet indice tu vas trouver car la je suis à deux doigts de m'énerver !!"
    hintRequestAngry3: "L'indice est nul ! Ça m'énerve !!"
    
    hintRequestFunny1: "Quelqu'un a commandé un indice pas trop cuit ?"
    hintRequestFunny2: "Oh un indice ! Ou devrais-je dire 1 10 ?"
    hintRequestFunny3: "Indice sauvage en approche. Je répète, indice sauvage en approche."
    
    hintRequestMean1: "Tssss vous êtes assez désespéré pour demander un indice ?"
    hintRequestMean2: "Soyons honnête, l'indice ne va rien changer quand à votre incompétence pour trouver le mot."
    hintRequestMean3: "Quelqu'un a demandé un indice ? Ahaha j'étais persuadé que vous n'étiez pas à la hauteur pour ce jeu."
    
    hintRequestNice1: "J'espère que cet indice va pouvoir t'aider."
    hintRequestNice2: "Ah cool, un nouvel indice !"
    hintRequestNice3: "Cet indice va sûrement t'être utile !"
    
    hintRequestSupportive1: "Bonne initiative d'avoir demandé un indice ! Continue comme ça !"
    hintRequestSupportive2: "Cet indice va t'aider pour trouver le mot ! Continue de chercher !"
    hintRequestSupportive3: "Ne perd pas espoir, voilà un indice pour t'aider !"
    
    winRatioAngry1: "Hey {} tu te crois plus fort parce que tu as gagné le plus de parties ? Tu m'énerves !!!"
    winRatioAngry2: "Je sais même pas pourquoi je joue, c'est encore {} qui va gagner !!!"
    winRatioAngry3: "Bon on peut kick-out {} svp ? Il a déjà gagné assez de parties comme ça !!"
    
    winRatioFunny1: "{} c'est le MVP de la partie ! (MVP c'est pas le concept de dropbox evidemment)"
    winRatioFunny2: "C'est l'histoire de {} qui joue à une partie. Et qui l'a gagne. (mes blagues sont automatisés, c'est pas ma faute ...)"
    winRatioFunny3: "Quand je vois le nombre de partie que {} a gagné, j'en perds le fil. (par fil j'entends thread evidemment. On oublie pas que je suis qu'un simple joueur virtuel)"
    
    winRatioMean1: "Hey {} C'est pas parce que ta gagné le plus de parties ici que tu vas gagner celle la."
    winRatioMean2: "Le but c'est de faire perdre {}, il a gagné suffisamment de parties comme ça !!"
    winRatioMean3: "{} c'est le joueur à abattre ! On doit faire baisser son ratio de victoires."
    
    winRatioNice1: "{} c'est le meilleur joueur de la partie !"
    winRatioNice2: "{} je pense que t'es bien parti pour gagner cette partie aussi."
    winRatioNice3: "On peut tous applaudir {} ! C'est lui qui a gagné le plus de parties parmi nous !"
    
    winRatioSupportive1: "{} il faut que tu laisses les autres gagner afin de les encourager !"
    winRatioSupportive2: "Courage {} ! Ne lâche rien !"
    winRatioSupportive3: "Poursuis tes efforts {} ça va payer, tu vas voir !"
`)
