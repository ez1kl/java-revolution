package jeu;

import java.io.Serializable;
import java.util.Random;

public class TestCompetence {
    private static final Random random = new Random();
    
    // Difficulté des tests
    public enum Difficulte {
        FACILE(8),
        MOYENNE(12),
        DIFFICILE(15);
        
        private final int seuilReussite;
        
        Difficulte(int seuilReussite) {
            this.seuilReussite = seuilReussite;
        }
        
        public int getSeuilReussite() {
            return seuilReussite;
        }
    }
    
    // Types de compétences
    public enum TypeCompetence implements Serializable {
        DIPLOMATIE("Diplomatie"),
        DISCRETION("Discrétion"),
        COMBAT("Combat"),
        CHARISME("Charisme"),
        PERCEPTION("Perception"),
        ADRESSE("Adresse");
        
        private final String nom;
        
        TypeCompetence(String nom) {
            this.nom = nom;
        }
        
        public String getNom() {
            return nom;
        }
        
        public static TypeCompetence fromString(String text) {
            for (TypeCompetence type : TypeCompetence.values()) {
                if (type.nom.equalsIgnoreCase(text)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Compétence inconnue: " + text);
        }
    }
    
    // Résultat d'un test
    public static class ResultatTest {
        private final TypeCompetence type;
        private final Difficulte difficulte;
        private final int valeurBase;
        private final int modificateur;
        private final int resultatDes;
        private final boolean reussite;
        private final String message;
        private final int degatsSubis;
        
        public ResultatTest(TypeCompetence type, Difficulte difficulte, int valeurBase, 
                          int modificateur, int resultatDes, boolean reussite) {
            this(type, difficulte, valeurBase, modificateur, resultatDes, reussite, null, 0);
        }
        
        public ResultatTest(TypeCompetence type, Difficulte difficulte, int valeurBase, 
                          int modificateur, int resultatDes, boolean reussite, 
                          String message, int degatsSubis) {
            this.type = type;
            this.difficulte = difficulte;
            this.valeurBase = valeurBase;
            this.modificateur = modificateur;
            this.resultatDes = resultatDes;
            this.reussite = reussite;
            this.message = message;
            this.degatsSubis = degatsSubis;
        }
        
        public boolean isReussite() {
            return reussite;
        }
        
        public String getMessage() {
            if (message != null) {
                return message;
            }
            
            StringBuilder sb = new StringBuilder();
            sb.append("Test de ").append(type.getNom()).append(" (difficulté ").append(difficulte).append(")\n");
            sb.append("Valeur de base: ").append(valeurBase);
            if (modificateur != 0) {
                sb.append(" (modificateur: ").append(modificateur).append(")");
            }
            sb.append("\nRésultat des dés: ").append(resultatDes);
            sb.append("\nRésultat final: ").append(resultatDes <= (valeurBase + modificateur) ? "Réussite!" : "Échec...");
            
            return sb.toString();
        }
        
        public int getDegatsSubis() {
            return degatsSubis;
        }
    }
    
    // Lancer un dé à X faces
    public static int lancerDe(int faces) {
        return random.nextInt(faces) + 1;
    }
    
    // Faire un test de compétence
    public static ResultatTest faireTest(TypeCompetence competence, Difficulte difficulte, int bonus) {
        int resultatDe = lancerDe(20);
        int resultatTotal = resultatDe + bonus;
        boolean reussite = resultatTotal >= difficulte.getSeuilReussite();
        
        return new ResultatTest(competence, difficulte, resultatDe, bonus, resultatTotal, reussite);
    }
    
    // Faire un test de combat
    public static ResultatTest faireCombat(int valeurCombat, int habileteAdversaire, int enduranceAdversaire) {
        // Le joueur lance un dé et ajoute sa valeur de combat
        int resultatJoueur = lancerDe(20) + valeurCombat;
        
        // L'adversaire lance un dé et ajoute son habileté
        int resultatAdversaire = lancerDe(20) + habileteAdversaire;
        
        // Déterminer le vainqueur
        boolean reussite = resultatJoueur >= resultatAdversaire;
        
        // Créer un résultat de test spécial pour le combat
        return new ResultatCombat(
            TypeCompetence.COMBAT, 
            Difficulte.MOYENNE, 
            resultatJoueur - valeurCombat, // dé du joueur
            valeurCombat, // bonus du joueur
            resultatJoueur, // total du joueur
            reussite,
            resultatAdversaire - habileteAdversaire, // dé de l'adversaire
            habileteAdversaire, // habileté de l'adversaire
            enduranceAdversaire // endurance de l'adversaire
        );
    }
    
    // Classe spécifique pour les résultats de combat
    public static class ResultatCombat extends ResultatTest {
        private static final long serialVersionUID = 2L;
        
        private final int deAdversaire;
        private final int habileteAdversaire;
        private final int enduranceAdversaire;
        
        public ResultatCombat(TypeCompetence competence, Difficulte difficulte, int resultatDe, int bonus, int resultatTotal, 
                             boolean reussite, int deAdversaire, int habileteAdversaire, int enduranceAdversaire) {
            super(competence, difficulte, resultatDe, bonus, resultatTotal, reussite);
            this.deAdversaire = deAdversaire;
            this.habileteAdversaire = habileteAdversaire;
            this.enduranceAdversaire = enduranceAdversaire;
        }
        
        @Override
        public String getMessage() {
            StringBuilder message = new StringBuilder();
            message.append("Combat :\n");
            message.append("Votre jet : ").append(getResultatDes()).append(" + ").append(getBonus()).append(" (Combat) = ").append(getResultatTotal()).append("\n");
            message.append("Jet de l'adversaire : ").append(deAdversaire).append(" + ").append(habileteAdversaire).append(" (Habileté) = ").append(deAdversaire + habileteAdversaire).append("\n\n");
            
            if (isReussite()) {
                message.append("Victoire ! Vous avez vaincu votre adversaire.");
                if (getResultatDes() == 20) {
                    message.append(" Coup critique !");
                }
            } else {
                message.append("Défaite... Vous avez été vaincu par votre adversaire.");
                if (getResultatDes() == 1) {
                    message.append(" Échec critique !");
                }
            }
            
            return message.toString();
        }
        
        // Getters pour accéder aux valeurs privées de la classe parente
        private int getResultatDes() {
            return super.resultatDes;
        }
        
        private int getBonus() {
            return super.modificateur;
        }
        
        private int getResultatTotal() {
            return super.resultatDes + super.modificateur;
        }
    }
} 