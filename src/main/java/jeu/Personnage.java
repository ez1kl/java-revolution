package jeu;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Personnage implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String nom;
    private final Map<TestCompetence.TypeCompetence, Integer> competences;
    private int pointsDeVie;
    private static final int POINTS_DE_VIE_MAX = 20;
    private transient Random random;  // Marqué comme transient car Random n'est pas sérialisable

    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.random = new Random();  // Réinitialisation de random après désérialisation
    }

    public Personnage(String nom) {
        this.nom = nom;
        this.competences = new HashMap<>();
        this.pointsDeVie = POINTS_DE_VIE_MAX;
        this.random = new Random();  // Initialisation dans le constructeur
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        // This method is not used in the current implementation
    }

    public int getCompetence(TestCompetence.TypeCompetence type) {
        return competences.getOrDefault(type, 0);
    }

    public void setCompetence(TestCompetence.TypeCompetence type, int valeur) {
        competences.put(type, valeur);
    }

    public TestCompetence.ResultatTest faireTest(TestCompetence.TypeCompetence type, TestCompetence.Difficulte difficulte) {
        int valeurBase = getCompetence(type);
        int modificateur = appliquerModificateurSante(type);
        
        // Lancer 2d6
        int resultatDes = random.nextInt(6) + 1 + random.nextInt(6) + 1;
        boolean reussite = resultatDes <= (valeurBase + modificateur);
        
        return new TestCompetence.ResultatTest(
            type, difficulte, valeurBase, modificateur, resultatDes, reussite
        );
    }

    public TestCompetence.ResultatTest faireCombat(int valeurCombatAdversaire, int pointsDeVieAdversaire) {
        int valeurBase = getCompetence(TestCompetence.TypeCompetence.COMBAT);
        int modificateur = appliquerModificateurSante(TestCompetence.TypeCompetence.COMBAT);
        
        // Lancer 2d6 pour le joueur et l'adversaire
        int resultatJoueur = random.nextInt(6) + 1 + random.nextInt(6) + 1;
        boolean reussite = resultatJoueur <= (valeurBase + modificateur);
        int degatsSubis = 0;
        
        // Simuler le combat
        if (!reussite) {
            degatsSubis = 2;
            modifierPointsDeVie(-degatsSubis);
        }
        
        String message = String.format("Combat contre adversaire (Valeur: %d, PV: %d)", 
            valeurCombatAdversaire, pointsDeVieAdversaire);
        
        return new TestCompetence.ResultatTest(
            TestCompetence.TypeCompetence.COMBAT,
            TestCompetence.Difficulte.MOYENNE,
            valeurBase,
            modificateur,
            resultatJoueur,
            reussite,
            message,
            degatsSubis
        );
    }

    private int appliquerModificateurSante(TestCompetence.TypeCompetence type) {
        if (type != TestCompetence.TypeCompetence.COMBAT) {
            return 0;
        }
        
        double pourcentageSante = (double) pointsDeVie / POINTS_DE_VIE_MAX;
        if (pourcentageSante > 0.75) return 0;
        if (pourcentageSante > 0.5) return -1;
        if (pourcentageSante > 0.25) return -2;
        return -3;
    }

    public int getPointsDeVie() {
        return pointsDeVie;
    }

    public int getPointsDeVieMax() {
        return POINTS_DE_VIE_MAX;
    }

    public void restaurerPointsDeVie() {
        this.pointsDeVie = POINTS_DE_VIE_MAX;
    }

    public void modifierPointsDeVie(int modification) {
        this.pointsDeVie = Math.max(0, Math.min(POINTS_DE_VIE_MAX, this.pointsDeVie + modification));
    }

    public String getEtatSante() {
        double pourcentage = (double) pointsDeVie / POINTS_DE_VIE_MAX;
        if (pourcentage > 0.75) return "En pleine forme";
        if (pourcentage > 0.5) return "Légèrement blessé";
        if (pourcentage > 0.25) return "Blessé";
        if (pourcentage > 0) return "Gravement blessé";
        return "Critique";
    }

    public boolean estVivant() {
        return pointsDeVie > 0;
    }

    public void soigner(int points) {
        modifierPointsDeVie(points);
    }
} 