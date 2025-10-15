package jeu;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class GameUI extends JFrame {
    private final Personnage joueur;
    private final Scenario scenario;
    private Chapitre chapitre;
    private final JTextArea texteArea;
    private final JPanel panneauBoutons;
    private final JScrollPane scrollPane;
    private final JMenuBar menuBar;
    private final String pseudo;
    private static final String DOSSIER_SAUVEGARDES = "sauvegardes";
    private final VoiceManager voiceManager;
    private final JButton btnSourdine;
    
    // Couleurs du thème
    private final Color bleuFrance = new Color(0, 85, 164);
    private final Color rougeFrance = new Color(239, 65, 53);
    private final Color blancFrance = new Color(255, 255, 255);
    
    // Fonction pour styliser les boutons
    private final java.util.function.Consumer<JButton> styliserBouton = (bouton) -> {
        bouton.setFont(new Font("Serif", Font.BOLD, 16));
        bouton.setPreferredSize(new Dimension(200, 50));
        bouton.setBackground(blancFrance);
        bouton.setForeground(Color.BLACK);
        bouton.setBorder(new LineBorder(rougeFrance, 3));
        bouton.setFocusPainted(false);
    };

    public GameUI(Personnage joueur) {
        this(joueur, "Héros");
    }
    
    public GameUI(Personnage joueur, String pseudo) {
        super("L'Ombre de la Guillotine - " + pseudo);
        this.joueur = joueur;
        this.pseudo = pseudo;
        this.voiceManager = VoiceManager.getInstance();
        
        // Créer le dossier de sauvegardes s'il n'existe pas
        File dossierSauvegardes = new File(DOSSIER_SAUVEGARDES);
        if (!dossierSauvegardes.exists()) {
            dossierSauvegardes.mkdir();
        }
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);
        
        // Définir les couleurs du thème révolutionnaire français
        Color fondTexte = new Color(253, 240, 213);  // Couleur parchemin
        Color texteColor = new Color(60, 30, 10);    // Brun foncé pour le texte
        
        // Appliquer le thème à la fenêtre
        getContentPane().setBackground(fondTexte);

        // Création de la barre de menu
        menuBar = new JMenuBar();
        menuBar.setBackground(bleuFrance);
        menuBar.setBorder(new LineBorder(rougeFrance, 1));
        
        JMenu menuFichier = new JMenu("Fichier");
        menuFichier.setForeground(Color.WHITE);
        JMenuItem sauvegarder = new JMenuItem("Sauvegarder");
        JMenuItem aide = new JMenuItem("Aide");
        JMenuItem quitter = new JMenuItem("Quitter");
        
        sauvegarder.addActionListener(e -> sauvegarderPartie(true));
        aide.addActionListener(e -> afficherAide());
        quitter.addActionListener(e -> retournerAuMenu());
        
        menuFichier.add(sauvegarder);
        menuFichier.add(aide);
        menuFichier.addSeparator();
        menuFichier.add(quitter);
        menuBar.add(menuFichier);
        setJMenuBar(menuBar);

        // Création des composants
        texteArea = new JTextArea();
        texteArea.setEditable(false);
        texteArea.setLineWrap(true);
        texteArea.setWrapStyleWord(true);
        texteArea.setFont(new Font("Serif", Font.PLAIN, 16));
        texteArea.setBackground(fondTexte);
        texteArea.setForeground(texteColor);
        texteArea.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        scrollPane = new JScrollPane(texteArea);
        scrollPane.setBorder(new LineBorder(rougeFrance, 2));
        
        // Panneau pour les boutons en bas
        panneauBoutons = new JPanel();
        panneauBoutons.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panneauBoutons.setBackground(fondTexte);
        panneauBoutons.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Bouton de sourdine
        btnSourdine = new JButton("🔇"); // Sourdine par défaut
        btnSourdine.setFont(new Font("Serif", Font.BOLD, 20));
        btnSourdine.setPreferredSize(new Dimension(50, 50));
        btnSourdine.setBackground(blancFrance);
        btnSourdine.setForeground(Color.BLACK);
        btnSourdine.setBorder(new LineBorder(rougeFrance, 2));
        btnSourdine.setFocusPainted(false);
        btnSourdine.addActionListener(e -> {
            if (voiceManager.isEnabled()) {
                voiceManager.toggleVoice(); // coupe la voix
                btnSourdine.setText("🔇");
            } else {
                voiceManager.toggleVoice(); // active la voix
                btnSourdine.setText("🔊");
            }
        });
        panneauBoutons.add(btnSourdine);

        // Layout
        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
        add(panneauBoutons, BorderLayout.SOUTH);

        // Chargement du scénario
        ScenarioLoader loader = new ScenarioLoader();
        scenario = loader.chargerScenario();
        if (scenario != null) {
            chapitre = scenario.getPremierChapitre();
            SwingUtilities.invokeLater(this::afficherChapitre);
        } else {
            texteArea.setText("Erreur lors du chargement du scénario.");
        }
    }
    
    public void chargerChapitre(int idChapitre) {
        if (scenario != null) {
            chapitre = scenario.getChapitre(idChapitre);
            SwingUtilities.invokeLater(this::afficherChapitre);
        }
    }

    private void afficherChapitre() {
        if (chapitre == null) {
            JOptionPane.showMessageDialog(this,
                "Erreur : Chapitre non trouvé",
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Sauvegarder automatiquement à chaque nouveau chapitre sans afficher de message
        SwingUtilities.invokeLater(() -> sauvegarderPartie(false));
        
        // Créer un panneau pour l'image du chapitre
        JPanel imagePanel = null;
        String nomImage = "chapitre_" + chapitre.getId() + ".jpg";
        if (ImageManager.imageExists(nomImage)) {
            imagePanel = ImageManager.createImagePanel(nomImage, 400, 300);
            imagePanel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        }
        
        // Panneau de contenu principal
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setOpaque(false);
        
        if (imagePanel != null) {
            contentPanel.add(imagePanel, BorderLayout.NORTH);
        }
        
        // Afficher le texte du chapitre
        StringBuilder texte = new StringBuilder();
        texte.append("Chapitre ").append(chapitre.getId()).append("\n\n");
        texte.append(chapitre.getTexte());
        
        // Ajouter les informations du héros
        texte.append("\n\n--- État du héros ---");
        texte.append("\nDiplomatie: ").append(joueur.getCompetence(TestCompetence.TypeCompetence.DIPLOMATIE));
        texte.append(" | Discrétion: ").append(joueur.getCompetence(TestCompetence.TypeCompetence.DISCRETION));
        texte.append(" | Combat: ").append(joueur.getCompetence(TestCompetence.TypeCompetence.COMBAT));
        texte.append(" | Charisme: ").append(joueur.getCompetence(TestCompetence.TypeCompetence.CHARISME));
        texte.append(" | Perception: ").append(joueur.getCompetence(TestCompetence.TypeCompetence.PERCEPTION));
        
        texteArea.setText(texte.toString());
        
        // Vérifier si un test de compétence est requis
        if (chapitre.requiresTest()) {
            panneauBoutons.removeAll();
            panneauBoutons.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));
            panneauBoutons.add(btnSourdine);

            // 1ère étape : Annonce du test
            StringBuilder annonce = new StringBuilder();
            TestCompetence.TypeCompetence typeTest = chapitre.getTestRequis();
            TestCompetence.Difficulte difficulte = chapitre.getDifficulteTest();
            int bonus = joueur.getCompetence(typeTest);
            int seuil = difficulte.getSeuilReussite();
            annonce.append("\n\n--- Test de ").append(typeTest.getNom()).append(" ---\n");
            annonce.append("Vous devez faire au moins ").append(seuil).append(" pour réussir.\n");
            annonce.append("Votre bonus : ").append(bonus).append("\n");
            texteArea.setText(texte.toString() + annonce.toString());

            JButton btnLancer = new JButton("Lancer les 2 dés");
            styliserBouton.accept(btnLancer);
            btnLancer.addActionListener(ev -> {
                int de1 = TestCompetence.lancerDe(6);
                int de2 = TestCompetence.lancerDe(6);
                int resultatDe = de1 + de2;
                int total = resultatDe + bonus;
                StringBuilder etape2 = new StringBuilder();
                etape2.append(texte.toString() + annonce.toString());
                etape2.append("\nVous lancez 2 dés : ").append(de1).append(" + ").append(de2).append(" = ").append(resultatDe);
                etape2.append(" + bonus (").append(bonus).append(") = ").append(total);
                texteArea.setText(etape2.toString());
                panneauBoutons.removeAll();
                panneauBoutons.add(btnSourdine);
                JButton btnVoir = new JButton("Voir le résultat");
                styliserBouton.accept(btnVoir);
                btnVoir.addActionListener(ev2 -> {
                    boolean reussite = total >= seuil;
                    StringBuilder etape3 = new StringBuilder();
                    etape3.append(etape2.toString());
                    etape3.append("\nRésultat : ").append(reussite ? "Réussite !" : "Échec...");
                    texteArea.setText(etape3.toString());
                    panneauBoutons.removeAll();
                    panneauBoutons.add(btnSourdine);
                    JButton btnContinuer = new JButton("Continuer");
                    styliserBouton.accept(btnContinuer);
                    btnContinuer.addActionListener(ev3 -> {
                        int chapitreSuivant = reussite ? chapitre.getChapitreSucces() : chapitre.getChapitreEchec();
                        if (chapitreSuivant > 0) {
                            chargerChapitre(chapitreSuivant);
                        } else {
                            JOptionPane.showMessageDialog(this,
                                "Erreur : Chapitre suivant non défini",
                                "Erreur",
                                JOptionPane.ERROR_MESSAGE);
                        }
                    });
                    panneauBoutons.add(btnContinuer);
                    panneauBoutons.revalidate();
                });
                panneauBoutons.add(btnVoir);
                panneauBoutons.revalidate();
            });
            panneauBoutons.add(btnLancer);
            panneauBoutons.revalidate();
            return;
        }
        
        // Gérer les choix
        panneauBoutons.removeAll();
        panneauBoutons.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));
        
        // Ajouter le bouton de sourdine
        panneauBoutons.add(btnSourdine);
        
        if (chapitre.getChoix() != null && !chapitre.getChoix().isEmpty()) {
            for (Choix choix : chapitre.getChoix()) {
                JButton bouton = new JButton(choix.getTexte());
                styliserBouton.accept(bouton);
                bouton.addActionListener(e -> {
                    boolean voixActivee = voiceManager.isEnabled();
                    if (voixActivee) voiceManager.toggleVoice();
                    if (choix.requiresTest()) {
                        TestCompetence.TypeCompetence typeTest = choix.getTestRequis();
                        TestCompetence.Difficulte difficulte = choix.getDifficulteTest();
                        int bonus = joueur.getCompetence(typeTest);
                        TestCompetence.ResultatTest resultat = TestCompetence.faireTest(typeTest, difficulte, bonus);
                        // Afficher le résultat dans la zone de texte
                        StringBuilder texteResultat = new StringBuilder();
                        texteResultat.append(texteArea.getText());
                        texteResultat.append("\n\n--- Résultat du test de ").append(typeTest.getNom()).append(" ---\n");
                        texteResultat.append(resultat.getMessage());
                        texteArea.setText(texteResultat.toString());
                        // Remplacer les boutons par un bouton Continuer
                        panneauBoutons.removeAll();
                        panneauBoutons.add(btnSourdine);
                        JButton btnContinuer = new JButton("Continuer");
                        styliserBouton.accept(btnContinuer);
                        btnContinuer.addActionListener(ev -> {
                            boolean voixActivee2 = voiceManager.isEnabled();
                            if (voixActivee2) voiceManager.toggleVoice();
                            int chapitreSuivant = resultat.isReussite() ? choix.getChapitreSucces() : choix.getChapitreEchec();
                            if (chapitreSuivant > 0) {
                                chargerChapitre(chapitreSuivant);
                                if (voixActivee2) voiceManager.toggleVoice();
                            } else {
                                JOptionPane.showMessageDialog(this,
                                    "Erreur : Chapitre suivant non défini",
                                    "Erreur",
                                    JOptionPane.ERROR_MESSAGE);
                            }
                        });
                        panneauBoutons.add(btnContinuer);
                        panneauBoutons.revalidate();
                        panneauBoutons.repaint();
                    } else {
                        chargerChapitre(choix.getDestination());
                        if (voixActivee) voiceManager.toggleVoice();
                    }
                });
                panneauBoutons.add(bouton);
            }
        } else if (chapitre.hasNextChapter()) {
            JButton bouton = new JButton("Continuer");
            styliserBouton.accept(bouton);
            bouton.addActionListener(e -> {
                boolean voixActivee = voiceManager.isEnabled();
                if (voixActivee) voiceManager.toggleVoice();
                try {
                    int nextChapter = chapitre.getNextChapter();
                    if (nextChapter > 0) {
                        chargerChapitre(nextChapter);
                        if (voixActivee) voiceManager.toggleVoice();
                    } else {
                        JOptionPane.showMessageDialog(this,
                            "Erreur : Chapitre suivant non défini",
                            "Erreur",
                            JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this,
                        "Erreur lors de la navigation : " + ex.getMessage(),
                        "Erreur",
                        JOptionPane.ERROR_MESSAGE);
                }
            });
            panneauBoutons.add(bouton);
        } else {
            // Si pas de choix ni nextChapter, proposer de continuer vers id+1 (sauf dernier chapitre)
            int idSuivant = chapitre.getId() + 1;
            if (scenario.getChapitre(idSuivant) != null) {
                JButton bouton = new JButton("Continuer");
                styliserBouton.accept(bouton);
                bouton.addActionListener(e -> {
                    boolean voixActivee = voiceManager.isEnabled();
                    if (voixActivee) voiceManager.toggleVoice();
                    chargerChapitre(idSuivant);
                    if (voixActivee) voiceManager.toggleVoice();
                });
                panneauBoutons.add(bouton);
            } else {
                // Dernier chapitre : afficher un message de fin
                JLabel fin = new JLabel("Fin de l'aventure.");
                fin.setFont(new Font("Serif", Font.BOLD, 20));
                panneauBoutons.add(fin);
            }
        }
        
        // Lire le texte à voix haute si activé
        if (voiceManager.isEnabled()) {
            voiceManager.speak(texte.toString());
        }
        
        // Rafraîchir l'interface
        panneauBoutons.revalidate();
        panneauBoutons.repaint();
        scrollPane.revalidate();
        scrollPane.repaint();
    }

    private void sauvegarderPartie(boolean afficherMessage) {
        File saveFile = new File(DOSSIER_SAUVEGARDES + File.separator + pseudo + ".sav");
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(saveFile))) {
            SaveGame saveGame = new SaveGame(joueur, chapitre.getId(), pseudo);
            oos.writeObject(saveGame);
            
            if (afficherMessage) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                        "Partie sauvegardée avec succès !",
                        "Sauvegarde",
                        JOptionPane.INFORMATION_MESSAGE);
                });
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, 
                "Erreur lors de la sauvegarde : " + ex.getMessage(), 
                "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void chargerPartie() {
        JFileChooser fileChooser = new JFileChooser(new File(DOSSIER_SAUVEGARDES));
        fileChooser.setDialogTitle("Charger une partie");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().endsWith(".sav");
            }
            
            @Override
            public String getDescription() {
                return "Fichiers de sauvegarde (*.sav)";
            }
        });
        
        int userSelection = fileChooser.showOpenDialog(this);
        
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToLoad = fileChooser.getSelectedFile();
            try {
                SaveGame saveGame = SaveGame.loadFromFile(fileToLoad.getPath());
                
                // Mettre à jour le joueur
                for (TestCompetence.TypeCompetence type : TestCompetence.TypeCompetence.values()) {
                    joueur.setCompetence(type, saveGame.getJoueur().getCompetence(type));
                }
                joueur.modifierPointsDeVie(saveGame.getJoueur().getPointsDeVie() - joueur.getPointsDeVie());
                
                // Charger le chapitre
                int idChapitre = saveGame.getIdChapitre();
                chapitre = scenario.getChapitre(idChapitre);
                
                if (chapitre == null) {
                    JOptionPane.showMessageDialog(this,
                        "Erreur : Impossible de charger le chapitre " + idChapitre,
                        "Erreur de chargement",
                        JOptionPane.ERROR_MESSAGE);
                    chapitre = scenario.getPremierChapitre();
                }
                
                // Mettre à jour l'affichage
                SwingUtilities.invokeLater(this::afficherChapitre);
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                    "Erreur lors du chargement : " + ex.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Extrait une référence à un chapitre dans le texte
     * @param texte Le texte à analyser
     * @return L'ID du chapitre référencé, ou -1 si aucune référence n'est trouvée
     */
    private int extraireReferenceChapitre(String texte) {
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
                    return Integer.parseInt(matcher.group(1));
                } catch (NumberFormatException e) {
                    // Ignorer les erreurs de parsing
                }
            }
        }
        
        return -1; // Aucune référence trouvée
    }

    private void retournerAuMenu() {
        dispose();
        new LoginScreen().setVisible(true);
    }

    private void afficherAide() {
        String messageAide = 
            "L'Ombre de la Guillotine - Guide du Jeu\n\n" +
            "Dans ce jeu de rôle, vous incarnez un personnage dans la France révolutionnaire.\n\n" +
            "Compétences :\n" +
            "- Diplomatie : Pour négocier et convaincre\n" +
            "- Discrétion : Pour passer inaperçu\n" +
            "- Combat : Pour les affrontements\n" +
            "- Charisme : Pour influencer les autres\n" +
            "- Perception : Pour remarquer les détails\n\n" +
            "Gameplay :\n" +
            "- Lisez attentivement chaque situation\n" +
            "- Choisissez vos actions parmi les options proposées\n" +
            "- Vos choix influencent le déroulement de l'histoire\n" +
            "- La partie est sauvegardée automatiquement à chaque chapitre\n\n" +
            "Conseils :\n" +
            "- Surveillez vos points de vie\n" +
            "- Utilisez vos compétences à bon escient\n" +
            "- Chaque décision peut avoir des conséquences importantes";

        JTextArea textArea = new JTextArea(messageAide);
        textArea.setEditable(false);
        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(true);
        textArea.setFont(new Font("Serif", Font.PLAIN, 14));
        textArea.setBackground(new Color(253, 240, 213));
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 400));
        
        JOptionPane.showMessageDialog(this, scrollPane, "Aide", JOptionPane.PLAIN_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Personnage joueur = new Personnage("Héros");
            // Initialisation des compétences par défaut
            for (TestCompetence.TypeCompetence type : TestCompetence.TypeCompetence.values()) {
                joueur.setCompetence(type, 7); // Valeur moyenne
            }
            new GameUI(joueur).setVisible(true);
        });
    }
} 