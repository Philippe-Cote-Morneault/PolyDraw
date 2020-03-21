package virtualplayer

/*
A struct for each line that bot might say :
	- Beginning of game.
	- End of round.
	- Message after hint according to personality
	- Reference to player in game {}.
	- Reference to win ratio
*/

var iStartGameLines lines
var iEndRoundLines lines
var iPlayerRefLines lines
var iHintLines lines
var iWinRatioLines lines

type lines struct {
	Angry      []string
	Funny      []string
	Mean       []string
	Nice       []string
	Supportive []string
}

func (l *lines) Init() {
	l.Angry = make([]string, 0)
	l.Funny = make([]string, 0)
	l.Mean = make([]string, 0)
	l.Nice = make([]string, 0)
	l.Supportive = make([]string, 0)
}

func initStartGameLines(l *lines) {

	l.Angry = append(l.Angry, "J'aime pas ce jeu et je n'aime pas les joueurs du groupe. Mauvais jeu à tous !")
	l.Angry = append(l.Angry, "Ras le bol de ce jeu, mais bon je vais quand même jouer vu qu'on est confiné.")
	l.Angry = append(l.Angry, "La partie va commencer, j'espère que je vais prendre du plaisir car d'habitude je ne m'amuse pas.")

	l.Funny = append(l.Funny, "La partie va commencer, accrochez vos ceintures ... Ou devrais-je dire à votre clavier et souris !")
	l.Funny = append(l.Funny, "Bon j'ai pris une nouvelle résolution pour la partie qui va commencer : Du 1080p xD. Bonne partie ;) !")
	l.Funny = append(l.Funny, "La partie va commencer. Protège ton écran des tricheurs qui veulent faire monter leur stats !")

	l.Mean = append(l.Mean, "Avant que la partie commence, je vous souhaite tous de perdre.")
	l.Mean = append(l.Mean, "La partie va commencer, j'espère que ton appareil va manquer de batterie avant la fin !")
	l.Mean = append(l.Mean, "Je tiens à vous prevenir avant le commencement, je vais tout faire pour vous faire perdre.")

	l.Nice = append(l.Nice, "Avant que la partie commence, je vous souhaite à tous bonne chance !")
	l.Nice = append(l.Nice, "La partie va commencer, prenez du plaisir et que le meilleur gagne !")
	l.Nice = append(l.Nice, "Je sens qu'on va bien s'amuser ! C'est parti !")

	l.Supportive = append(l.Supportive, "Avant que la partie commence, n'oubliez pas que l'important c'est de participer. Tout le monde sort gagnant de la partie !")
	l.Supportive = append(l.Supportive, "Faire des erreurs, c'est la preuve d’avoir essayé ! Alors donne tout pour cette partie !")
	l.Supportive = append(l.Supportive, "Si tu as envie d'abandonner, dis toi que les stats vont compter dans ton GPA. Ça va te donner un boost !")
}

func initEndRoundLines(l *lines) {

	l.Angry = append(l.Angry, "J'aime toujours pas ce jeu !!!!")
	l.Angry = append(l.Angry, "Toujours confiné, même apres le round. I guess qu'il faut qu'on continue...")
	l.Angry = append(l.Angry, "Le round était nul. Mais je sens que le prochain va être pire.")

	l.Funny = append(l.Funny, "Le round est fini. Personne n'est encore ko j'espere ?")
	l.Funny = append(l.Funny, "Avant que le prochain round commence, je peux aller aux toilettes svp ? Je dois vider ma RAM.")
	l.Funny = append(l.Funny, "Wow c'etait physique ce round. On dirait que j'ai soulevé des montagnes. Et oui, vous serez surpris de ce qu'un bot virtuel peut faire.")

	l.Mean = append(l.Mean, "Le round est fini, pour moi vous avez tous perdu car vous êtes tous nuls.")
	l.Mean = append(l.Mean, "Ta batterie a tenu pour ce round la, mais elle va lacher au prochain (du moins je le souhaite).")
	l.Mean = append(l.Mean, "C'était quoi ce round guys ? J'ai rarement vu des joueurs aussi mauvais.")

	l.Nice = append(l.Nice, "Ce round était trop cool. On passe au prochain ?")
	l.Nice = append(l.Nice, "Wow quel round. Je prends vraiment du plaisir.")
	l.Nice = append(l.Nice, "Ce round était bon, mais je suis sûr que le prochain sera encore mieux !")

	l.Supportive = append(l.Supportive, "Tout le monde s'est très bien debrouillé dans ce round. Bravo et passons au prochain !")
	l.Supportive = append(l.Supportive, "Même si tu as fait des erreurs, ne t'inquiète pas. Tu feras mieux au prochain.")
	l.Supportive = append(l.Supportive, "Aller on lâche rien ! Ton GPA est toujours en jeu !")
}

func initPlayerRefLines(l *lines) {

	l.Angry = append(l.Angry, "Je sais pas pourquoi mais {}, je ne l'aime pas.")
	l.Angry = append(l.Angry, "Ça m'énerve même confiné {} joue toujours.")
	l.Angry = append(l.Angry, "Bon on peut kick-out {} svp ? Il m'enerve.")

	l.Funny = append(l.Funny, "Hey {} ! Toujours partant pour faire un bowling après la partie ?")
	l.Funny = append(l.Funny, "{} peux-tu me preter ton corps stp ? Je veux aller à la piscine mais ma carte mère est pas resistante à l'eau.")
	l.Funny = append(l.Funny, "J'avais une blague sur {} mais après on va dire que je suis raciste ...")

	l.Mean = append(l.Mean, "Hey {} ! Tu es nul !")
	l.Mean = append(l.Mean, "Je vais hack {} et récupèrer tes informations personnelles !")
	l.Mean = append(l.Mean, "Bon on peut faire comme si que {} jouait pas. Ça serait pareil ...")

	l.Nice = append(l.Nice, "J'adore faire des parties avec {} !")
	l.Nice = append(l.Nice, "Quel plaisir de jouer en présence de {}")
	l.Nice = append(l.Nice, "Oh trop cool que {} soit avec nous dasn cette partie !")

	l.Supportive = append(l.Supportive, "Bien joué {}, continue comme ça !")
	l.Supportive = append(l.Supportive, "Courage {} ! Ne lâche rien !")
	l.Supportive = append(l.Supportive, "Poursuis tes efforts {} ça va payer, tu vas voir !")
}

func initHintLines(l *lines) {

	l.Angry = []string{"Un indice ?!! Ça m'énerve !!",
		"J'espere qu'avec cet indice tu vas trouver car la je suis à deux doigts de m'énerver !!",
		"L'indice est nul ! Ça m'énerve !!"}

	l.Funny = []string{"Quelqu'un a commandé un indice pas trop cuit ?",
		"Oh un indice ! Ou devrais-je dire 1 10 ?",
		"Indice sauvage en approche. Je répète, indice sauvage en approche."}

	l.Mean = []string{"Tssss vous êtes assez désespéré pour demander un indice ?",
		"Soyons honnète, l'indice ne va rien changer quand à votre incompétence pour trouver le mot.",
		"Quelqu'un a demandé un indice ? Ahaha j'étais persuadé que vous n'étiez pas à la hauteur pour ce jeu."}

	l.Nice = []string{"J'espère que cet indice va pouvoir t'aider.",
		"Ah cool, un nouvel indice !",
		"Cet indice va sûrement t'être utile !"}

	l.Supportive = []string{"Bonne initiative d'avoir demandé un indice ! Continue comme ça !",
		"Cet indice va t'aider pour trouver le mot ! Continue de chercher !",
		"Ne perd pas espoir, voila un indice pour t'aider !"}
}

func initRatioLines(l *lines) {

	l.Angry = append(l.Angry, "Hey {} tu te crois plus fort parce que tu as gagné le plus de parties ? Tu m'énerves !!!")
	l.Angry = append(l.Angry, "Je sais même pas pourquoi je joue, c'est encore {} qui va gagner !!!")
	l.Angry = append(l.Angry, "Bon on peut kick-out {} svp ? Il a déjà gagné assez de parties comme ça !!")

	l.Funny = append(l.Funny, "{} c'est le MVP de la partie ! (MVP c'est pas le concept de dropbox evidemment)")
	l.Funny = append(l.Funny, "C'est l'histoire de {} qui joue à une partie. Et qui l'a gagne. (mes blagues sont automatisés, c'est pas ma faute ...)")
	l.Funny = append(l.Funny, "Quand je vois le nombre de partie que {} a gagné, j'en perds le fil. (par fil j'entends thread evidemment. On oublie pas que je suis qu'un simple joueur virtuel)")

	l.Mean = append(l.Mean, "Hey {} C'est pas parce que ta gagné le plus de parties ici que tu vas gagner celle la.")
	l.Mean = append(l.Mean, "Le but c'est de faire perdre {}, il a gagné suffisamment de parties comme ça !!")
	l.Mean = append(l.Mean, "{} c'est le joueur à abattre ! On doit faire baisser son ratio de victoires.")

	l.Nice = append(l.Nice, "{} c'est le meilleur joueur de la partie !")
	l.Nice = append(l.Nice, "{} je pense que t'es bien parti pour gagner cette partie aussi.")
	l.Nice = append(l.Nice, "On peut tous applaudir {} ! C'est lui qui a gagné le plus de parties parmi nous !")

	l.Supportive = append(l.Supportive, "{} il faut que tu laisses les autres gagner afin de les encourager !")
	l.Supportive = append(l.Supportive, "Courage {} ! Ne lâche rien !")
	l.Supportive = append(l.Supportive, "Poursuis tes efforts {} ça va payer, tu vas voir !")
}

func initLines() {
	initStartGameLines(&iStartGameLines)
	initEndRoundLines(&iEndRoundLines)
	initPlayerRefLines(&iPlayerRefLines)
	initHintLines(&iHintLines)
	initRatioLines(&iWinRatioLines)
}
