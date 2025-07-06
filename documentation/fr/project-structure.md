# Structure du projet

Les fichiers sont séparés en deux grandes catégories: le [frontend](#frontend) et le [backend](#backend).

- [Structure du projet](#structure-du-projet)
  - [Frontend](#frontend)
    - [Structure interne des composants](#structure-interne-des-composants)
  - [Backend](#backend)
    - [Structure interne](#structure-interne)
      - [Contrôlleur](#contrôlleur)
      - [Service](#service)
      - [Logique](#logique)

---

## Frontend

> [!IMPORTANT]
> Le framework [Angular](https://angular.dev) est utilisé pour le frontend. Si vous n'êtes pas familier avec ce framework, il est recommandé de faire le [tutoriel](https://angular.dev/tutorials) disponible sur le site officiel.

Dans l'arborescence des fichiers, tout les fichiers et dossiers appartiennent au frontend sauf:
- Le dossier [tests](/tests/).
- Le dossier [documentation](/documentation/).
- Le dossier [backend](/backend/).
- Les trois dossiers de configurations [.angular](/.angular/), [.github](/.github) et [.vscode](/.vscode).
- Les fichiers [LICENSE](/LICENSE) et [README.md](/README.md).

> [!NOTE]
> Un dossier [.git](/.git/) existe aussi si le projet à été télécharger. C'est ainsil que GitHub détecte les changemnts dans le code, et le synchronise avec sa version en ligne.

Les fichiers directements dans le [dossier principal](/) sont tous des fichiers de configurations, à ne pas modifier à moins que vous savez ce que vous faites.

<details>
<summary><b>Documentations associés</b></summary>

> Voici les différents liens vers les documentations associés si vous êtes intéressé(e):
> - [**.editorconfig**](/.editorconfig): https://editorconfig.org/
> - [**.gitignore**](/.gitignore): https://git-scm.com/docs/gitignore
> - [**.postcssrc.json**](/.postcssrc.json): https://postcss.org/
> - [**angular.json**](/angular.json): https://angular.dev/reference/configs/workspace-config
> - [**eslint.config.js**](/eslint.config.js): https://eslint.org/docs/latest/use/configure/
> - [**package.json**](/package.json) et [**package-lock.json**](/package-lock.json): https://docs.npmjs.com/cli/v11/configuring-npm/package-json
> - [**tailwind.config.js**](/tailwind.config.js): https://v2.tailwindcss.com/docs/configuration
> - [**tsconfig.app.json**](/tsconfig.app.json), [**tsconfig.json**](/tsconfig.json) et [**tsconfig.spec.json**](/tsconfig.spec.json) : https://www.typescriptlang.org/docs/handbook/tsconfig-json.html
>
> Et pour ce qui est à venir:
> - Fichiers en ".scss": https://sass-lang.com/guide/
</details>
<br>

Le dossier [public](/public/) contient toutes les ressources "publique", accessible directement via un URL.
> Par exemple, le fichier [favicon.ico](/public/favicon.ico) est accessible avec l'URL "http://localhost:4200/favicon.ico", quand le serveur frontend est lancé.

L'entièreté du code du frontend est donc situé dans le dossier [src](/src/):
- [index.html](/src/index.html): Fichier d'entré, la racine, du site web.
- [main.ts](/src/main.ts): Fichier d'entré d'Angular.
- [style.scss](/src/styles.scss): Fichier de style appliqué globalement à l'ensemble du site web.
- [environments](/src/environments/): Dossier des fichiers de configuration du projet. Voir [README-fr.md: Configuration initiale](/documentation/fr/README-fr.md#configuration-initiale).
- [models](/src/models/): Liste de fichiers qui décrivent les types et interface utilisés globallement.
- [services](/src/services/): Dossier où les services utilisés par le projet sont situés. Voir [Angular: Service](https://angular.dev/ecosystem/service-workers).
- [shared](/src/shared/): Liste de composants Angular utilisés globalement, et qui n'ont pas de page associées spécifiquement.
- [utils](/src/utils/): Liste de fichiers contenant des fonctions utiles qui sont réutilisées et partagées à travers plusieurs composants.

Il reste donc le dossier [app](/src/app/), qui contient le coeur du frontend:
- [main](/src/app/main/): Page d'acceuil.
- Les quatres fichiers [app.component](/src/app/app.component.ts) représente le composant "app", qui est le premier composant que charge Angular.
- [app.config.ts](/src/app/app.config.ts): Contient des [providers](https://angular.dev/guide/di/dependency-injection-providers), permettant certains comportements, notamment un utilitaire de requêtes HTTP.
- [app.route.ts](/src/app/app.routes.ts): Fichier contenant toutes les routes du site web, spécifiant le titre à afficher, le composant à utilisé, l'URL et plus encore. Voir [Angular: Routing](https://angular.dev/guide/routing).

### Structure interne des composants

Un composant a quatre fichiers, qui peuvent être générés en suivant les étapes suivantes:
```shell
cd /src/chermin/vers/parent
ng generate component nom
ng g c nom # cette ligne est identique à celle ci-dessus
```

La première ligne permet de créer le composant là où vous êtes situé dans l'arborescence du projet.  
La deuxième ligne va créer un composant nommé "*nom*", dans un dossier du même nom, avec les quatre fichiers cités précédemments.

Les quatre fichiers sont:
- *nom*.component.html: Le fichier HTML du composant, c'est la partie visible pour les utilisateurs.
- *nom*.component.scss: Le fichier de style pour le HTML, pour ce componsant uniquement. Voir [Angular: Encapsulation](https://angular.dev/guide/components/styling).
- *nom*.component.spec.ts: Le fichier de test du composant.
- *nom*.component.ts: Le fichier de script du composant, où se situe la logique.

Comme avec le composant [main](/src/app/main/), certains composants ont eux même un ou plusieurs composants dans leur dossier. En général, un composant directement dans [app](/src/app/) est pointé par une [route](/src/app/app.routes.ts). Mais si il requiert un sous composant pour alléger le code, ou pour se répéter, mais que ce composant n'est utilisé que par sont parent, alors il est mis dans le dossier du composant parent, ce qui est le cas de [simplex-response](/src/app/main/simplex-response/).

<div align="right"><kbd><a href="#structure-du-projet">Retour en haut</a></kbd></div>

---

## Backend


> [!IMPORTANT]
> Le framework [SpringBoot](https://spring.io/projects/spring-boot) est utilisé pour le backend. Si vous n'êtes pas familier avec ce framework, il est recommandé de lire [tutoriel](https://docs.spring.io/spring-boot/tutorial/first-application/index.html) disponible sur le site officiel.

Dans l'arborescence des fichiers, seul le fossier [backend](/backend/) contient le code du backend.

Les fichiers [build.sh](/backend/build.sh), [mvnw.cmd](/backend/mvnw.cmd) et [mvnw.sh](/backend/mvnw.sh) peuvent être utilisés pour lancer le backend (voir [README-fr.md: Développement](/documentation/fr/README-fr.md#développement)).

Le fichier [pom.xml](/backend/pom.xml) contient la liste des paquets utilisés par Maven pour compiler le projet ([documentation sur les POM](https://maven.apache.org/guides/introduction/introduction-to-the-pom.html)). Il remplis le même rôle que [package.json](/package.json) pour NPM, mais pour Maven.

Comme pour le frontend, [src](/backend/src/) contient le code. Mais il y a deux dossiers dedans:
- [main](/backend/src/main/): Contient le code Java.
- [test](/backend/src/test/): Contient les tests unitaires, lancés à chaque compilation (par défaut).

Le code Java se trouve (*loin*) dans [/backend/src/main/java/optim/optim/](/backend/src/main/java/optim/optim/), optim/optim étant le nom du packet qu'est le projet.

Ce dossier contient:
- [SimplexApplication.java](/backend/src/main/java/optim/optim/SimplexApplication.java): Le point d'entré du framework SpringBoot.
- [controller](/backend/src/main/java/optim/optim/controller/): Les différents contrôlleurs, qui sont les fichiers executés lors d'un appel HTTP sur leur URL.
- [response](/backend/src/main/java/optim/optim/response/): Une liste de fichiers spécifiques aux réponses attendues par le frontend (ils doivent être consistant avec les [models](/src/models/)).
- [service](/backend/src/main/java/optim/optim/service/): Une liste de fichiers qui font le lien entre les contrôlleurs et le code qui résout la demande.
- [src](/backend/src/main/java/optim/optim/src/): Liste de dossier spécifique pour la résolution des demandes, la "logique" en soit.

Le dossier [test](/backend/src/main/java/optim/optim/test/) contient d'anciennes versions de fichiers à effacer, ou des tests rapides à faire, sans avoir besoin de lancer le serveur en entier. Tout les éléments dedans seront ignorés par GitHub.

En résumé, une requête fait ce chemin en interne:
```
Requête HTTP ->
    controller X ->
        service Y ->
            Réponse donnée par le logique dans src ->
        service Y formatte la réponse ->
    contoller X renvoie la réponse formattée ->
Réponse HTTP
```

### Structure interne

#### Contrôlleur

Un contrôlleur remplis juste le rôle de récupérer les informations requises, et de les transmettre au service concerné. Il renvoie une erreur si une informations requises est manquantes.

#### Service

Un service vérifie la validité des paramètres fournies, et prépare la réponse, en appellant diverses fonctions, dans les fichiers de la logique.

#### Logique

Répond à un problème spécifique, dans sont propre dossier dans [src](/backend/src/main/java/optim/optim/src/).

<div align="right"><kbd><a href="#structure-du-projet">Retour en haut</a></kbd></div>

