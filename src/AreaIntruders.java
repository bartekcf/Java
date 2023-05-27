import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

public class AreaIntruders extends JFrame implements ActionListener {
    public static final int BOARD_WIDTH = 800;
    public static final int BOARD_HEIGHT = 600;
    public static final int ENEMY_WIDTH = 30;
    public static final int ENEMY_HEIGHT = 30;
    private static final String RULES_FILE = "src/files/zasady.txt";
    public static final String SCORE_FILE = "src/files/lista_najlepszych_graczy.txt";

    public JPanel gamePanel;
    public JPanel scorePanel;
    public JLabel scoreLabel;
    public JLabel playerLabel;
    public JPanel cards;
    public JComboBox<String> shipSelection;
    public CardLayout cardLayout;

    public List<PlayerScore> topScores;
    public List<JLabel> enemyLabels;

    public JButton startButton;
    public JButton leftButton;
    public JButton rightButton;
    public JButton shootButton;

    public Timer enemyMovementTimer;
    public Timer shootTimer;

    public String playerName;
    public int score;
    public boolean alternateControlsEnabled;
    public long startTime;
    public int difficultyLevel;
    public Player player;
    public boolean gamePaused = false;
    private String gameRules = "";
    private String scoreFile = "";


    public AreaIntruders() {
        setTitle("Area Intruders");
        setSize(BOARD_WIDTH, BOARD_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ImageIcon game_icon = new ImageIcon("src/images/game_icon.jpg");
        this.setIconImage(game_icon.getImage());


        try {
            gameRules = new String(Files.readAllBytes(Paths.get(RULES_FILE)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            scoreFile = new String(Files.readAllBytes(Paths.get(SCORE_FILE)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        //_________Tworzenie Menu________________//
        JMenuBar menuBar = new JMenuBar();
        JMenu gameMenu = new JMenu("Menu");
        JMenuItem rulesItem = new JMenuItem("Zasady");
        JMenuItem pauseItem = new JMenuItem("Pauza");
        JMenuItem newGameItem = new JMenuItem("Nowa gra");
        JMenuItem topScorers = new JMenuItem("Najlepsi gracze");
        JMenuItem exitItem = new JMenuItem("Wyjdz");


        gameMenu.add(rulesItem);
        gameMenu.add(pauseItem);
        gameMenu.add(newGameItem);
        gameMenu.add(topScorers);
        gameMenu.add(exitItem);

        menuBar.add(gameMenu);
        setJMenuBar(menuBar);


        rulesItem.addActionListener(e -> JOptionPane.showMessageDialog(this, gameRules, "Zasady gry", JOptionPane.INFORMATION_MESSAGE));
        topScorers.addActionListener(e -> JOptionPane.showMessageDialog(this, scoreFile, "TOP 10 GRACZY", JOptionPane.INFORMATION_MESSAGE));

        pauseItem.addActionListener(e -> {
            gamePaused = !gamePaused;

            if (gamePaused) {
                enemyMovementTimer.stop();
                shootTimer.stop();
            } else {
                enemyMovementTimer.start();
                shootTimer.start();
            }
        });

        newGameItem.addActionListener(e -> {
            resetGame();
            cardLayout.show(cards, "StartScreen");
        });
        exitItem.addActionListener(e -> System.exit(0));


        topScores = new ArrayList<>();
        cardLayout = new CardLayout();
        cards = new JPanel(cardLayout);

        gamePanel = new JPanel();
        gamePanel.setLayout(null);
        gamePanel.setBackground(Color.BLACK);
        gamePanel.setFocusable(true);
        gamePanel.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                handleKeyPress(e);
            }
        });

        scorePanel = new JPanel();
        scorePanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        scoreLabel = new JLabel("Wynik: 0");
        scorePanel.add(scoreLabel);

        playerLabel = new JLabel();
        playerLabel.setSize(Player.PLAYER_WIDTH, Player.PLAYER_HEIGHT);
        playerLabel.setVisible(false);
        this.player = new Player(playerLabel, gamePanel);
        gamePanel.add(playerLabel);



        // Sterowanie przyciskami
        leftButton = new JButton("<");
        shootButton = new JButton("Strzal");
        rightButton = new JButton(">");

        leftButton.setBounds(310, BOARD_HEIGHT - 150, 45, 30);
        rightButton.setBounds(435, BOARD_HEIGHT - 150, 45, 30);
        shootButton.setBounds(360, BOARD_HEIGHT - 150, 70, 30);

        rightButton.addActionListener(e -> player.movePlayerRight());
        shootButton.addActionListener(e -> shoot());
        leftButton.addActionListener(e -> player.movePlayerLeft());

        rightButton.setVisible(false);
        shootButton.setVisible(false);
        leftButton.setVisible(false);

        gamePanel.add(rightButton);
        gamePanel.add(shootButton);
        gamePanel.add(leftButton);


        String[] difficulties = {"Latwy", "Sredni", "Trudny"};
        String[] controls = {"Klawiatura", "Mysz"};
        String[] ships = {"src/images/rakieta.png", "src/images/statek.png", "src/images/niespodzianka.png"};
        JComboBox<String> difficultySelection = new JComboBox<>(difficulties);
        JComboBox<String> controlSelection = new JComboBox<>(controls);
        shipSelection = new JComboBox<>(ships);

        BackgroundImage startPanel = new BackgroundImage("src/images/area.jpg");
        startPanel.setLayout(new FlowLayout());
        startPanel.add(shipSelection);
        startPanel.add(difficultySelection);
        startPanel.add(controlSelection);


        //wybrane ustawienia gracza
        startButton = new JButton("Start");
        startButton.addActionListener(e -> {
            String selectedShip = (String) shipSelection.getSelectedItem();
            playerLabel.setIcon(new ImageIcon(selectedShip));

            String selectedControl = (String) controlSelection.getSelectedItem();
            alternateControlsEnabled = "Mysz".equals(selectedControl);
            leftButton.setVisible(alternateControlsEnabled);
            rightButton.setVisible(alternateControlsEnabled);
            shootButton.setVisible(alternateControlsEnabled);

            String selectedDifficulty = (String) difficultySelection.getSelectedItem();
            if ("Latwy".equals(selectedDifficulty)) {
                difficultyLevel = 1;
            } else if ("Sredni".equals(selectedDifficulty)) {
                difficultyLevel = 2;
            } else if ("Trudny".equals(selectedDifficulty)) {
                difficultyLevel = 3;
            }

            playerName = JOptionPane.showInputDialog("Podaj swoj nickname:");

            cardLayout.show(cards, "GameScreen");
            loadScores();
            startGame();
        });

        startPanel.add(startButton);

        cards.add(startPanel, "StartScreen");
        cards.add(gamePanel, "GameScreen");

        add(cards);
        add(scorePanel, BorderLayout.NORTH);


        enemyLabels = new ArrayList<>();
        enemyMovementTimer = new Timer(500, this);
        shootTimer = new Timer(1000, this);
        loadBackgroundMusic();
    }


    // __________________________________ OBSŁUGA STEROWANIA ____________________________________________________//
    public void handleKeyPress(KeyEvent e) {
        int keyCode = e.getKeyCode();

        if (keyCode == KeyEvent.VK_LEFT || (alternateControlsEnabled && keyCode == KeyEvent.VK_A)) {
            player.movePlayerLeft();
        } else if (keyCode == KeyEvent.VK_RIGHT || (alternateControlsEnabled && keyCode == KeyEvent.VK_D)) {
            player.movePlayerRight();
        } else if (keyCode == KeyEvent.VK_SPACE) {
            shoot();
        }
    }

    public void shoot() {
        int playerX = playerLabel.getX();
        int playerY = playerLabel.getY();
        JLabel bulletLabel = new JLabel();
        bulletLabel.setSize(5, 10);
        bulletLabel.setBackground(Color.GREEN);
        bulletLabel.setOpaque(true);
        bulletLabel.setLocation(playerX + Player.PLAYER_WIDTH / 2, playerY);
        gamePanel.add(bulletLabel);

        Timer bulletTimer = new Timer(20, e -> {
            int bulletY = bulletLabel.getY();
            bulletLabel.setLocation(bulletLabel.getX(), bulletY - 5);

            if (checkCollision(bulletLabel)) {
                // e.getSource() zwraca obiekt, który wywołał zdarzenie ActionListenera.
                ((Timer) e.getSource()).stop();
                gamePanel.remove(bulletLabel);

                if (checkAllEnemiesDown()) {
                    PlayerScore playerScore = new PlayerScore(playerName, score);
                    addToTopScores(playerScore);
                    long endTime = System.currentTimeMillis();
                    long gameTime = (endTime - startTime) / 1000;
                    enemyMovementTimer.stop();
                    shootTimer.stop();
                    JOptionPane.showMessageDialog(gamePanel, "Gratulacje, " + playerName + "! wygrałeś! \nCzas gry: " + gameTime + " sekund");


                    int playAgain = JOptionPane.showConfirmDialog(
                            gamePanel,
                            "Czy chcesz zagrać ponownie?",
                            "Koniec gry",
                            JOptionPane.YES_NO_OPTION);

                    if (playAgain == JOptionPane.YES_OPTION) {
                        resetGame();
                    } else {
                        System.exit(0);
                    }
                }
            }

            if (bulletY < 0) {
                ((Timer) e.getSource()).stop();
                gamePanel.remove(bulletLabel);
            }
        });

        bulletTimer.start();
    }


    // __________________________________ PRZECIWNICY ____________________________________________________//

    public void createEnemies() {
        int totalEnemies = 5 * (difficultyLevel*2);
        int lines = difficultyLevel; // Ilość linii wrogów
        int enemiesPerLine = totalEnemies / lines;
        int initialY = -ENEMY_HEIGHT - 10;
        int enemySpacing = (BOARD_WIDTH - enemiesPerLine * ENEMY_WIDTH) / (enemiesPerLine + 1);
        Random random = new Random();

        for (int i = 0; i < lines; i++) {
            for (int j = 0; j < enemiesPerLine; j++) {
                JLabel enemyLabel = new JLabel();
                enemyLabel.setSize(ENEMY_WIDTH, ENEMY_HEIGHT);
                enemyLabel.setIcon(new ImageIcon("src/images/enemy.png"));

                int enemyX = j * (ENEMY_WIDTH + enemySpacing);
                int enemyY = initialY - (i * ENEMY_HEIGHT * 2) - random.nextInt(200);

                enemyLabel.setLocation(enemyX, enemyY);
                gamePanel.add(enemyLabel);
                enemyLabels.add(enemyLabel);
            }
        }
    }

    public void moveEnemies() {
        boolean dropDown = false;

        for (JLabel enemyLabel : enemyLabels) {
            int enemyX = enemyLabel.getX();
            int enemyY = enemyLabel.getY();

            if (enemyX <= 0 || enemyX >= BOARD_WIDTH - ENEMY_WIDTH || enemyY >= BOARD_HEIGHT - Player.PLAYER_HEIGHT) {
                dropDown = true;
                break;
            }
        }

        for (JLabel enemyLabel : enemyLabels) {
            int enemyX = enemyLabel.getX();
            int enemyY = enemyLabel.getY();

            if (enemyLabel.isVisible()) {
                if (dropDown) {
                    enemyY += 5 * difficultyLevel;
                }
                enemyLabel.setLocation(enemyX, enemyY);
            }
        }

        for (JLabel enemyLabel : enemyLabels) {
            int enemyY = enemyLabel.getY();
            if (enemyY >= BOARD_HEIGHT - Player.PLAYER_HEIGHT) {
                endGame();
                return;
            }
        }
    }


    public boolean checkAllEnemiesDown() {
        for (JLabel enemyLabel : enemyLabels) {
            if (enemyLabel.isVisible()) {
                return false;
            }
        }
        return true;
    }

    public boolean checkCollision(JLabel bulletLabel) {
        Rectangle bulletRect = bulletLabel.getBounds();
        for (JLabel enemyLabel : enemyLabels) {
            Rectangle enemyRect = enemyLabel.getBounds();
            //metoda intersects() sprawdza, czy "2 kwadraty się przecięły to znaczy, czy pocisk trafił we wroga.
            if (bulletRect.intersects(enemyRect) && enemyLabel.isVisible()) {
                enemyLabel.setVisible(false);
                score++;
                scoreLabel.setText("Wynik: " + score);
                return true;
            }
        }
        return false;
    }


    // __________________________________ OBSŁUGA GRY ____________________________________________________//
    public void startGame() {
        score = 0;
        scoreLabel.setText("Wynik: " + score);
        startTime = System.currentTimeMillis();

        createEnemies();
        player.positionPlayer();

        gamePanel.requestFocus();
        gamePanel.repaint();

        enemyMovementTimer.start();
        shootTimer.start();
    }


    public void endGame() {
        enemyMovementTimer.stop();
        shootTimer.stop();
        JOptionPane.showMessageDialog(this, "Przegrałeś! Twój wynik: " + score);
        PlayerScore playerScore = new PlayerScore(playerName, score);
        addToTopScores(playerScore);

        int playAgain = JOptionPane.showConfirmDialog(this, "Czy chcesz zagrać ponownie?");
        if (playAgain == JOptionPane.YES_OPTION) {
            resetGame();
        } else {
            System.exit(0);
        }
    }


    public void resetGame() {
        gamePanel.removeAll();
        enemyLabels.clear();
        gamePanel.add(playerLabel);
        gamePanel.add(leftButton);
        gamePanel.add(rightButton);
        gamePanel.add(shootButton);
        startGame();
    }


    // __________________________________ LISTA TOP 10 ____________________________________________________//

    public void loadScores() {
        try {
            List<String> lines = Files.readAllLines(Paths.get(SCORE_FILE));
            for (String line : lines) {
                if (!Objects.equals(line, "")) {
                    String[] players_score = line.split(" - ");
                    String name = players_score[0];
                    int score = Integer.parseInt(players_score[1]);
                    topScores.add(new PlayerScore(name, score));
                }
            }
        } catch (IOException e) {
            System.err.println("Problem z wczytaniem pliku wyników");
        }
    }

    public void saveScores() {
        try (FileWriter writer = new FileWriter(SCORE_FILE)) {
            for (PlayerScore score : topScores) {
                writer.write(score.getName() + " - " + score.getScore() + "\n");
            }
        } catch (IOException e) {
            System.err.println("Problem z zapisem pliku wyników");
        }
    }

    public void addToTopScores(PlayerScore score) {
        topScores.add(score);
        Collections.sort(topScores);
        if (topScores.size() > 10) {
            topScores = topScores.subList(0, 10);
        }
        saveScores();
    }

    //dodanie muzyczki do gry
    private void loadBackgroundMusic() {
        try {
            File audioFile = new File("src/music/muzyczka.wav");
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);

            Clip backgroundMusic = AudioSystem.getClip();
            backgroundMusic.open(audioStream);
            backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (IOException | UnsupportedAudioFileException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    // URUCHOMIENIE GRY

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == startButton) {
            String selectedShip = (String) shipSelection.getSelectedItem();
            playerLabel.setIcon(new ImageIcon(selectedShip));
            cardLayout.show(cards, "GameScreen");
            startGame();
        } else if (e.getSource() == enemyMovementTimer) {
            moveEnemies();
        }
    }

    public static void main(String[] args) {
        //invokeLater sprawdza, czy interakcja użytkownika z GUI obywa się na tzw. wątku dystrybucji zdarzeń (Event Dispatch Thread, EDT).
        SwingUtilities.invokeLater(() -> {
            AreaIntruders game = new AreaIntruders();
            game.setVisible(true);
        });
    }
}
