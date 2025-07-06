# Project structure

Files are splitted into two big categories: [frontend](#frontend) and [backend](#backend).

- [Project structure](#project-structure)
  - [Frontend](#frontend)
    - [Internal components structure](#internal-components-structure)
  - [Backend](#backend)
    - [Internal structure](#internal-structure)
      - [Controller](#controller)
      - [Service](#service)
      - [Logic](#logic)

---

## Frontend

> [!IMPORTANT]
> The [Angular](https://angular.dev) framework is used for the frontend. If you are not familiar with this framework, please take a look at their [tutorial](https://angular.dev/tutorials) on their official website.

In the project file tree, every file and folder belong to the frontend, except:
- The [tests](/tests/) folder.
- The [documentation](/documentation/) folder.
- The [backend](/backend/) folder.
- The three configurations files [.angular](/.angular/), [.github](/.github) and [.vscode](/.vscode).
- [LICENSE](/LICENSE) and [README.md](/README.md) files.

> [!NOTE]
> A [.git](/.git/) folder exist as well if the project has been downloaded. It is how GitHub detect changes in the code, and synchronise it with the online version.

All files in the [main folder](/) are configurations files, and should not be midified except if you know what you are doing.

<details>
<summary><b>Complementary documentation</b></summary>

> If you are curious, here are the associated documentations:
> - [**.editorconfig**](/.editorconfig): https://editorconfig.org/
> - [**.gitignore**](/.gitignore): https://git-scm.com/docs/gitignore
> - [**.postcssrc.json**](/.postcssrc.json): https://postcss.org/
> - [**angular.json**](/angular.json): https://angular.dev/reference/configs/workspace-config
> - [**eslint.config.js**](/eslint.config.js): https://eslint.org/docs/latest/use/configure/
> - [**package.json**](/package.json) and [**package-lock.json**](/package-lock.json): https://docs.npmjs.com/cli/v11/configuring-npm/package-json
> - [**tailwind.config.js**](/tailwind.config.js): https://v2.tailwindcss.com/docs/configuration
> - [**tsconfig.app.json**](/tsconfig.app.json), [**tsconfig.json**](/tsconfig.json) and [**tsconfig.spec.json**](/tsconfig.spec.json) : https://www.typescriptlang.org/docs/handbook/tsconfig-json.html
>
> And for what is incoming:
> - ".scss" files: https://sass-lang.com/guide/
</details>
<br>

The [public](/public/) folder contains, as the name implies, every resources accessible via an URL.
> e.g. the file [favicon.ico](/public/favicon.ico) is accessible with the URL "http://localhost:4200/favicon.ico", when the frontend server is running.

The whole frontend content is otherwise contained into the [src](/src/) folder:
- [index.html](/src/index.html): Entry file, the root, of the website.
- [main.ts](/src/main.ts): Angular script entry file.
- [style.scss](/src/styles.scss): Globally applied style file.
- [environments](/src/environments/): Project configuration folder. See [README.md: Initial configuration](/README.md#initial-configuration).
- [models](/src/models/): File list that describe types and interface used globally.
- [services](/src/services/): Folder where services used by the project are. See [Angular: Service](https://angular.dev/ecosystem/service-workers).
- [shared](/src/shared/): List of globally used Angular components, who don't have an specific associated page.
- [utils](/src/utils/): List of files containing useful functions that are shared and used throughout multiple components.

The [app](/src/app/) is the only one left, it contains the heart of the frontend:
- [main](/src/app/main/): Home page.
- The four files [app.component](/src/app/app.component.ts) are the "app" component, which is the first component loaded by Angular.
- [app.config.ts](/src/app/app.config.ts): Containes [providers](https://angular.dev/guide/di/dependency-injection-providers), enabling some features, like a HTTP request wrapper.
- [app.route.ts](/src/app/app.routes.ts): File containing the website routes, specifyning the title to display, the component to use, the URL and more. See [Angular: Routing](https://angular.dev/guide/routing).

### Internal components structure

A component is made of four files, and can be generated with the following steps:
```shell
cd /src/path/to/parent
ng generate component name
ng g c name # this line is the same as the one above
```

The first line is to create the component where you currently are in the project file tree.  
The second line will create a component nammed "*name*", in a folder of the same name, with the four files from earlier.

The four files are:
- *nom*.component.html: HTML file of the component, that's the visible part for the user..
- *nom*.component.scss: The HTML style file, for this component only. See [Angular: Encapsulation](https://angular.dev/guide/components/styling).
- *nom*.component.spec.ts: The test file for the component.
- *nom*.component.ts: The component script file, where the logic is.

Like with the [main](/src/app/main/) component, some contains another component(s) inside their folder. Generally, a component is directly in [app](/src/app/) if it is used in a [route](/src/app/app.routes.ts). However, it may requires a sub component, to lighten the code or complex parts that appear often, but this sub component is used only by it's parent. This is the case for the [simplex-response](/src/app/main/simplex-response/) component.

<div align="right"><kbd><a href="#project-structure">Back to top</a></kbd></div>

---

## Backend


> [!IMPORTANT]
> The [SpringBoot](https://spring.io/projects/spring-boot) framework is used for the backend. If you are not familiar with this framework, please take a look at their [tutorial](https://docs.spring.io/spring-boot/tutorial/first-application/index.html) on their official website.

In the project file tree, only the [backend](/backend/) folder contains the backend code.

The files [build.sh](/backend/build.sh), [mvnw.cmd](/backend/mvnw.cmd) and [mvnw.sh](/backend/mvnw.sh) can be used to run the backend (see [README.md: Developing](/README.md#developing)).

The [pom.xml](/backend/pom.xml) file contains the list of packets used by Maven to compile the project ([POM documentation](https://maven.apache.org/guides/introduction/introduction-to-the-pom.html)). It has the same role as [package.json](/package.json) with NPM, but for Maven.

Like the frontend, [src](/backend/src/) contains the code. But there are two folders inside:
- [main](/backend/src/main/): Contains the Java code.
- [test](/backend/src/test/): Contains the unit tests, that are launched at each compilation (by default).

The java code is (*far*) in [/backend/src/main/java/optim/optim/](/backend/src/main/java/optim/optim/), optim/optim being the package name of the project.

This folder contains:
- [SimplexApplication.java](/backend/src/main/java/optim/optim/SimplexApplication.java): The entry point for the SpringBoot fromework.
- [controller](/backend/src/main/java/optim/optim/controller/): Multiple controllers, who are the executed files when a HTTP request is made with their URL.
- [response](/backend/src/main/java/optim/optim/response/): A list of files specific to the responses expected by the frontend (they should match on of the [models](/src/models/)).
- [service](/backend/src/main/java/optim/optim/service/): A list of files that make the link between controllers and the code that resolve the request.
- [src](/backend/src/main/java/optim/optim/src/): A list of folders specific for a request, the "logic".

The [test](/backend/src/main/java/optim/optim/test/) folder contains old scripts versions to delete, or quick tests to run, without having to start the whole server. Every elements will be ignored by GitHub.


In summary, a request makes the internal path:
```
HTTP request ->
    controller X ->
        service Y ->
            Response given by the logic in src ->
        service Y formats the response ->
    contoller X returns the formated response ->
HTTP response
```

### Internal structure

#### Controller

A controller has the role to get the parameters needed, and to sned them to the service concerned. It will send an error if a required parameters is missing.

#### Service

A service will verify the validity of the given parameters, and prepare the response, by calling the logic necessary.

#### Logic

Answer to a specific problem, in his own folder in [src](/backend/src/main/java/optim/optim/src/).

<div align="right"><kbd><a href="#project-structure">Back to top</a></kbd></div>

