package jeu;

public class VoiceManager {
    private static VoiceManager instance;
    private boolean isEnabled = false; // Désactivé par défaut
    private Thread currentVoiceThread = null;
    private Process currentProcess = null;

    private VoiceManager() {}

    public static VoiceManager getInstance() {
        if (instance == null) {
            instance = new VoiceManager();
        }
        return instance;
    }

    public synchronized void speak(String text) {
        if (!isEnabled) return;
        
        // Arrêter la lecture précédente si elle existe
        if (currentVoiceThread != null && currentVoiceThread.isAlive()) {
            currentVoiceThread.interrupt();
            try {
                currentVoiceThread.join(500); // attendre l'arrêt du thread précédent (max 0,5s)
            } catch (InterruptedException e) {
                // Ignorer
            }
        }
        
        // Tuer le processus PowerShell précédent si besoin
        if (currentProcess != null && currentProcess.isAlive()) {
            currentProcess.destroyForcibly();
            try {
                currentProcess.waitFor(500, java.util.concurrent.TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                // Ignorer
            }
        }
        
        // Créer un nouveau thread pour la synthèse vocale
        currentVoiceThread = new Thread(() -> {
            try {
                // Nettoyer le texte : retirer les stats et limiter la longueur
                String texteCourt = text.replaceAll("--- État du héros ---.*", "")
                                      .replaceAll("\n+", ". ")
                                      .replaceAll("[\r\n]+", " ")
                                      .trim();
                if (texteCourt.length() > 400) {
                    texteCourt = texteCourt.substring(0, 400) + "...";
                }
                
                // Échapper les caractères spéciaux pour PowerShell
                String escapedText = texteCourt.replace("'", "''").replace("$", "`$");
                String command = "powershell.exe -Command \"" +
                               "[System.Reflection.Assembly]::LoadWithPartialName('System.Speech'); " +
                               "$speak = New-Object System.Speech.Synthesis.SpeechSynthesizer; " +
                               "$speak.Rate = 0; " +
                               "$speak.Volume = 100; " +
                               "$speak.Speak('" + escapedText + "')\"";
                
                ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", command);
                processBuilder.redirectErrorStream(true);
                currentProcess = processBuilder.start();
                
                int exitCode = currentProcess.waitFor();
                if (exitCode != 0) {
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        javax.swing.JOptionPane.showMessageDialog(null, 
                            "Erreur lors de la synthèse vocale (code : " + exitCode + ")", 
                            "Synthèse vocale", 
                            javax.swing.JOptionPane.ERROR_MESSAGE);
                    });
                }
            } catch (InterruptedException e) {
                if (currentProcess != null && currentProcess.isAlive()) {
                    currentProcess.destroyForcibly();
                }
                return;
            } catch (Exception e) {
                javax.swing.SwingUtilities.invokeLater(() -> {
                    javax.swing.JOptionPane.showMessageDialog(null, 
                        "Erreur lors de la synthèse vocale : " + e.getMessage(), 
                        "Synthèse vocale", 
                        javax.swing.JOptionPane.ERROR_MESSAGE);
                });
                e.printStackTrace();
            }
        });
        
        // Démarrer le thread
        currentVoiceThread.start();
    }

    public void toggleVoice() {
        isEnabled = !isEnabled;
        // Si on désactive la voix, arrêter la lecture en cours
        if (!isEnabled) {
            if (currentVoiceThread != null && currentVoiceThread.isAlive()) {
                currentVoiceThread.interrupt();
            }
            if (currentProcess != null && currentProcess.isAlive()) {
                currentProcess.destroyForcibly();
            }
        }
    }

    public boolean isEnabled() {
        return isEnabled;
    }
} 