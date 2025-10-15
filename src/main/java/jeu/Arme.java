package jeu;

import java.util.Random;

public class Arme {
    private static final Random random = new Random();
    
    private final String nom;
    private final int degatsDe; // Nombre de faces du dé de dégâts
    private final int nbDes;    // Nombre de dés à lancer
    private final int bonusDegats;
    
    public Arme(String nom, int degatsDe, int nbDes, int bonusDegats) {
        this.nom = nom;
        this.degatsDe = degatsDe;
        this.nbDes = nbDes;
        this.bonusDegats = bonusDegats;
    }
    
    public String getNom() {
        return nom;
    }
    
    public int calculerDegats() {
        int degats = 0;
        for (int i = 0; i < nbDes; i++) {
            degats += random.nextInt(degatsDe) + 1;
        }
        return degats + bonusDegats;
    }
    
    @Override
    public String toString() {
        return nom + " (" + nbDes + "d" + degatsDe + (bonusDegats != 0 ? (bonusDegats > 0 ? "+" : "") + bonusDegats : "") + ")";
    }
} 