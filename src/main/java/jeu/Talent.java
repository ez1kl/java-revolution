package jeu;

import java.io.Serializable;

public class Talent implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public enum TypeTalent {
        FORCE,
        AGILITE,
        CHANCE,
        DIPLOMATIE,
        ADRESSE,  // armes à feu
        HABILETE, // armes blanches
        EQUITATION
    }

    public enum Difficulte {
        TRES_FACILE(8),
        FACILE(10),
        MOYEN(12),
        DIFFICILE(14),
        TRES_DIFFICILE(16);

        private final int seuilReussite;

        Difficulte(int seuilReussite) {
            this.seuilReussite = seuilReussite;
        }

        public int getSeuilReussite() {
            return seuilReussite;
        }
    }

    private final TypeTalent type;
    private int valeur;
    private static final int MIN_POINTS = 2;
    private static final int MAX_POINTS = 12;

    public Talent(TypeTalent type, int valeur) {
        this.type = type;
        setValeur(valeur);
    }

    public void setValeur(int valeur) {
        if (valeur < MIN_POINTS || valeur > MAX_POINTS) {
            throw new IllegalArgumentException("La valeur d'un talent doit être comprise entre " + MIN_POINTS + " et " + MAX_POINTS);
        }
        this.valeur = valeur;
    }

    public int getValeur() {
        return valeur;
    }

    public TypeTalent getType() {
        return type;
    }

    public boolean testerTalent() {
        // Lance 2 dés (2d6)
        int resultat = (int) (Math.random() * 6) + 1 + (int) (Math.random() * 6) + 1;
        return resultat <= valeur;
    }

    public boolean testerTalentReduit() {
        // Pour le combat, avec talent divisé par 2 (arrondi au supérieur)
        int valeurReduite = (valeur + 1) / 2;
        int resultat = (int) (Math.random() * 6) + 1 + (int) (Math.random() * 6) + 1;
        return resultat <= valeurReduite;
    }

    public boolean estCoupCritique() {
        // Double-as (1,1) sur 2d6
        int de1 = (int) (Math.random() * 6) + 1;
        int de2 = (int) (Math.random() * 6) + 1;
        return de1 == 1 && de2 == 1;
    }
} 