package jeu;

import java.util.Random;

public class Monstre {
    private static final Random random = new Random();
    
    private final String nom;
    private int pointsDeVie;
    private final int pointsDeVieMax;
    private final int bonusAttaque;
    private final int defense;
    private final int degatsDe; // Nombre de faces du dé de dégâts
    private final int nbDes;    // Nombre de dés à lancer
    private final int bonusDegats;
    
    public Monstre(String nom, int pointsDeVie, int bonusAttaque, int defense, int degatsDe, int nbDes, int bonusDegats) {
        this.nom = nom;
        this.pointsDeVie = pointsDeVie;
        this.pointsDeVieMax = pointsDeVie;
        this.bonusAttaque = bonusAttaque;
        this.defense = defense;
        this.degatsDe = degatsDe;
        this.nbDes = nbDes;
        this.bonusDegats = bonusDegats;
    }
    
    public String getNom() {
        return nom;
    }
    
    public int getPointsDeVie() {
        return pointsDeVie;
    }
    
    public void setPointsDeVie(int pointsDeVie) {
        this.pointsDeVie = Math.max(0, Math.min(pointsDeVie, pointsDeVieMax));
    }
    
    public void modifierPointsDeVie(int modification) {
        setPointsDeVie(pointsDeVie + modification);
    }
    
    public int getPointsDeVieMax() {
        return pointsDeVieMax;
    }
    
    public int getBonusAttaque() {
        return bonusAttaque;
    }
    
    public int getDefense() {
        return defense;
    }
    
    public int calculerDegats() {
        int degats = 0;
        for (int i = 0; i < nbDes; i++) {
            degats += random.nextInt(degatsDe) + 1;
        }
        return degats + bonusDegats;
    }
    
    public String getEtatSante() {
        double pourcentage = (double) pointsDeVie / pointsDeVieMax * 100;
        
        if (pourcentage >= 75) {
            return "En bonne santé";
        } else if (pourcentage >= 50) {
            return "Blessé";
        } else if (pourcentage >= 25) {
            return "Gravement blessé";
        } else if (pourcentage > 0) {
            return "Presque mort";
        } else {
            return "Mort";
        }
    }
} 