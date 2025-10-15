package jeu;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

public class LoginScreen extends JFrame {
    private static final String DOSSIER_SAUVEGARDES = "sauvegardes";
    private final Color bleuFrance = new Color(0, 85, 164);
    private final Color rougeFrance = new Color(239, 65, 53);
    private final Color blancFrance = new Color(255, 255, 255);
    private final Color fondParchemin = new Color(253, 240, 213);

    public LoginScreen() {
        super("L'Ombre de la Guillotine");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        // Créer le dossier de sauvegardes s'il n'existe pas
        File dossierSauvegardes = new File(DOSSIER_SAUVEGARDES);
        if (!dossierSauvegardes.exists()) {
            dossierSauvegardes.mkdir();
        }

        // Panel principal avec fond parchemin
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(fondParchemin);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        // Titre
        JLabel titre = new JLabel("L'Ombre de la Guillotine", SwingConstants.CENTER);
        titre.setFont(new Font("Serif", Font.BOLD, 36));
        titre.setForeground(rougeFrance);
        mainPanel.add(titre, BorderLayout.NORTH);

        // Panel central pour les boutons
        JPanel buttonPanel = new JPanel(new GridLayout(4, 1, 20, 20));
        buttonPanel.setOpaque(false);

        // Bouton Nouvelle Partie
        JButton btnNouvellePartie = createStyledButton("Nouvelle Partie");
        btnNouvellePartie.addActionListener(e -> nouvellePartie());

        // Bouton Charger Partie
        JButton btnChargerPartie = createStyledButton("Charger une Partie");
        btnChargerPartie.addActionListener(e -> chargerPartie());

        // Bouton Aide
        JButton btnAide = createStyledButton("Aide");
        btnAide.addActionListener(e -> afficherAide());

        // Bouton Quitter
        JButton btnQuitter = createStyledButton("Quitter");
        btnQuitter.addActionListener(e -> System.exit(0));

        buttonPanel.add(btnNouvellePartie);
        buttonPanel.add(btnChargerPartie);
        buttonPanel.add(btnAide);
        buttonPanel.add(btnQuitter);

        mainPanel.add(buttonPanel, BorderLayout.CENTER);
        setContentPane(mainPanel);
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Serif", Font.BOLD, 20));
        button.setPreferredSize(new Dimension(250, 60));
        button.setBackground(blancFrance);
        button.setForeground(bleuFrance);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(rougeFrance, 3),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        button.setFocusPainted(false);
        return button;
    }

    private void nouvellePartie() {
        // Demander le pseudo
        String pseudo = JOptionPane.showInputDialog(this,
            "Entrez votre pseudo :",
            "Nouveau personnage",
            JOptionPane.QUESTION_MESSAGE);
        
        if (pseudo != null && !pseudo.trim().isEmpty()) {
            // Créer un nouveau personnage
            new CreationPersonnage(pseudo).setVisible(true);
            
            // Fermer l'écran de login
            this.dispose();
        }
    }

    private void chargerPartie() {
        JFileChooser fileChooser = new JFileChooser("sauvegardes");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".sav");
            }
            public String getDescription() {
                return "Fichiers de sauvegarde (*.sav)";
            }
        });
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                // Charger la sauvegarde
                SaveGame saveGame = SaveGame.loadFromFile(fileChooser.getSelectedFile().getPath());
                
                // Vérifier que le pseudo correspond au nom du fichier
                String pseudoFichier = fileChooser.getSelectedFile().getName().replace(".sav", "");
                if (!saveGame.getPseudo().equals(pseudoFichier)) {
                    throw new SecurityException("Le fichier de sauvegarde ne correspond pas au joueur sélectionné");
                }
                
                // Créer l'interface de jeu avec les données chargées
                GameUI gameUI = new GameUI(saveGame.getJoueur(), saveGame.getPseudo());
                gameUI.chargerChapitre(saveGame.getIdChapitre());
                
                // Fermer la fenêtre de connexion
                this.dispose();
                
                // Afficher l'interface de jeu
                gameUI.setVisible(true);
                
            } catch (SecurityException e) {
                JOptionPane.showMessageDialog(this,
                    "Erreur de sécurité : " + e.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Erreur lors du chargement de la partie : " + e.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
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
        textArea.setBackground(fondParchemin);
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 400));
        
        JOptionPane.showMessageDialog(this, scrollPane, "Aide", JOptionPane.PLAIN_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginScreen loginScreen = new LoginScreen();
            loginScreen.setVisible(true);
        });
    }
} 