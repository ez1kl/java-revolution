package jeu;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScenarioLoader {
    private static final Pattern PATTERN_SUIVANT = Pattern.compile("Rendez-vous au (\\d+)");
    private static final Pattern PATTERN_SUIVANT_ALT = Pattern.compile("rendez-vous au (\\d+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_SUIVANT_ALT2 = Pattern.compile("au paragraphe (\\d+)", Pattern.CASE_INSENSITIVE);
    
    // Patterns pour les tests de compétences
    private static final Pattern PATTERN_TEST = Pattern.compile("Testez votre (\\w+)[. ]", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_TEST_ALT = Pattern.compile("testez votre (\\w+) pour", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_TEST_ALT2 = Pattern.compile("Si vous voulez .* testez votre (\\w+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_TEST_ALT3 = Pattern.compile("faire appel à (?:toute )?votre (\\w+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_TEST_ALT4 = Pattern.compile("Lancez les dés", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_DIFFICULTE = Pattern.compile("difficulté (Facile|Moyenne|Difficile)", Pattern.CASE_INSENSITIVE);
    
    // Patterns pour les combats
    private static final Pattern PATTERN_COMBAT = Pattern.compile("(\\w+)\\s*\\(HABILETÉ\\s*:\\s*(\\d+)\\s*/\\s*ENDURANCE\\s*:\\s*(\\d+)\\)", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_COMBAT_RESULTAT = Pattern.compile("Si vous êtes vainqueur, rendez-vous au (\\d+)\\. Si vous êtes vaincu, rendez-vous au (\\d+)\\.", Pattern.CASE_INSENSITIVE);
    
    // Patterns pour les résultats des tests
    private static final Pattern PATTERN_TEST_RESULTAT = Pattern.compile("Si .* favorable, rendez-vous au (\\d+)\\. Sinon, rendez-vous au (\\d+)\\.", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_TEST_RESULTAT_ALT = Pattern.compile("Si vous réussissez, .* \\(rendez-vous au (\\d+)\\)\\. Si vous échouez, rendez-vous au (\\d+)\\.", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_TEST_RESULTAT_ALT2 = Pattern.compile("Si vous réussissez, .* rendez-vous au (\\d+)\\. Si vous échouez, rendez-vous au (\\d+)\\.", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_TEST_RESULTAT_ALT3 = Pattern.compile("Si vous touchez.*rendez-vous au (\\d+)\\. Si vous le manquez, rendez-vous au (\\d+)\\.", Pattern.CASE_INSENSITIVE);
    
    private static final String[] CHEMINS_SCENARIO = {
        "src/main/resources/scenario.json",
        "resources/scenario.json",
        "scenario.json"
    };

    public Scenario chargerScenario() {
        String contenuJson = null;
        
        // Essayer de charger le fichier depuis différents chemins
        for (String chemin : CHEMINS_SCENARIO) {
            try {
                Path path = Paths.get(chemin);
                if (Files.exists(path)) {
                    contenuJson = Files.readString(path);
                    System.out.println("Fichier trouvé à : " + path.toAbsolutePath());
                    break;
                }
            } catch (IOException e) {
                System.err.println("Erreur lors de la lecture de " + chemin + ": " + e.getMessage());
            }
        }

        if (contenuJson == null) {
            System.err.println("Aucun fichier scenario.json trouvé dans les chemins recherchés.");
            return null;
        }

        try {
            Scenario scenario = new Scenario();
            String[] lignes = contenuJson.split("\\n");
            Chapitre chapitreActuel = null;
            List<Choix> choixActuels = new ArrayList<>();
            StringBuilder texteBuilder = new StringBuilder();
            boolean dansChoix = false;
            String texteChoixCourant = null;
            int destinationChoixCourant = -1;

            for (int i = 0; i < lignes.length; i++) {
                String ligne = lignes[i].trim();
                if (ligne.isEmpty() || ligne.equals("{") || ligne.equals("}") || 
                    ligne.equals("[") || ligne.equals("]") || ligne.equals(",")) {
                    continue;
                }

                if (ligne.contains("\"id\":")) {
                    // Finaliser le chapitre précédent s'il existe
                    if (chapitreActuel != null) {
                        // Transférer le texte accumulé
                        chapitreActuel.setTexte(texteBuilder.toString().trim());
                        texteBuilder.setLength(0);
                        for (Choix choix : choixActuels) {
                            chapitreActuel.addChoix(choix);
                        }
                        scenario.addChapitre(chapitreActuel);
                        System.out.println("Chapitre " + chapitreActuel.getId() + " ajouté, " + 
                            (chapitreActuel.getNextChapter() != null ? "suivant -> " + chapitreActuel.getNextChapter() + ", " : "") +
                            "choix -> " + (chapitreActuel.getChoix() != null ? chapitreActuel.getChoix().size() : 0));
                    }
                    
                    // Créer un nouveau chapitre
                    int id = Integer.parseInt(ligne.split(":")[1].trim().replace(",", ""));
                    chapitreActuel = new Chapitre(id);
                    choixActuels.clear();
                    texteBuilder.setLength(0);
                    dansChoix = false;
                    texteChoixCourant = null;
                    destinationChoixCourant = -1;
                }
                else if ((ligne.contains("\"texte\":") || ligne.contains("\"text\":") || ligne.contains("\"test\":")) && !dansChoix) {
                    // Extraire la valeur du champ texte avec une regex
                    Pattern patternTexte = Pattern.compile("\\\"(?:texte|text|test)\\\"\\s*:\\s*\\\"(.*)\\\"[,}]?");
                    Matcher matcherTexte = patternTexte.matcher(ligne);
                    String texte = "";
                    if (matcherTexte.find()) {
                        texte = matcherTexte.group(1);
                    } else {
                        // Fallback si la ligne ne correspond pas exactement
                        texteChoixCourant = ligne.substring(ligne.indexOf(":") + 1).trim();
                        if (texteChoixCourant.startsWith("\"")) texteChoixCourant = texteChoixCourant.substring(1);
                        if (texteChoixCourant.endsWith("\"")) texteChoixCourant = texteChoixCourant.substring(0, texteChoixCourant.length() - 1);
                    }
                    texteBuilder.append(texte);

                    // Détecter le chapitre suivant dans le texte
                    Matcher matcher = PATTERN_SUIVANT.matcher(texte);
                    Matcher matcherAlt = PATTERN_SUIVANT_ALT.matcher(texte);
                    Matcher matcherAlt2 = PATTERN_SUIVANT_ALT2.matcher(texte);
                    
                    // Détecter les tests de compétence avec différents patterns
                    Matcher matcherTest = PATTERN_TEST.matcher(texte);
                    Matcher matcherTestAlt = PATTERN_TEST_ALT.matcher(texte);
                    Matcher matcherTestAlt2 = PATTERN_TEST_ALT2.matcher(texte);
                    Matcher matcherTestAlt3 = PATTERN_TEST_ALT3.matcher(texte);
                    Matcher matcherTestAlt4 = PATTERN_TEST_ALT4.matcher(texte);
                    
                    // Détecter les combats
                    Matcher matcherCombat = PATTERN_COMBAT.matcher(texte);
                    Matcher matcherCombatResultat = PATTERN_COMBAT_RESULTAT.matcher(texte);
                    
                    Matcher matcherTestResultat = PATTERN_TEST_RESULTAT.matcher(texte);
                    Matcher matcherTestResultatAlt = PATTERN_TEST_RESULTAT_ALT.matcher(texte);
                    Matcher matcherTestResultatAlt2 = PATTERN_TEST_RESULTAT_ALT2.matcher(texte);
                    Matcher matcherTestResultatAlt3 = PATTERN_TEST_RESULTAT_ALT3.matcher(texte);
                    
                    // Vérifier si le texte contient un test de compétence
                    boolean testTrouve = false;
                    String competence = null;
                    TestCompetence.Difficulte difficulte = TestCompetence.Difficulte.MOYENNE; // Par défaut
                    
                    if (matcherTest.find()) {
                        competence = matcherTest.group(1).toUpperCase();
                        testTrouve = true;
                    } else if (matcherTestAlt.find()) {
                        competence = matcherTestAlt.group(1).toUpperCase();
                        testTrouve = true;
                    } else if (matcherTestAlt2.find()) {
                        competence = matcherTestAlt2.group(1).toUpperCase();
                        testTrouve = true;
                    } else if (matcherTestAlt3.find()) {
                        competence = matcherTestAlt3.group(1).toUpperCase();
                        testTrouve = true;
                    } else if (matcherTestAlt4.find()) {
                        // Si on trouve 'Lancez les dés', on cherche la compétence dans la phrase précédente
                        int idx = texte.toLowerCase().indexOf("lancez les dés");
                        if (idx > 0) {
                            String avant = texte.substring(0, idx);
                            Pattern p = Pattern.compile("(Diplomatie|Discrétion|Combat|Charisme|Perception|Adresse)", Pattern.CASE_INSENSITIVE);
                            Matcher m = p.matcher(avant);
                            String last = null;
                            while (m.find()) last = m.group(1);
                            if (last != null) {
                                competence = last.toUpperCase();
                                testTrouve = true;
                            }
                        }
                    }
                    
                    // Détecter la difficulté
                    Matcher matcherDifficulte = PATTERN_DIFFICULTE.matcher(texte);
                    if (matcherDifficulte.find()) {
                        String niveauDifficulte = matcherDifficulte.group(1).toUpperCase();
                        try {
                            difficulte = TestCompetence.Difficulte.valueOf(niveauDifficulte);
                        } catch (IllegalArgumentException e) {
                            System.err.println("Niveau de difficulté invalide: " + niveauDifficulte);
                        }
                    }
                    
                    // Vérifier si le texte contient un combat
                    boolean combatTrouve = false;
                    String nomAdversaire = null;
                    int habileteAdversaire = 0;
                    int enduranceAdversaire = 0;
                    
                    if (matcherCombat.find()) {
                        nomAdversaire = matcherCombat.group(1);
                        habileteAdversaire = Integer.parseInt(matcherCombat.group(2));
                        enduranceAdversaire = Integer.parseInt(matcherCombat.group(3));
                        combatTrouve = true;
                    }
                    
                    // Vérifier si le texte contient les résultats du test ou du combat
                    int chapitreSucces = -1;
                    int chapitreEchec = -1;
                    
                    if (matcherTestResultat.find()) {
                        chapitreSucces = Integer.parseInt(matcherTestResultat.group(1));
                        chapitreEchec = Integer.parseInt(matcherTestResultat.group(2));
                    } else if (matcherTestResultatAlt.find()) {
                        chapitreSucces = Integer.parseInt(matcherTestResultatAlt.group(1));
                        chapitreEchec = Integer.parseInt(matcherTestResultatAlt.group(2));
                    } else if (matcherTestResultatAlt2.find()) {
                        chapitreSucces = Integer.parseInt(matcherTestResultatAlt2.group(1));
                        chapitreEchec = Integer.parseInt(matcherTestResultatAlt2.group(2));
                    } else if (matcherTestResultatAlt3.find()) {
                        chapitreSucces = Integer.parseInt(matcherTestResultatAlt3.group(1));
                        chapitreEchec = Integer.parseInt(matcherTestResultatAlt3.group(2));
                    } else if (matcherCombatResultat.find()) {
                        chapitreSucces = Integer.parseInt(matcherCombatResultat.group(1));
                        chapitreEchec = Integer.parseInt(matcherCombatResultat.group(2));
                    } else {
                        // Recherche manuelle des chapitres de succès et d'échec
                        Pattern patternSucces = Pattern.compile("Si vous réussissez.*rendez-vous au (\\d+)", Pattern.CASE_INSENSITIVE);
                        Pattern patternEchec = Pattern.compile("Si vous échouez.*rendez-vous au (\\d+)", Pattern.CASE_INSENSITIVE);
                        Pattern patternVainqueur = Pattern.compile("Si vous êtes vainqueur.*rendez-vous au (\\d+)", Pattern.CASE_INSENSITIVE);
                        Pattern patternVaincu = Pattern.compile("Si vous êtes vaincu.*rendez-vous au (\\d+)", Pattern.CASE_INSENSITIVE);
                        
                        Matcher matcherSucces = patternSucces.matcher(texte);
                        Matcher matcherEchec = patternEchec.matcher(texte);
                        Matcher matcherVainqueur = patternVainqueur.matcher(texte);
                        Matcher matcherVaincu = patternVaincu.matcher(texte);
                        
                        if (matcherSucces.find()) {
                            chapitreSucces = Integer.parseInt(matcherSucces.group(1));
                        } else if (matcherVainqueur.find()) {
                            chapitreSucces = Integer.parseInt(matcherVainqueur.group(1));
                        }
                        
                        if (matcherEchec.find()) {
                            chapitreEchec = Integer.parseInt(matcherEchec.group(1));
                        } else if (matcherVaincu.find()) {
                            chapitreEchec = Integer.parseInt(matcherVaincu.group(1));
                        }
                    }
                    
                    // Si un test de compétence a été trouvé et que les chapitres de succès et d'échec sont valides
                    if (testTrouve && chapitreSucces != -1 && chapitreEchec != -1 && chapitreActuel != null) {
                        try {
                            TestCompetence.TypeCompetence typeCompetence = TestCompetence.TypeCompetence.valueOf(competence);
                            chapitreActuel.setTestRequis(typeCompetence, difficulte);
                            chapitreActuel.setChapitreSucces(chapitreSucces);
                            chapitreActuel.setChapitreEchec(chapitreEchec);
                            
                            System.out.println("Détecté test de " + competence + " (" + difficulte + ") dans le chapitre " + 
                                chapitreActuel.getId() + ". Succès -> " + chapitreSucces + ", Échec -> " + chapitreEchec);
                        } catch (IllegalArgumentException e) {
                            System.err.println("Type de compétence invalide: " + competence);
                        }
                    } 
                    // Si un combat a été trouvé et que les chapitres de succès et d'échec sont valides
                    else if (combatTrouve && chapitreSucces != -1 && chapitreEchec != -1 && chapitreActuel != null) {
                        chapitreActuel.setTestRequis(TestCompetence.TypeCompetence.COMBAT, TestCompetence.Difficulte.MOYENNE);
                        chapitreActuel.setChapitreSucces(chapitreSucces);
                        chapitreActuel.setChapitreEchec(chapitreEchec);
                        
                        System.out.println("Détecté combat contre " + nomAdversaire + " (Habileté: " + habileteAdversaire + 
                            ", Endurance: " + enduranceAdversaire + ") dans le chapitre " + 
                            chapitreActuel.getId() + ". Victoire -> " + chapitreSucces + ", Défaite -> " + chapitreEchec);
                    }
                    else if (matcher.find() && chapitreActuel != null) {
                        int suivant = Integer.parseInt(matcher.group(1));
                        chapitreActuel.setNextChapter(suivant);
                        System.out.println("Détecté 'Rendez-vous au " + suivant + "' dans le texte");
                    } else if (matcherAlt.find() && chapitreActuel != null) {
                        int suivant = Integer.parseInt(matcherAlt.group(1));
                        chapitreActuel.setNextChapter(suivant);
                        System.out.println("Détecté 'rendez-vous au " + suivant + "' dans le texte");
                    } else if (matcherAlt2.find() && chapitreActuel != null) {
                        int suivant = Integer.parseInt(matcherAlt2.group(1));
                        chapitreActuel.setNextChapter(suivant);
                        System.out.println("Détecté 'au paragraphe " + suivant + "' dans le texte");
                    }
                }
                else if (ligne.contains("\"choix\"") || ligne.contains("\"choices\"")) {
                    if (chapitreActuel != null) {
                        chapitreActuel.setTexte(texteBuilder.toString().trim());
                    }
                    dansChoix = true;
                }
                else if (dansChoix && (ligne.contains("\"texte\"") || ligne.contains("\"text\""))) {
                    // Sauvegarder le texte du choix courant
                    texteChoixCourant = ligne.substring(ligne.indexOf(":") + 1).trim();
                    if (texteChoixCourant.startsWith("\"")) texteChoixCourant = texteChoixCourant.substring(1);
                    if (texteChoixCourant.endsWith("\",")) texteChoixCourant = texteChoixCourant.substring(0, texteChoixCourant.length() - 2);
                    else if (texteChoixCourant.endsWith("\'")) texteChoixCourant = texteChoixCourant.substring(0, texteChoixCourant.length() - 2);
                    else if (texteChoixCourant.endsWith(",")) texteChoixCourant = texteChoixCourant.substring(0, texteChoixCourant.length() - 1);
                    if (texteChoixCourant.endsWith("\"")) texteChoixCourant = texteChoixCourant.substring(0, texteChoixCourant.length() - 1);
                    
                    // Vérifier si le texte contient un test de compétence
                    java.util.regex.Pattern patternTest = java.util.regex.Pattern.compile(
                        "(?i)(?:Testez|Test(?:er)? (?:votre|de)) (Force|Combat|Agilité|Charisme|Perception|Diplomatie|Discrétion)(?: \\(difficulté (Facile|Moyenne|Difficile)\\))?"
                    );
                    java.util.regex.Matcher matcherTest = patternTest.matcher(texteChoixCourant);
                    
                    if (matcherTest.find()) {
                        String competence = matcherTest.group(1);
                        String difficulte = matcherTest.group(2);
                        
                        TestCompetence.TypeCompetence typeCompetence;
                        switch (competence.toLowerCase()) {
                            case "force": typeCompetence = TestCompetence.TypeCompetence.DIPLOMATIE; break;
                            case "combat": typeCompetence = TestCompetence.TypeCompetence.COMBAT; break;
                            case "agilité": 
                            case "discrétion": typeCompetence = TestCompetence.TypeCompetence.DISCRETION; break;
                            case "charisme": typeCompetence = TestCompetence.TypeCompetence.CHARISME; break;
                            case "perception": typeCompetence = TestCompetence.TypeCompetence.PERCEPTION; break;
                            case "diplomatie": typeCompetence = TestCompetence.TypeCompetence.DIPLOMATIE; break;
                            default: typeCompetence = TestCompetence.TypeCompetence.DIPLOMATIE;
                        }
                        
                        TestCompetence.Difficulte niveauDifficulte = TestCompetence.Difficulte.MOYENNE;
                        if (difficulte != null) {
                            switch (difficulte.toLowerCase()) {
                                case "facile": niveauDifficulte = TestCompetence.Difficulte.FACILE; break;
                                case "moyenne": niveauDifficulte = TestCompetence.Difficulte.MOYENNE; break;
                                case "difficile": niveauDifficulte = TestCompetence.Difficulte.DIFFICILE; break;
                            }
                        }
                        
                        // Créer le choix avec le test
                        Choix choix = new Choix(texteChoixCourant, destinationChoixCourant);
                        choix.setTestRequis(typeCompetence);
                        choix.setDifficulteTest(niveauDifficulte);
                        choix.setChapitreSucces(destinationChoixCourant);
                        choix.setChapitreEchec(destinationChoixCourant + 1); // Par défaut, l'échec mène au chapitre suivant
                        choixActuels.add(choix);
                        System.out.println("Ajout du choix avec test: '" + texteChoixCourant + "' -> " + destinationChoixCourant);
                        texteChoixCourant = null;
                        destinationChoixCourant = -1;
                    }
                }
                else if (dansChoix && (ligne.contains("\"destination\"") || ligne.contains("\"suivant\"") || ligne.contains("\"next_id\"")) && texteChoixCourant != null) {
                    // Récupérer la destination et créer le choix
                    String[] parts = ligne.split(":");
                    if (parts.length > 1) {
                        destinationChoixCourant = Integer.parseInt(parts[1].trim().replace(",", ""));
                        choixActuels.add(new Choix(texteChoixCourant, destinationChoixCourant));
                        System.out.println("Ajout du choix: '" + texteChoixCourant + "' -> " + destinationChoixCourant);
                        texteChoixCourant = null;
                        destinationChoixCourant = -1;
                    }
                }
                // Ajout : Parsing explicite du champ 'test' du JSON
                if (ligne.contains("\"test\"")) {
                    // On suppose que la ligne suivante contient les infos du test
                    String type = null;
                    String difficulte = null;
                    Integer succes = null;
                    Integer echec = null;
                    while (!ligne.contains("}")) {
                        ligne = ligne.replace(" ", "");
                        if (ligne.contains("\"type\"")) {
                            type = ligne.split(":")[1].replace(",","").replace("\"","");
                        } else if (ligne.contains("\"difficulte\"")) {
                            difficulte = ligne.split(":")[1].replace(",","").replace("\"","");
                        } else if (ligne.contains("\"succes\"")) {
                            String val = ligne.split(":")[1].replace(",","").replace("\"","");
                            if (!val.equals("null")) succes = Integer.parseInt(val);
                        } else if (ligne.contains("\"echec\"")) {
                            String val = ligne.split(":")[1].replace(",","").replace("\"","");
                            if (!val.equals("null")) echec = Integer.parseInt(val);
                        }
                        ligne = lignes[++i].trim();
                    }
                    if (type != null && difficulte != null && succes != null && echec != null && chapitreActuel != null) {
                        try {
                            TestCompetence.TypeCompetence typeCompetence = TestCompetence.TypeCompetence.valueOf(type.toUpperCase());
                            TestCompetence.Difficulte diff = TestCompetence.Difficulte.valueOf(difficulte.toUpperCase());
                            chapitreActuel.setTestRequis(typeCompetence, diff);
                            chapitreActuel.setChapitreSucces(succes);
                            chapitreActuel.setChapitreEchec(echec);
                            System.out.println("[JSON] Test global détecté pour chapitre " + chapitreActuel.getId() + " : " + type + " (" + difficulte + ") Succès->" + succes + ", Échec->" + echec);
                        } catch (Exception ex) {
                            System.err.println("Erreur parsing test global JSON : " + ex.getMessage());
                        }
                    }
                    continue;
                }
            }

            // Ajouter le dernier chapitre
            if (chapitreActuel != null) {
                if (texteBuilder.length() > 0) {
                    chapitreActuel.setTexte(texteBuilder.toString().trim());
                    texteBuilder.setLength(0);
                }
                for (Choix choix : choixActuels) {
                    chapitreActuel.addChoix(choix);
                }
                scenario.addChapitre(chapitreActuel);
                System.out.println("Dernier chapitre " + chapitreActuel.getId() + " ajouté, " + 
                    (chapitreActuel.getNextChapter() != null ? "suivant -> " + chapitreActuel.getNextChapter() + ", " : "") +
                    "choix -> " + (chapitreActuel.getChoix() != null ? chapitreActuel.getChoix().size() : 0));
            }
            
            // Après avoir ajouté tous les chapitres, vérifier le premier chapitre
            Chapitre premierChapitre = scenario.getPremierChapitre();
            System.out.println("Premier chapitre - ID: " + premierChapitre.getId() + 
                              ", Nombre de choix: " + (premierChapitre.getChoix() != null ? premierChapitre.getChoix().size() : 0) + 
                              ", Chapitre suivant: " + (premierChapitre.hasNextChapter() ? premierChapitre.getNextChapter() : "aucun"));
            
            // Si le premier chapitre n'a ni choix ni chapitre suivant, ajouter un chapitre suivant par défaut
            if ((premierChapitre.getChoix() == null || premierChapitre.getChoix().isEmpty()) && !premierChapitre.hasNextChapter()) {
                System.out.println("Le premier chapitre n'a ni choix ni chapitre suivant. Ajout d'un chapitre suivant par défaut.");
                premierChapitre.setNextChapter(2); // Définir le chapitre 2 comme suite par défaut
            }
            
            System.out.println("Chargement terminé. Total des chapitres : " + scenario.getNombreChapitres());
            if (scenario.getPremierChapitre() != null) {
                System.out.println("Premier chapitre - ID: " + scenario.getPremierChapitre().getId() + 
                                ", Nombre de choix: " + 
                                (scenario.getPremierChapitre().getChoix() != null ? 
                                scenario.getPremierChapitre().getChoix().size() : 0) +
                                (scenario.getPremierChapitre().getNextChapter() != null ? 
                                ", Chapitre suivant: " + scenario.getPremierChapitre().getNextChapter() : ""));
            }

            // Test spécifique pour le chapitre 245
            Chapitre chapitre245 = scenario.getChapitre(245);
            if (chapitre245 != null) {
                chapitre245.setTestRequis(TestCompetence.TypeCompetence.DIPLOMATIE, TestCompetence.Difficulte.MOYENNE);
                chapitre245.setChapitreSucces(76);
                chapitre245.setChapitreEchec(231);
            }

            // Après avoir traité le texte du chapitre, détecter automatiquement un combat si certains mots-clés sont présents
            String texteChapitre = texteBuilder.toString().toLowerCase();
            String[] motsClesCombat = {"combat", "affronter", "duel", "épée", "lame", "bataille", "attaque", "adversaire"};
            boolean motCleCombatDetecte = false;
            for (String motCle : motsClesCombat) {
                if (texteChapitre.contains(motCle) && chapitreActuel != null && !chapitreActuel.requiresTest()) {
                    motCleCombatDetecte = true;
                    break;
                }
            }
            if (motCleCombatDetecte && chapitreActuel != null && !chapitreActuel.requiresTest()) {
                chapitreActuel.setTestRequis(TestCompetence.TypeCompetence.COMBAT, TestCompetence.Difficulte.MOYENNE);
                // Par défaut, succès = chapitre suivant, échec = chapitre actuel
                if (chapitreActuel.getNextChapter() != null) {
                    chapitreActuel.setChapitreSucces(chapitreActuel.getNextChapter());
                    chapitreActuel.setChapitreEchec(chapitreActuel.getId());
                }
            }

            return scenario;
            
        } catch (Exception e) {
            System.err.println("Erreur lors du parsing du JSON : " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private void extraireTestsCompetences(Chapitre chapitre) {
        String texte = chapitre.getTexte();
        
        // Patterns pour les tests de compétences
        Pattern patternTest = Pattern.compile("(?i)(?:Testez|Test(?:er)? (?:votre|de)) (Force|Combat|Agilité|Charisme|Perception|Diplomatie|Discrétion)(?: \\(difficulté (Facile|Moyenne|Difficile)\\))?");
        Matcher matcherTest = patternTest.matcher(texte);
        
        if (matcherTest.find()) {
            String competence = matcherTest.group(1);
            String difficulte = matcherTest.group(2);
            
            TestCompetence.TypeCompetence typeCompetence;
            switch (competence.toLowerCase()) {
                case "force": typeCompetence = TestCompetence.TypeCompetence.DIPLOMATIE; break;
                case "combat": typeCompetence = TestCompetence.TypeCompetence.COMBAT; break;
                case "agilité": 
                case "discrétion": typeCompetence = TestCompetence.TypeCompetence.DISCRETION; break;
                case "charisme": typeCompetence = TestCompetence.TypeCompetence.CHARISME; break;
                case "perception": typeCompetence = TestCompetence.TypeCompetence.PERCEPTION; break;
                case "diplomatie": typeCompetence = TestCompetence.TypeCompetence.DIPLOMATIE; break;
                default: typeCompetence = TestCompetence.TypeCompetence.DIPLOMATIE;
            }
            
            TestCompetence.Difficulte niveauDifficulte = TestCompetence.Difficulte.MOYENNE;
            if (difficulte != null) {
                switch (difficulte.toLowerCase()) {
                    case "facile": niveauDifficulte = TestCompetence.Difficulte.FACILE; break;
                    case "moyenne": niveauDifficulte = TestCompetence.Difficulte.MOYENNE; break;
                    case "difficile": niveauDifficulte = TestCompetence.Difficulte.DIFFICILE; break;
                }
            }
            
            chapitre.setTestRequis(typeCompetence, niveauDifficulte);
            
            // Chercher les chapitres de succès et d'échec
            extraireChapitresTestResultat(chapitre, texte);
        }
        
        // Extraire les informations de combat si présentes
        extraireInformationsCombat(chapitre, texte);
    }
    
    private void extraireInformationsCombat(Chapitre chapitre, String texte) {
        // Pattern pour les combats avec valeur de combat et points de vie
        Pattern patternCombat = Pattern.compile("(?i)(?:Combat|Adversaire|Ennemi)[^.]*?(?:valeur de combat|habileté)[^.]*?(\\d+)[^.]*?(?:points de vie|santé)[^.]*?(\\d+)");
        Matcher matcherCombat = patternCombat.matcher(texte);
        
        if (matcherCombat.find()) {
            try {
                int valeurCombat = Integer.parseInt(matcherCombat.group(1));
                int pointsDeVie = Integer.parseInt(matcherCombat.group(2));
                
                // Stocker ces informations dans le chapitre
                chapitre.setValeurCombatAdversaire(valeurCombat);
                chapitre.setPointsDeVieAdversaire(pointsDeVie);
                
                // Si c'est un combat, définir le test requis comme COMBAT
                if (!chapitre.requiresTest()) {
                    chapitre.setTestRequis(TestCompetence.TypeCompetence.COMBAT, TestCompetence.Difficulte.MOYENNE);
                    
                    // Chercher les chapitres de succès et d'échec si pas déjà définis
                    extraireChapitresTestResultat(chapitre, texte);
                }
                
                System.out.println("Combat détecté dans le chapitre " + chapitre.getId() + 
                                  " - Adversaire: Combat=" + valeurCombat + ", PV=" + pointsDeVie);
            } catch (NumberFormatException e) {
                System.out.println("Erreur lors de l'extraction des valeurs de combat pour le chapitre " + chapitre.getId());
            }
        }
    }
    
    private void extraireChapitresTestResultat(Chapitre chapitre, String texte) {
        // Pattern pour trouver le chapitre en cas de succès
        Pattern patternSucces = Pattern.compile("(?i)(?:Si vous réussissez|En cas de réussite|Si vous gagnez|Si vous êtes victorieux)[^.]*?(?:rendez-vous|allez|paragraphe|chapitre)[^\\d]*(\\d+)");
        Matcher matcherSucces = patternSucces.matcher(texte);
        
        // Pattern pour trouver le chapitre en cas d'échec
        Pattern patternEchec = Pattern.compile("(?i)(?:Si vous échouez|En cas d'échec|Si vous perdez|Si vous êtes vaincu)[^.]*?(?:rendez-vous|allez|paragraphe|chapitre)[^\\d]*(\\d+)");
        Matcher matcherEchec = patternEchec.matcher(texte);
        
        // Extraire le chapitre de succès
        if (matcherSucces.find()) {
            try {
                int chapitreSucces = Integer.parseInt(matcherSucces.group(1));
                chapitre.setChapitreSucces(chapitreSucces);
                System.out.println("Chapitre " + chapitre.getId() + " - En cas de succès: chapitre " + chapitreSucces);
            } catch (NumberFormatException e) {
                System.out.println("Erreur lors de l'extraction du chapitre de succès pour le chapitre " + chapitre.getId());
            }
        } else {
            // Si pas trouvé, utiliser le chapitre suivant par défaut
            if (chapitre.getNextChapter() != null) {
                chapitre.setChapitreSucces(chapitre.getNextChapter());
                System.out.println("Chapitre " + chapitre.getId() + " - Utilisation du chapitre suivant comme succès: " + chapitre.getNextChapter());
            } else {
                // Si pas de chapitre suivant, utiliser le chapitre actuel + 1
                chapitre.setChapitreSucces(chapitre.getId() + 1);
                System.out.println("Chapitre " + chapitre.getId() + " - Utilisation du chapitre " + (chapitre.getId() + 1) + " comme succès par défaut");
            }
        }
        
        // Extraire le chapitre d'échec
        if (matcherEchec.find()) {
            try {
                int chapitreEchec = Integer.parseInt(matcherEchec.group(1));
                chapitre.setChapitreEchec(chapitreEchec);
                System.out.println("Chapitre " + chapitre.getId() + " - En cas d'échec: chapitre " + chapitreEchec);
            } catch (NumberFormatException e) {
                System.out.println("Erreur lors de l'extraction du chapitre d'échec pour le chapitre " + chapitre.getId());
            }
        } else {
            // Si pas trouvé, utiliser le chapitre actuel comme échec (rester sur place)
            chapitre.setChapitreEchec(chapitre.getId());
            System.out.println("Chapitre " + chapitre.getId() + " - Utilisation du chapitre actuel comme échec par défaut");
        }
    }
}
