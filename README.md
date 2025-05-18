# Lab 0-1-2
Ce laboratoire fait partie de l’étape 1 de l’évaluation , en combinaison avec les laboratoires 1 et 2. Il est donc évalué, et les livrables produits feront partie du livrable final attendu à la fin de l’étape 1.
# Application Java Hello World
## Description de l'application
Mon application Java est un simple projet qui à pour but de montrer le message "Hello, World!" dans la console. Elle constitue la base de mon projet pour les futurs laboratoire 1 et 2. Docker sera utiliser et la réalisation de tests unitaires automatisés et l’intégration continue (CI/CD) sera fait dans l'application. L'objectif est de démontrer la mise en place d'un environnement de développement automatisé, reproductible et bien structuré.
## Objectifs du projet
- Créer mon application Java simple avec des tests unitaires
- Conteneuriser l'application avec Docker
- Automatiser les étapes de build, test et vérification avec une pipeline CI/CD
- Orchestration avec Docker Compose pour simplifier le démarrage des containers
## Instructions d'exécution et de build
### 1. Prérequis 
- Avoir JDK21 (Java Development Kit) d'installé sur la machine.
- Avoir Docker d'installé sur la machine pour la conteneurisation.
- Avoir Maven ou Gradle pour la gestion de build (si souhaité)
### 2. Cloner le dépot
- Cloner le projet sur la machine virtuelle:
```git clone https://github.com/Username/projetGit```
- Accéder le projet sur la machine virtuelle:
```cd "projetGit"```
### 3. Instructions pour Docker et Docker Compose
- Conteneurisation avec Docker de l’application
  - Construire l'image à partir du Dockerfile dans java-hello-world/:
```docker build -t java-hello-app ./java-hello-world```
  -  Lancer le conteneur en mode console:
```docker run --rm java-hello-app```
  - Lancer le conteneur avec accès web sur le port 8080:
```docker run --rm -p 8080:8080 java-hello-app```
### 4. Instructions pour Docker Compose
- Orchestration avec Docker Compose
  - Installer Docker Compose (si ce n’est pas déjà fait)
```sudo apt install docker-compose```
  - Construire l'image via Docker Compose
```docker-compose build```
  - Lancer l'application
```docker-compose up```
  - Lancer l'application en arrière-plan (mode détaché)
```docker-compose up -d```
  - Afficher les logs du conteneur
```docker-compose logs -f```
## Structuration du projet
En MVC
```
TPO-LOG430/
├── .github/
│   └── workflows/
│       └── ci.yml
├── .vscode/
│   ├── launch.json
│   ├── settings.json
│   └── task.json
├── java-hello-world/
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/
│       ├── main/
│       │   └── java/
│       │       └── com/
│       │           └── log430/
│       │               └── tp0/
│       │                   ├── controller/
│       │                   │   └── HelloController.java
│       │                   ├── model/
│       │                   │   └── HelloMovel.java
│       │                   ├── server/
│       │                   │   └── HelloServer.java
│       │                   ├── view/
│       │                   │   └── HelloView.java
│       │                   └── HelloApp.java
│       └── test/
│           └── java/
│               └── com/
│                   └── log430/
│                       └── tp0/
│                           ├── controller/
│                           │   └── HelloControllerTest.java
│                           ├── server/
│                           │   └── HelloServerTest.java
│                           ├── view/
│                           │   └── HelloViewTest.java
│                           └── HelloWorldTest.java
├── docker-compose.yml
├── .gitignore
└── README.md
```

