package jeu;

import java.util.Random;

public class Combat {
    private static final Random random = new Random();
    
    private final Personnage hero;
    private final Arme armeHero;
    
    public Combat(Personnage hero, Arme armeHero) {
        this.hero = hero;
        this.armeHero = armeHero;
    }
    
    public ResultatCombat simuler() {
        System.out.println("Début du combat entre " + hero.getNom() + " et " + "un monstre");
        System.out.println(hero.getNom() + " : " + hero.getPointsDeVie() + " PV");
        
        boolean tourHero = true; // Le héros commence
        
        while (hero.getPointsDeVie() > 0) {
            if (tourHero) {
                // Tour du héros
                attaqueHero();
            } else {
                // Tour du monstre
                attaqueMonstre();
            }
            
            // Changer de tour
            tourHero = !tourHero;
        }
        
        // Déterminer le vainqueur
        if (hero.getPointsDeVie() <= 0) {
            System.out.println(hero.getNom() + " a été vaincu !");
            return ResultatCombat.DEFAITE;
        } else {
            System.out.println("Le monstre a été vaincu !");
            return ResultatCombat.VICTOIRE;
        }
    }
    
    private void attaqueHero() {
        System.out.println("\n" + hero.getNom() + " attaque avec " + armeHero.getNom());
        
        // Lancer le dé pour l'attaque
        int resultatDe = lancerDe(20);
        System.out.println("Résultat du dé : " + resultatDe);
        
        // Ajouter le bonus de combat
        int bonusCombat = hero.getCompetence(TestCompetence.TypeCompetence.COMBAT);
        int resultatTotal = resultatDe + bonusCombat;
        System.out.println("Bonus de combat : " + bonusCombat);
        System.out.println("Résultat total : " + resultatTotal);
        
        // Vérifier si l'attaque touche
        if (resultatTotal >= 10 + hero.getCompetence(TestCompetence.TypeCompetence.COMBAT) / 2) {
            // Calculer les dégâts
            int degats = armeHero.calculerDegats();
            System.out.println("Touché ! Dégâts infligés : " + degats);
            
            // Appliquer les dégâts
            hero.modifierPointsDeVie(-degats);
            System.out.println(hero.getNom() + " a maintenant " + hero.getPointsDeVie() + " PV");
            
            // Vérifier si le héros est mort
            if (hero.getPointsDeVie() <= 0) {
                System.out.println(hero.getNom() + " est tombé inconscient !");
                hero.modifierPointsDeVie(-hero.getPointsDeVie()); // Met les points de vie à 0
            }
        } else {
            System.out.println("Raté !");
        }
    }
    
    private void attaqueMonstre() {
        System.out.println("\nUn monstre attaque !");
        
        // Lancer le dé pour l'attaque
        int resultatDe = lancerDe(20);
        System.out.println("Résultat du dé : " + resultatDe);
        
        // Ajouter le bonus d'attaque du monstre
        int resultatTotal = resultatDe + 5; // Bonus d'attaque du monstre
        System.out.println("Bonus d'attaque : " + 5);
        System.out.println("Résultat total : " + resultatTotal);
        
        // Vérifier si l'attaque touche
        if (resultatTotal >= 10 + hero.getCompetence(TestCompetence.TypeCompetence.COMBAT) / 2) {
            // Calculer les dégâts
            int degats = 5; // Dégâts infligés par le monstre
            System.out.println("Touché ! Dégâts infligés : " + degats);
            
            // Appliquer les dégâts
            hero.modifierPointsDeVie(-degats);
            System.out.println(hero.getNom() + " a maintenant " + hero.getPointsDeVie() + " PV");
            
            // Vérifier si le héros est mort
            if (hero.getPointsDeVie() <= 0) {
                System.out.println(hero.getNom() + " est tombé inconscient !");
                hero.modifierPointsDeVie(-hero.getPointsDeVie()); // Met les points de vie à 0
            }
        } else {
            System.out.println("Raté !");
        }
    }
    
    private int lancerDe(int faces) {
        return random.nextInt(faces) + 1;
    }
    
    public enum ResultatCombat {
        VICTOIRE,
        DEFAITE
    }
} 