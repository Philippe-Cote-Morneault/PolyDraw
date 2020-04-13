# Serveur PolyDraw
Le serveur pour l'application est fait pour s'exécuter sur Docker. En effet, l'image de Docker contient tous les exécutables ainsi que les ressources nécessaires pour exécuter le serveur.

## Obtention de l'image
Le serveur requiert plusieurs composantes connexes. Un fichier docker-compose.yml est fourni dans le dossier afin de partir le serveur facilement en local. L'image compilée est sauvegardée dans le docker repository privé de Gitlab. Il faut donc se connecter à l’entrepôt pour accéder à l'image. On peut s'y connecter avec la commande ci-dessous. Vos comptes ont été ajoutés dans l'entrepôt Gitlab avec les permissions de lecture seule. Vous avez donc accès à l'image.

```docker login registry.gitlab.com```

Dans le cas où l'image n'est pas accessible pour des problèmes de connexions à l'entrepôt, on peut utiliser le [lien](https://log3900.blob.core.windows.net/polydraw/docker/server_latest.tar.gz ) suivant pour télécharger l'image directement.
Il faut ensuite importer l’image dans docker sous le nom suivant `registry.gitlab.com/jigsawcorp/log3900/server:latest`

## Démarrage du serveur
On peut démarrer le serveur en utilisant la commande suivante.
 
```docker-compose up```

Le premier démarrage peut prendre un certain temps. Il s'agit du temps nécessaire pour importer les dictionnaires de mots dans Redis.

## Connexion des clients
Il est a noter que les clients sont configurés pour communiquer avec le serveur à l'adresse suivante: `log3900-203.canadacentral.cloudapp.azure.com`. Il faut donc modifier le code source des clients si nous voulons communiquer avec un autre serveur.

Ce serveur est hébergé sur Azure. Il sera opérationnel durant toute la période de correction. Le serveur utilise le port 5000 sur HTTP et 5001 sur socket pour communiquer aux clients. Le serveur devrait répondre `"REST SERVER"` lorsqu'on fait une requête HTTP sur le port 5000.