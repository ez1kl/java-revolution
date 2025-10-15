package jeu;

import java.util.ArrayList;
import java.util.List;

public class Chapitre {
    private int id;
    private String texte;
    private List<Choix> choix;
    private Integer nextChapter;
    private TestCompetence.TypeCompetence testRequis;  // Type de test requis
    private TestCompetence.Difficulte difficulteTest;  // Difficulté du test
    private Integer chapitreSucces;  // Chapitre en cas de réussite du test
    private Integer chapitreEchec;   // Chapitre en cas d'échec du test
    private Integer valeurCombatAdversaire; // Valeur de combat de l'adversaire
    private Integer pointsDeVieAdversaire;  // Points de vie de l'adversaire

    public Chapitre(int id) {
        this.id = id;
        this.choix = new ArrayList<>();
        
        // Configuration spéciale pour le chapitre 55
        if (id == 55) {
            this.testRequis = TestCompetence.TypeCompetence.DIPLOMATIE;
            this.difficulteTest = TestCompetence.Difficulte.MOYENNE;
            this.chapitreSucces = 120;
            this.chapitreEchec = 15;
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTexte() {
        return texte;
    }

    public void setTexte(String texte) {
        this.texte = texte;
        // Si pas de choix définis, chercher une référence de chapitre dans le texte
        if ((choix == null || choix.isEmpty()) && nextChapter == null) {
            // Patterns pour détecter les références aux chapitres
            java.util.regex.Pattern[] patterns = {
                java.util.regex.Pattern.compile("Rendez-vous (?:maintenant )?au (\\d+)", java.util.regex.Pattern.CASE_INSENSITIVE),
                java.util.regex.Pattern.compile("rendez-vous (?:maintenant )?au (\\d+)", java.util.regex.Pattern.CASE_INSENSITIVE),
                java.util.regex.Pattern.compile("au paragraphe (\\d+)", java.util.regex.Pattern.CASE_INSENSITIVE),
                java.util.regex.Pattern.compile("allez au (\\d+)", java.util.regex.Pattern.CASE_INSENSITIVE)
            };
            
            // Chercher une correspondance avec chaque pattern
            for (java.util.regex.Pattern pattern : patterns) {
                java.util.regex.Matcher matcher = pattern.matcher(texte);
                if (matcher.find()) {
                    try {
                        this.nextChapter = Integer.parseInt(matcher.group(1));
                        break;
                    } catch (NumberFormatException e) {
                        // Ignorer les erreurs de parsing
                    }
                }
            }
        }
    }

    public List<Choix> getChoix() {
        return choix;
    }

    public void addChoix(Choix choix) {
        this.choix.add(choix);
    }

    public Integer getNextChapter() {
        return nextChapter;
    }

    public void setNextChapter(Integer nextChapter) {
        this.nextChapter = nextChapter;
    }

    public boolean hasNextChapter() {
        return nextChapter != null || requiresTest();
    }

    public TestCompetence.TypeCompetence getTestRequis() {
        return testRequis;
    }

    public TestCompetence.Difficulte getDifficulteTest() {
        return difficulteTest;
    }

    public Integer getChapitreSucces() {
        return chapitreSucces;
    }

    public Integer getChapitreEchec() {
        return chapitreEchec;
    }

    public void setTestRequis(TestCompetence.TypeCompetence testRequis) {
        this.testRequis = testRequis;
    }

    public void setTestRequis(TestCompetence.TypeCompetence testRequis, TestCompetence.Difficulte difficulte) {
        this.testRequis = testRequis;
        this.difficulteTest = difficulte;
    }

    public void setChapitreSucces(Integer chapitreSucces) {
        this.chapitreSucces = chapitreSucces;
    }

    public void setChapitreEchec(Integer chapitreEchec) {
        this.chapitreEchec = chapitreEchec;
    }

    public boolean requiresTest() {
        return testRequis != null && difficulteTest != null && 
               chapitreSucces != null && chapitreEchec != null;
    }

    public Integer getValeurCombatAdversaire() {
        return valeurCombatAdversaire;
    }

    public void setValeurCombatAdversaire(Integer valeurCombatAdversaire) {
        this.valeurCombatAdversaire = valeurCombatAdversaire;
    }

    public Integer getPointsDeVieAdversaire() {
        return pointsDeVieAdversaire;
    }

    public void setPointsDeVieAdversaire(Integer pointsDeVieAdversaire) {
        this.pointsDeVieAdversaire = pointsDeVieAdversaire;
    }
}
