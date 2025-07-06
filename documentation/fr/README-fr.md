# Application de résolution de problèmes linéaires

![CYTech_Logo](https://upload.wikimedia.org/wikipedia/commons/4/4a/CY_Tech.png)

![GitHub problèmes ouverts](https://img.shields.io/github/issues-search?query=repo%3AShadowoftheRedFox%2FSimplex%20is%3Aopen%20is%3Aissue&label=Pobl%C3%A8mes%20ouverts&color=blue)
![License Github](https://img.shields.io/github/license/ShadowoftheRedFox/SimplexLinearOptimisation?label=License)


Il s'agit d'un outil pédagogique créé pour [CY Tech](https://cytech.cyu.fr/). Il vise à résoudre des problèmes linéaires, principalement par la méthode du simplexe, et présente diverses informations utiles pour illustrer la méthode ou aider les étudiants.

Voir le [dossier de documentation](/documentation/) pour des explications plus approfondies.

<u>🇬🇧 See the [README.md](/README.md) for the english version.</u>

---

## Table des matières
- [Application de résolution de problèmes linéaires](#application-de-résolution-de-problèmes-linéaires)
  - [Table des matières](#table-des-matières)
  - [Prérequis](#prérequis)
  - [Mise en place](#mise-en-place)
    - [Frontend](#frontend)
    - [Backend](#backend)
    - [Configuration initiale](#configuration-initiale)
  - [Développement](#développement)
    - [Construction](#construction)
  - [Caractéristiques](#caractéristiques)
  - [Contributions](#contributions)
  - [Liens](#liens)
  - [License](#license)

---

## Prérequis

- Installez [Node.js](https://nodejs.org/en/download/) qui comprend [Node Package Manager](http://npmjs.com).
- Installez Java compatible avec la version 21.
- Installez Maven compatible avec la version 21 de Java.

<div align="right"><kbd><a href="#table-des-matières">Retour en haut</a></kbd></div>

---

## Mise en place

### Frontend

Pour installer le frontend, il suffit juste de taper les commandes ci-dessous:

```shell
npm install -g @angular/cli
npm install
ng start
```

La ligne `npm install -g @angular/cli` va installer la ILC (Interface de Ligne de Commande, ou CLI en anglais) d'Angular, pour pouvoir lancer, créer et gérer des projets Angular à partir du terminal de commande.
> [!TIP]
> Le `-g` est pour l'installer globalement sur votre machine.

La ligne `npm install` installera tout les paquets spécifiés dans le fichier [package.json](/package.json).

Si le projet casse avec cette commande, utilisez `npm ci` à la place, qui forcera NPM (Node Package Manager) à utiliser les versions précises données avec [package-lock.json](/package-lock.json).

Finalement, `ng serve` utilisera la ILC d'Angular pour lancer un serveur local sur http://localhost:4200 (par défaut).

### Backend

```shell
cd ./backend/
# Manuellement
mvn install spring-boot:run
# Linux
./mvnw.sh install spring-boot:run
# Windows
./mvnw.cmd install spring-boot:run
```

Le backend, le serveur qui lance les différents solveur d'optimisation, est dans le dossier [backend](/backend/), ce qui est pourquoi `cd ./backend/` est utilisée. Pour lancer le serveur, il suffit de faire `mvn install spring-boot:run`. Cela compilera et testera le code, avant de lancer le serveur.

Les deux autres lignes de commandes sont des [Maven Wrapper](https://maven.apache.org/wrapper/), qui est un utilitaire qui vous permet d'exécuter des projets Maven sans avoir besoin d'une version Maven préinstallée.

<div align="right"><kbd><a href="#table-des-matières">Retour en haut</a></kbd></div>

---

### Configuration initiale

Ce projet à besoin de quelques informations, qui sont données dans différents fichiers:

- Pour le **frontend**, les fichiers [environments.ts](/src/environments/environment.ts) et [environment.development.ts](/src/environments/environment.development.ts) sont les deux fichiers de configurations. Le premier est utilisé lors de la construction du projet (voir [Construction](#construction)), le seconde est utilisé quand la commande `ng serve` ci-dessus est utilisée.  
Chaque champ doit être présent sur **les deux fichiers**, sinon Angular lancera une erreur.
- Pour le **backend**, [application.config](/backend/src/main/resources/application.config) est le fichier de configuration.

Chaque champ a sa propre description, n'hésitez pas à les regarder.

<div align="right"><kbd><a href="#table-des-matières">Retour en haut</a></kbd></div>

---

## Développement

Pour commencer à développé sur le projet, suivez ces étapes:

```shell
git clone https://github.com/ShadowoftheRedFox/Simplex.git
cd simplex/
```

> [!NOTE]
> Si la commandes `git` est inconnue, installez [git](https://git-scm.com/downloads) ou télécharger le code directement sur GitHub.

La première ligne va aller chercher le dépôt GitHub en ligne, et créer un dossier nommé "simplex", où tout les fichiers seront. Puis, la deuxième ligne change simplment le terminal dans le dossier nouvellement créé.

Suivez ensuite la section [Mise en place](#mise-en-place).

Le projet devrait être lancé! Ouvrez sur un explorateur internet http://localhost:4200, la page du projet, et essayez.

Pour plus d'informations sur le code, regardez [structure du projet](/documentation/fr/project-structure.md).

<div align="right"><kbd><a href="#table-des-matières">Retour en haut</a></kbd></div>

---

### Construction

> [!WARNING]
> Section non finie.  
> Le projet n'est pas constructible pour le moment.
>
> Dans le future, le projet devrait pouvoir tourner localement, en lançant un exécutable.

<div align="right"><kbd><a href="#table-des-matières">Retour en haut</a></kbd></div>

---

## Caractéristiques

> [!NOTE]
> Travail en cours.

La principal fonctionnalité du projet est, bien sûr, de pouvoir résoudre un problème d'optimization linéaire en utilisant la méthode du simplex.

En plus de ceci, le projet peut:
- Afficher graphiquement le domaine faisable en 2D (peut être en 3D).
- Affichier les étapes de la méthode séparation et évaluation.
- Affichier les étapes pour résoudre le simplex en nombre entier.
- Affichier la résolution avec la méthode des points intérieurs.

<div align="right"><kbd><a href="#table-des-matières">Retour en haut</a></kbd></div>

---

## Contributions

N'hésitez pas à contribuer ! Veuillez dupliquer le dépôt et utiliser une nouvelle branche. Les PR sont les bienvenues. Des labels sont disponibles pour distinguer votre travail.

Les seules exigences sont :
- Utiliser des espaces et non des tabulations.
- Commenter le code afin que chacun puisse comprendre rapidement ce que vous avez écrit.
- Le projet doit pouvoir être compilé et exécuté.
- Les linters ne doivent pas renvoyer d'erreur.
- S'il y a des changements qui casse le projet, ils doivent être **explicitement** indiqués, pour avertir les autres utilisateurs.
- La [documentation anglaise](/documentation/en/) doit être mis à jour en conséquence. Et vu que vous lisez cela, n'hésitez pas à faire de même pour la [documentation française](/documentation/fr/), cela reste fortement conseillé.

> [!IMPORTANT]
> En soumettant une PR, vous acceptez de permettre au(x) propriétaire(s) du projet de concéder une licence pour votre travail selon les termes de la [license MIT](/LICENSE).

<div align="right"><kbd><a href="#table-des-matières">Retour en haut</a></kbd></div>

---

## Liens

- Dépôt: https://github.com/ShadowoftheRedFox/Simplex
- Suivi des problèmes: https://github.com/your/awesome-project/issues
  - En cas de bugs sensibles tels que des vulnérabilités de sécurité, veuillez contacter leon.merin@gmail.com directement plutôt que d'utiliser le suivi des problèmes. Nous apprécions vos efforts pour améliorer la sécurité et la confidentialité de ce projet.
- Informations utiles:
  - Simplex: https://fr.wikipedia.org/wiki/Algorithme_du_simplexe
  - Séparation et évaluation: https://fr.wikipedia.org/wiki/S%C3%A9paration_et_%C3%A9valuation
  - Méthode des points intérieurs: https://fr.wikipedia.org/wiki/M%C3%A9thodes_de_points_int%C3%A9rieurs

<div align="right"><kbd><a href="#table-des-matières">Retour en haut</a></kbd></div>

---

## License

Le code de ce projet est sous la [license MIT](/LICENSE).

<div align="right"><kbd><a href="#table-des-matières">Retour en haut</a></kbd></div>
