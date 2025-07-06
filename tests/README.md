# English

You first need to decompress the [tests.zip archive](/tests/tests.zip), and put all it's content in this folder.

This test folder contains a large amount of files, where:
- [assignmentTests](./assignmentTests/) has 6 file with a relatively large amount of data, without the results.
- [relaxUnitTest](./relaxUnitTests/) contains hundreds of files, following this pattern:
  - **name.dict**: The input data.
  - **name.dict.output**: The expected result for the relaxed problem.
  - **name.dict.tex**: A LaTeX file contained each resolution steps. They are all compressed in the [relaxUnitOutputSteps.zip](/tests/relaxUnitTests/relaxUnitOutputSteps.zip) file.
- [unitTest](./unitTests/) contains hundreds of files, following this pattern:
  - **name** (without extension): The input data.
  - **name.output**: The expected result for the integer problem.
  - **name.tex**: A LaTeX file contained each resolution steps. They are all compressed in the [unitOutputSteps.zip](/tests/unitTests/unitOutputSteps.zip) file.

# Français

Vous devez d'abord décompresser [l'archive tests.zip](/tests/tests.zip), et mettre tout son contenu directement dans ce dossier..

Ce dossier test contient une large quantité de fichiers, où:
- [assignmentTests](./assignmentTests/) a 6 fichiers avec une quantité relativement grande de données, sans les résultats.
- [relaxUnitTest](./relaxUnitTests/) contient quelques centaine de fichiers, qui suivent ce schéma:
  - **nom.dict**: Les données d'entré.
  - **nom.dict.output**: Le résultat attendu pour le problème relaxé.
  - **nom.dict.tex**: Un fichier LaTeX contenant les étapes de résolutions. Ils sont tous compréssés dans le fichier [relaxUnitOutputSteps.zip](/tests/relaxUnitTests//relaxUnitOutputSteps.zip).
- [unitTest](./unitTests/) contient quelques centaine de fichiers, qui suivent ce schéma:
  - **nom** (sans extension): Les données d'entré.
  - **nom.output**: Le résultat attendu pour le problème en nombre entier.
  - **nom.tex**: Un fichier LaTeX contenant les étapes de résolutions. Ils sont tous compréssés dans le fichier [unitOutputSteps.zip](/tests/unitTests/unitOutputSteps.zip).
