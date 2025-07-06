# Linear problem solver application

![CYTech_Logo](https://upload.wikimedia.org/wikipedia/commons/4/4a/CY_Tech.png)

![GitHub open issue](https://img.shields.io/github/issues-search?query=repo%3AShadowoftheRedFox%2FSimplex%20is%3Aopen%20is%3Aissue&label=Open%20issues&color=blue)
![GitHub License](https://img.shields.io/github/license/ShadowoftheRedFox/SimplexLinearOptimisation?label=License)

This is an academic tools created for [CY Tech](https://cytech.cyu.fr/). It is aimed to solve linear problems, mainly using the simplex method, showing various informations useful to illustrate the method or help students.

See the [documentation folder](documentation/) for more in depth explanations.

It is focused on being "student friendly", meaning the interface and the code documentation may be more deeply explained than necessary.

<u>🇫🇷 Voir [README-fr.md](/documentation/fr/README-fr.md) pour la version française.</u>

---

## Table of contents
- [Linear problem solver application](#linear-problem-solver-application)
  - [Table of contents](#table-of-contents)
  - [Prerequisites](#prerequisites)
  - [Getting started](#getting-started)
    - [Frontend](#frontend)
    - [Backend](#backend)
    - [Initial Configuration](#initial-configuration)
  - [Developing](#developing)
    - [Building](#building)
  - [Features](#features)
  - [Contributing](#contributing)
  - [Links](#links)
  - [Licensing](#licensing)

---

## Prerequisites

- Install [Node.js](https://nodejs.org/en/download/) which includes [Node Package Manager](http://npmjs.com).
- Install Java compatible with version 21.
- Install Maven compatible with Java version 21.

<div align="right"><kbd><a href="#table-of-contents">Back to top</a></kbd></div>

---

## Getting started

### Frontend

To install the frontend, you only need to type the commands below:

```shell
npm install -g @angular/cli
npm install
ng serve
```

The line `npm install -g @angular/cli` will install the Angular CLI (Command Line Interface), that is able to launch, create and manage Angular projects from the terminal.
> [!TIP]
> The `-g` is to install if globally on your computer.

The line `npm install` will install all package specified in the [package.json file](/package.json).

If the project breaks after this command, used instead `npm ci`, which will force the NPM (Node Package Manager) to use the version provided in the [package-lock.json](/package-lock.json).

Finally, `ng serve` use the angular-cli to launch a local server on http://localhost:4200 (by default).

### Backend

```shell
cd ./backend/
# Manually
mvn install spring-boot:run
# Linux
./mvnw.sh install spring-boot:run
# Windows
./mvnw.cmd install spring-boot:run
```

The backend, the server running the different linear optimisation solver, is in the [backend folder](/backend/), which is why we use `cd ./backend/`.  
To launch the server, you can juste use the `mvn install spring-boot:run` command line. It will build and test the code, before launching the server.

The two other command lines are [Maven Wrapper](https://maven.apache.org/wrapper/), which is a utility that allows you to run Maven projects without needing a pre-installed Maven version.

<div align="right"><kbd><a href="#table-of-contents">Back to top</a></kbd></div>

---

### Initial Configuration

This project needs some informations, provided in multiples files:

- For the **frontend**, the [environments.ts](/src/environments/environment.ts) and [environment.development.ts](/src/environments/environment.development.ts) files are both configuration files. The first file is used when building the project (see [Building](#building)), the second is used when running the `ng serve` command above.  
Each fields needs to be present on **both files**, otherwise Angular will throw an error. 
- For the **backend**, [application.config](/backend/src/main/resources/application.config) is used as the configuration file.

Each fields has their own description, feel free to look at them.

<div align="right"><kbd><a href="#table-of-contents">Back to top</a></kbd></div>

---

## Developing

To start developing on the project, follow those steps:

```shell
git clone https://github.com/ShadowoftheRedFox/Simplex.git
cd simplex/
```

> [!NOTE]
> If the  `git` command is unknown, install [git](https://git-scm.com/downloads) or download the code directly from GitHub.

The first line will pull the GitHub repository online and create a folder name "simplex", where all the files will be. Then the second line juste move your terminal into the newly created folder.

Then follow the [Getting started](#getting-started) section to setup the project.

The project should be running! Open in your internet browser http://localhost:4200, the project page, and try it out.

For further informations about the code itself, see [project-structure](/documentation/en/project-structure.md).


<div align="right"><kbd><a href="#table-of-contents">Back to top</a></kbd></div>

---

### Building

> [!WARNING]
> Unfinished section.  
> The project isn't buildable yet.
>
> In the future, the project should be able to run locally, by launching an executable.

<div align="right"><kbd><a href="#table-of-contents">Back to top</a></kbd></div>

---

## Features

> [!NOTE]
> Work in progress.

The main functionnality is, of course, to be able to solve an optimization problem using the simplex method.

In addition to this, the project can:
- Display graphically the feasible domain in 2D (and maybe in 3D).
- Display the tree of a branch and bound resolution.
- Display the steps to solve the integer simplex.
- Display the resolution using the interior-point method.

<div align="right"><kbd><a href="#table-of-contents">Back to top</a></kbd></div>

---

## Contributing

Feel free to contribute, please fork the repository and use a new branch. Pull requests are warmly welcome. Labels are available to distinguish your work.

The only requirements are:
- Use spaces and not tabs.
- Comment your codes, so that anyone can understand quickly what you wrote.
- The project should be able to compile and run.
- The linters should not return any errors.
- If there are breaking change, they must be **explicitly** stated, to warn other users.
- The [english documentation](/documentation/en/) should be updated accordingly. If your fluent in french, feel free to do the same for the [french documentation](/documentation/fr/).

> [!IMPORTANT]
> By submitting a PR, your agree to allow the project owner(s) to license your work under the terms of the [MIT license](/LICENSE).

<div align="right"><kbd><a href="#table-of-contents">Back to top</a></kbd></div>

---

## Links

- Repository: https://github.com/ShadowoftheRedFox/Simplex
- Issue tracker: https://github.com/your/awesome-project/issues
  - In case of sensitive bugs like security vulnerabilities, please contact leon.merin@gmail.com directly instead of using issue tracker. We value your effort to improve the security and privacy of this project!
- Useful informations:
  - Simplex: https://en.wikipedia.org/wiki/Simplex_algorithm
  - Branch and bound: https://en.wikipedia.org/wiki/Branch_and_bound
  - Interior-point method: https://en.wikipedia.org/wiki/Interior-point_method

<div align="right"><kbd><a href="#table-of-contents">Back to top</a></kbd></div>

---

## Licensing

The code in this project is licensed under [MIT license](/LICENSE).

<div align="right"><kbd><a href="#table-of-contents">Back to top</a></kbd></div>
