package virtualplayers

import (
	"log"
)

func main() {
	log.Println("hello")
}

type player struct{}

func createAngryPlayer()       {}
func createFunnyPlayer()       {}
func createMeanPlayer()        {}
func createNicePlayer()        {}
func createSupportivePlayer()  {}

/*
Messages for :
	- Beginning of game.
	- End of round.
	- Reference to player in game {}.
	- Reference to win ratio
	- Message after hint according to personality

*/

// Beginning of game
var startAngry []string = {"J'aime pas ce jeu et je n'aime pas les joueurs du groupe. Mauvais jeu à tous !",
						   "Ras le bol de ce jeu, mais bon je vais quand même jouer vu qu'on est confiné.",
						   "La partie va commencer, j'espère que je vais prendre du plaisir car d'habitude je ne m'amuse pas."}

var startFunny []string = {"La partie va commencer, accrochez vos ceintures ... Ou devrais-je dire à votre clavier et souris !"
						   "Bon j'ai pris une nouvelle résolution pour la partie qui va commencer : Du 1080p xD. Bonne partie ;) !",
						   "La partie va commencer. Protège ton écran des tricheurs qui veulent faire monter leur stats !"}

var startMean []string = {"Avant que la partie commence, je vous souhaite tous de perdre.",
						  "La partie va commencer, j'espère que ton appareil va manquer de batterie avant la fin !",
						  "Je tiens à vous prevenir avant le commencement, je vais tout faire pour vous faire perdre."}

var startNice []string = {"Avant que la partie commence, je vous souhaite à tous bonne chance !",
						  "La partie va commencer, prenez du plaisir et que le meilleur gagne !",
						  "Je sens qu'on va bien s'amuser ! C'est parti !"}

var startSupportive []string = {"Avant que la partie commence, n'oubliez pas que l'important c'est de participer. Tout le monde sort gagnant de la partie !",
						  		"Faire des erreurs, c'est la preuve d’avoir essayé ! Alors donne tout pour cette partie !",
								"Si tu as envie d'abandonner, dis toi que les stats vont compter dans ton GPA. Ça va te donner un boost !"}

//End of round
var endAngry []string = {"J'aime toujours pas ce jeu !!!!",
						"Toujours confiné, même apres le round. I guess qu'il faut qu'on continue...",
						"Le round était nul. Mais je sens que le prochain va être pire."}

var endFunny []string = {"Le round est fini. Personne n'est encore ko j'espere ?"
						"Avant que le prochain round commence, je peux aller aux toilettes svp ? Je dois vider ma RAM.",
						"Wow c'etait physique ce round. On dirait que j'ai soulevé des montagnes. Et oui, vous serez surpris de ce qu'un bot virtuel peut faire."}

var endMean []string = {"Le round est fini, pour moi vous avez tous perdu car vous êtes tous nuls.",
						"Ta batterie a tenu pour ce round la, mais elle va lacher au prochain (du moins je le souhaite).",
						"C'était quoi ce round guys ? J'ai rarement vu des joueurs aussi mauvais."}

var endNice []string = {"Ce round était trop cool. On passe au prochain ?",
						"Wow quel round. Je prends vraiment du plaisir.",
						"Ce round était bon, mais je suis sûr que le prochain sera encore mieux !"}

var endSupportive []string = {"Tout le monde s'est très bien debrouillé dans ce round. Bravo et passons au prochain !",
							  "Même si tu as fait des erreurs, ne t'inquiète pas. Tu feras mieux au prochain.",
							  "Aller on lâche rien ! Ton GPA est toujours en jeu !"}


// Reference to player in game {}.
var ingamePlayerAngry []string = {"Je sais pas pourquoi mais {}, je ne l'aime pas.",
								"Ça m'énerve même confiné {} joue toujours.",
								"Bon on peut kick-out {} svp ? Il m'enerve."}

var ingamePlayerFunny []string = {"Hey {} ! Toujours partant pour faire un bowling après la partie ?"
								"{} peux-tu me preter ton corps stp ? Je veux aller à la piscine mais ma carte mère est pas resistante à l'eau.",
								"J'avais une blague sur {} mais après on va dire que je suis raciste ..."}

var ingamePlayerMean []string = {"Hey {} ! Tu es nul !",
								"Je vais hack {} et récupèrer tes informations personnelles !",
								"Bon on peut faire comme si que {} jouait pas. Ça serait pareil ..."}

var ingamePlayerNice []string = {"J'adore faire des parties avec {} !",
								"Quel plaisir de jouer en présence de {}",
								"Oh trop cool que {} soit avec nous dasn cette partie !"}

var ingamePlayerSupportive []string = {"Bien joué {}, continue comme ça !",
							 			"Courage {} ! Ne lâche rien !",
										"Poursuis tes efforts {} ça va payer, tu vas voit !"}
										  

// Reference to player already played with {}.
var ratioAngry []string = {"Hey {} tu te crois plus fort parce que tu as gagné le plus de parties ? Tu m'énerves !!!",
							"Je sais même pas pourquoi je joue, c'est encore {} qui va gagner !!!",
							"Bon on peut kick-out {} svp ? Il a déjà gagné assez de parties comme ça !!"}

var ratioFunny []string = {"{} c'est le MVP de la partie ! (MVP c'est pas le concept de dropbox evidemment)"
							"C'est l'histoire de {} qui joue à une partie. Et qui l'a gagne. (mes blagues sont automatisés, c'est pas ma faute ...)",
							"Quand je vois le nombre de partie que {} a gagné, j'en perds le fil. (par fil j'entends thread evidemment. On oublie pas que je suis qu'un simple joueur virtuel)"}

var ratioMean []string = {"Hey {} C'est pas parce que ta gagné le plus de parties ici que tu vas gagner celle la.",
							"Le but c'est de faire perdre {}, il a gagné suffisamment de parties comme ça !!",
							"{} c'est le joueur à abattre ! On doit faire baisser son ratio de victoires."}

var ratioNice []string = {"{} c'est le meilleur joueur de la partie !",
							"{} je pense que t'es bien parti pour gagner cette partie aussi.",
							"On peut tous applaudir {} ! C'est lui qui a gagné le plus de parties parmi nous !"}

var ratioSupportive []string = {"{} il faut que tu laisses les autres gagner afin de les encourager !",
							 	"Courage {} ! Ne lâche rien !",
							  	"Poursuis tes efforts {} ça va payer, tu vas voit !"}


// Reference to player already played with {}.
var hintAngry []string = {"Un indice ?!! Ça m'énerve !!",
							"J'espere qu'avec cet indice tu vas trouver car la je suis à deux doigts de m'énerver !!",
							"L'indice est nul ! Ça m'énerve !!"}

var hintFunny []string = {"Quelqu'un a commandé un indice pas trop cuit ?"
						  "Oh un indice ! Ou devrais-je dire 1 10 ?",
						"Indice sauvage en approche. Je répète, indice sauvage en approche."}

var hintMean []string = {"Tssss vous êtes assez désespéré pour demander un indice ?",
						"Soyons honnète, l'indice ne va rien changer quand à votre incompétence pour trouver le mot.",
						"Quelqu'un a demandé un indice ? Ahaha j'étais persuadé que vous n'étiez pas à la hauteur pour ce jeu."}

var hintNice []string = {"J'espère que cet indice va pouvoir t'aider.",
						"Ah cool, un nouvel indice !",
						"Cet indice va sûrement t'être utile !"}

var hintSupportive []string = {"Bonne initiative d'avoir demandé un indice ! Continue comme ça !",
							 	"Cet indice va t'aider pour trouver le mot ! Continue de chercher !",
							  	"Ne perd pas espoir, voila un indice pour t'aider !"}