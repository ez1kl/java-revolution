# Java Revolution - L'Ombre de la Guillotine

Un jeu narratif interactif inspiré du concept du **Livre dont vous êtes le héros**, développé en Java avec interface graphique Swing. Plongez dans la France révolutionnaire et vivez une aventure épique où vos choix déterminent votre destinée.

![Java](https://img.shields.io/badge/Java-ED8B00?style=flat&logo=openjdk&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-C71A36?style=flat&logo=apache-maven&logoColor=white)
![Swing](https://img.shields.io/badge/Swing-GUI-blue)

## Description Revolution - L'Ombre de la Guillotine

Un jeu narratif interactif inspiré du concept du **Livre dont vous êtes le héros**, développé en Java avec interface graphique Swing. Plongez dans la France révolutionnaire et vivez une aventure épique où vos choix déterminent votre destinée !

![Java](https://img.shields.io/badge/Java-ED8B00?style=flat&logo=openjdk&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-C71A36?style=flat&logo=apache-maven&logoColor=white)
![Swing](https://img.shields.io/badge/Swing-GUI-blue)

## � Description

**Java Revolution** est un RPG narratif où vous incarnez un personnage dans la France de 1789. Le jeu propose :

- Une narration dynamique chargée depuis des fichiers JSON
- Un système de combat au tour par tour
- Des choix qui influencent l'histoire
- Un système de sauvegarde/chargement de partie
- Une interface graphique soignée avec Swing
- Des effets sonores et musiques d'ambiance
- Création de personnage avec différentes classes et talents

## Fonctionnalités

- Scénario "L'Ombre de la Guillotine" entièrement jouable
- Système de personnages avec stats (vie, force, défense, etc.)
- Combat stratégique contre des monstres
- Inventaire d'armes et objets
- Tests de compétences (dés, probabilités)
- Interface d'authentification et gestion des utilisateurs
- Sauvegarde de progression
- Architecture modulaire et extensible

## Technologies utilisées

- **Langage** : Java 17+
- **Build Tool** : Maven
- **Interface** : Java Swing
- **Base de données** : H2 (embarquée)
- **Format de données** : JSON pour les scénarios
- **Architecture** : POO avec pattern MVC

## Prérequis

- **Java JDK 17** ou supérieur
- **Maven 3.6+**
- Un IDE Java (IntelliJ IDEA, Eclipse, VS Code)

### Installation de Java et Maven

**Sur macOS (avec Homebrew) :**
```bash
brew install openjdk@17
brew install maven
```

**Sur Ubuntu/Debian :**
```bash
sudo apt install openjdk-17-jdk maven
```

**Sur Windows :**
Téléchargez Java depuis [adoptium.net](https://adoptium.net/)

## Installation et lancement

### Méthode 1 : Avec Maven (recommandé)

```bash
# Cloner le projet
git clone https://github.com/ez1kl/java-revolution.git
cd java-revolution

# Compiler le projet
mvn clean compile

# Lancer le jeu
mvn exec:java -Dexec.mainClass="jeu.LoginScreen"
```

### Méthode 2 : Compilation manuelle

```bash
# Compiler
javac -d out src/main/java/jeu/*.java

# Lancer
java -cp out jeu.LoginScreen
```

### Méthode 3 : Créer un JAR exécutable

```bash
mvn clean package
java -jar target/java-revolution-1.0.jar
```

## Structure du projet

```
java-revolution/
├── src/main/java/jeu/
│   ├── LoginScreen.java       # Écran de connexion
│   ├── GameUI.java            # Interface principale du jeu
│   ├── ScenarioLoader.java    # Chargement des scénarios JSON
│   ├── Personnage.java        # Classe personnage
│   ├── Chapitre.java          # Gestion des chapitres
│   ├── Combat.java            # Système de combat
│   ├── Monstre.java           # Ennemis
│   ├── Arme.java              # Système d'armes
│   ├── Talent.java            # Compétences spéciales
│   └── VoiceManager.java      # Gestion audio
├── resources/
│   ├── scenarios/             # Fichiers JSON des histoires
│   ├── images/                # Assets graphiques
│   └── sounds/                # Effets sonores
├── sauvegardes/               # Sauvegardes des parties
├── pom.xml                    # Configuration Maven
└── README.md
```

## Comment jouer

1. **Créez un compte** ou connectez-vous
2. **Créez votre personnage** en choisissant :
   - Votre nom
   - Votre classe (Guerrier, Mage, Voleur, etc.)
   - Vos talents et compétences
3. **Lisez l'histoire** et faites vos choix
4. **Combattez** les ennemis avec stratégie
5. **Sauvegardez** votre progression à tout moment

## Scénarios disponibles

### L'Ombre de la Guillotine
Incarnez un citoyen dans la France révolutionnaire de 1789. Survivez aux tumultes de la Révolution, faites des choix moraux difficiles et découvrez les multiples fins possibles selon vos actions.

## Développement

### Ajouter un nouveau scénario

1. Créez un fichier JSON dans `resources/scenarios/`
2. Suivez la structure du fichier existant
3. Chargez-le via `ScenarioLoader`

### Structure JSON d'un scénario

```json
{
  "titre": "Titre du scénario",
  "chapitres": [
    {
      "id": 1,
      "texte": "Description du chapitre",
      "choix": [
        {
          "texte": "Option 1",
          "destination": 2
        }
      ]
    }
  ]
}
```

## Tests

```bash
# Lancer les tests
mvn test
```

## Auteur

**Yassine Badaoui** - [@ez1kl](https://github.com/ez1kl)

Projet réalisé dans le cadre du projet annuel - ESGI Aix-en-Provence

## Remerciements

- L'équipe pédagogique de l'ESGI
- Les inspirations : Les Livres dont vous êtes le héros
- La communauté Java

## Notes

Ce projet a été développé à des fins éducatives dans le cadre de ma formation à l'ESGI. Le code est optimisé pour la lisibilité et l'apprentissage de la POO en Java.