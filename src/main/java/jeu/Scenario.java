package jeu;

import java.util.HashMap;
import java.util.Map;

public class Scenario {
    private final Map<Integer, Chapitre> chapitres;

    public Scenario() {
        this.chapitres = new HashMap<>();
    }

    public void addChapitre(Chapitre chapitre) {
        chapitres.put(chapitre.getId(), chapitre);
    }

    public Chapitre getChapitre(int id) {
        return chapitres.get(id);
    }

    public Chapitre getPremierChapitre() {
        return chapitres.get(1);
    }

    public int getNombreChapitres() {
        return chapitres.size();
    }
}
