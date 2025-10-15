package jeu;

public class Choix {
    private String texte;
    private int destination;
    private TestCompetence.TypeCompetence testRequis;
    private TestCompetence.Difficulte difficulteTest;
    private int chapitreSucces;
    private int chapitreEchec;

    public Choix(String texte, int destination) {
        this.texte = texte;
        this.destination = destination;
    }

    public String getTexte() {
        return texte;
    }

    public int getDestination() {
        return destination;
    }

    public TestCompetence.TypeCompetence getTestRequis() {
        return testRequis;
    }

    public void setTestRequis(TestCompetence.TypeCompetence testRequis) {
        this.testRequis = testRequis;
    }

    public TestCompetence.Difficulte getDifficulteTest() {
        return difficulteTest;
    }

    public void setDifficulteTest(TestCompetence.Difficulte difficulteTest) {
        this.difficulteTest = difficulteTest;
    }

    public int getChapitreSucces() {
        return chapitreSucces;
    }

    public void setChapitreSucces(int chapitreSucces) {
        this.chapitreSucces = chapitreSucces;
    }

    public int getChapitreEchec() {
        return chapitreEchec;
    }

    public void setChapitreEchec(int chapitreEchec) {
        this.chapitreEchec = chapitreEchec;
    }

    public boolean requiresTest() {
        return testRequis != null && difficulteTest != null && chapitreSucces > 0 && chapitreEchec > 0;
    }
}
