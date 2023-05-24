import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SpaceInvadersGame extends JFrame implements ActionListener {
    private static final int BOARD_WIDTH = 800;
    private static final int BOARD_HEIGHT = 600;
    private static final int PLAYER_WIDTH = 50;
    private static final int PLAYER_HEIGHT = 50;
    private static final int ENEMY_WIDTH = 30;
    private static final int ENEMY_HEIGHT = 30;

    private JPanel gamePanel;
    private JPanel scorePanel;
    private JLabel scoreLabel;
    private JButton startButton;
    private JLabel playerLabel;
    private List<JLabel> enemyLabels;
    private Timer enemyMovementTimer;
    private Timer shootTimer;

    private JPanel cards;
    private JComboBox<String> shipSelection;
    private CardLayout cardLayout;

    private JButton leftButton;
    private JButton rightButton;
    private JButton shootButton;

    private String playerName;
    private int score;
    private int enemySpeed;
    private boolean alternateControlsEnabled;
    private long startTime;

    private int difficultyLevel;


    public SpaceInvadersGame() {
        setTitle("Space Invaders");
        setSize(BOARD_WIDTH, BOARD_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JMenuBar menuBar = new JMenuBar();
        JMenu gameMenu = new JMenu("Game");
        JMenuItem newGameItem = new JMenuItem("New Game");
        JMenuItem exitItem = new JMenuItem("Exit");

        newGameItem.addActionListener(e -> {
            resetGame();
            cardLayout.show(cards, "StartScreen");
        });
        exitItem.addActionListener(e -> System.exit(0));

        gameMenu.add(newGameItem);
        gameMenu.add(exitItem);

        menuBar.add(gameMenu);

        setJMenuBar(menuBar);

        cardLayout = new CardLayout();
        cards = new JPanel(cardLayout);

        // Game panel
        gamePanel = new JPanel();
        gamePanel.setLayout(null);
        gamePanel.setBackground(Color.BLACK);
        gamePanel.setFocusable(true);
        gamePanel.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                handleKeyPress(e);
            }
        });

        // Score panel
        scorePanel = new JPanel();
        scorePanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        scoreLabel = new JLabel("Score: 0");
        scorePanel.add(scoreLabel);

        // Player label
        playerLabel = new JLabel();
        playerLabel.setSize(PLAYER_WIDTH, PLAYER_HEIGHT);
        playerLabel.setIcon(new ImageIcon("src/player.png"));
        playerLabel.setVisible(false);

        gamePanel.add(playerLabel);

        leftButton = new JButton("<");
        shootButton = new JButton("Strzal");
        rightButton = new JButton(">");


        // Zmieniamy rozmiar przycisków i przesuwamy je do widocznego obszaru.
        leftButton.setBounds(310, BOARD_HEIGHT - 150, 45, 30);
        rightButton.setBounds(435, BOARD_HEIGHT - 150, 45, 30);
        shootButton.setBounds(360, BOARD_HEIGHT - 150, 70, 30);


        rightButton.addActionListener(e -> movePlayerRight());
        shootButton.addActionListener(e -> shoot());
        leftButton.addActionListener(e -> movePlayerLeft());


        rightButton.setVisible(false);
        shootButton.setVisible(false);
        leftButton.setVisible(false);


        gamePanel.add(rightButton);
        gamePanel.add(shootButton);
        gamePanel.add(leftButton);



        // Start panel
        String[] difficulties = { "Easy", "Medium", "Hard" };
        JComboBox<String> difficultySelection = new JComboBox<>(difficulties);
        String[] controls = { "Keyboard", "Mouse" };
        JComboBox<String> controlSelection = new JComboBox<>(controls);
        BackgroundImage startPanel = new BackgroundImage("src/area.jpg");
        startPanel.setLayout(new FlowLayout());
        String[] ships = { "src/player.png", "src/player2.png", "src/player3.png" };
        shipSelection = new JComboBox<>(ships);
        startPanel.add(shipSelection);
        startPanel.add(difficultySelection);
        startPanel.add(controlSelection);

        // Dodajemy wybór sterowania
        startButton = new JButton("Start");
        startButton.addActionListener(e -> {
            String selectedShip = (String) shipSelection.getSelectedItem();
            playerLabel.setIcon(new ImageIcon(selectedShip));

            // Wybieramy rodzaj sterowania
            String selectedControl = (String) controlSelection.getSelectedItem();
            alternateControlsEnabled = "Mouse".equals(selectedControl);
            leftButton.setVisible(alternateControlsEnabled);
            rightButton.setVisible(alternateControlsEnabled);
            shootButton.setVisible(alternateControlsEnabled);

            // Ustawiamy poziom trudności
            String selectedDifficulty = (String) difficultySelection.getSelectedItem();
            if ("Easy".equals(selectedDifficulty)) {
                difficultyLevel = 1;
            } else if ("Medium".equals(selectedDifficulty)) {
                difficultyLevel = 2;
            } else if ("Hard".equals(selectedDifficulty)) {
                difficultyLevel = 3;
            }

            cardLayout.show(cards, "GameScreen");
            startGame();
        });

        startPanel.add(startButton);

        cards.add(startPanel, "StartScreen");
        cards.add(gamePanel, "GameScreen");

        add(cards);
        add(scorePanel, BorderLayout.NORTH);;

        enemyLabels = new ArrayList<>();
        enemyMovementTimer = new Timer(500, this);
        shootTimer = new Timer(1000, this);
    }


    private void startGame() {
        Random rand = new Random();
        playerName = JOptionPane.showInputDialog("Podaj swoj nickname:");
        score = 0;
        scoreLabel.setText("Wynik: " + score);
        startTime = System.currentTimeMillis();

        alternateControlsEnabled = false;

        createEnemies();
        positionPlayer();

        gamePanel.requestFocus();
        gamePanel.repaint();

        enemyMovementTimer.start();
        shootTimer.start();
    }

    private void createEnemies() {
        int totalEnemies = 10 * difficultyLevel;
        int lines = difficultyLevel; // Ilość linii wrogów
        int enemiesPerLine = totalEnemies / lines;
        int initialY = -ENEMY_HEIGHT - 10;
        int enemySpacing = (BOARD_WIDTH - enemiesPerLine * ENEMY_WIDTH) / (enemiesPerLine + 1);
        Random random = new Random();

        for (int i = 0; i < lines; i++) {
            for (int j = 0; j < enemiesPerLine; j++) {
                JLabel enemyLabel = new JLabel();
                enemyLabel.setSize(ENEMY_WIDTH, ENEMY_HEIGHT);
                enemyLabel.setIcon(new ImageIcon("src/enemy.png"));

                int enemyX = j * (ENEMY_WIDTH + enemySpacing);
                int enemyY = initialY - (i * ENEMY_HEIGHT * 2) - random.nextInt(200);

                enemyLabel.setLocation(enemyX, enemyY);
                gamePanel.add(enemyLabel);
                enemyLabels.add(enemyLabel);
            }
        }
    }



    private void positionPlayer() {
        int playerX = (BOARD_WIDTH - PLAYER_WIDTH) / 2;
        int playerY = BOARD_HEIGHT - 120;
        playerLabel.setLocation(playerX, playerY);
        playerLabel.setVisible(true);
    }

    private void handleKeyPress(KeyEvent e) {
        int keyCode = e.getKeyCode();

        if (keyCode == KeyEvent.VK_LEFT || (alternateControlsEnabled && keyCode == KeyEvent.VK_A)) {
            movePlayerLeft();
        } else if (keyCode == KeyEvent.VK_RIGHT || (alternateControlsEnabled && keyCode == KeyEvent.VK_D)) {
            movePlayerRight();
        } else if (keyCode == KeyEvent.VK_SPACE) {
            shoot();
        }
    }

    private void movePlayerLeft() {
        int playerX = playerLabel.getX();
        if (playerX > 0) {
            playerLabel.setLocation(playerX - 10, playerLabel.getY());
        }
    }

    private void movePlayerRight() {
        int playerX = playerLabel.getX();
        if (playerX < BOARD_WIDTH - PLAYER_WIDTH) {
            playerLabel.setLocation(playerX + 10, playerLabel.getY());
        }
    }

    private void shoot() {
        int playerX = playerLabel.getX();
        int playerY = playerLabel.getY();
        JLabel bulletLabel = new JLabel();
        bulletLabel.setSize(5, 10);
        bulletLabel.setBackground(Color.RED);
        bulletLabel.setOpaque(true);
        bulletLabel.setLocation(playerX + PLAYER_WIDTH / 2, playerY);
        gamePanel.add(bulletLabel);

        Timer bulletTimer = new Timer(20, e -> {
            int bulletY = bulletLabel.getY();
            bulletLabel.setLocation(bulletLabel.getX(), bulletY - 5);

            if (checkCollision(bulletLabel)) {
                ((Timer) e.getSource()).stop();
                gamePanel.remove(bulletLabel);

                // Sprawdzamy, czy wszyscy przeciwnicy zostali pokonani po strzale
                if (checkAllEnemiesDown()) {

                    long endTime = System.currentTimeMillis();
                    long gameTime = (endTime - startTime) / 1000;
                    enemyMovementTimer.stop();
                    shootTimer.stop();
                    JOptionPane.showMessageDialog(gamePanel,
                            "Gratulacje, " + playerName + "! wygrałeś! \nCzas gry: " + gameTime + " sekund");

                    // Pytanie, czy gracz chce zagrać ponownie
                    int playAgain = JOptionPane.showConfirmDialog(
                            gamePanel,
                            "Czy chcesz zagrać ponownie?",
                            "Koniec gry",
                            JOptionPane.YES_NO_OPTION);

                    if (playAgain == JOptionPane.YES_OPTION) {
                        // Resetujemy grę
                        resetGame();
                    } else {
                        // Zamykamy program
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

    private boolean checkAllEnemiesDown() {
        for (JLabel enemyLabel : enemyLabels) {
            if (enemyLabel.isVisible()) {
                return false;
            }
        }
        return true;
    }

    private boolean checkCollision(JLabel bulletLabel) {
        Rectangle bulletRect = bulletLabel.getBounds();
        for (JLabel enemyLabel : enemyLabels) {
            Rectangle enemyRect = enemyLabel.getBounds();
            if (bulletRect.intersects(enemyRect) && enemyLabel.isVisible()) {
                enemyLabel.setVisible(false);
                score++;
                scoreLabel.setText("Wynik: " + score);
                return true;
            }
        }
        return false;
    }

    private void moveEnemies() {
        int playerX = playerLabel.getX();
        boolean dropDown = false;

        // Sprawdź, czy którykolwiek wróg dotarł do granicy ekranu
        for (JLabel enemyLabel : enemyLabels) {
            int enemyX = enemyLabel.getX();
            if (enemyX <= 0 || enemyX >= BOARD_WIDTH - ENEMY_WIDTH) {
                dropDown = true;
                break;
            }
        }

        for (JLabel enemyLabel : enemyLabels) {
            int enemyX = enemyLabel.getX();
            int enemyY = enemyLabel.getY();

            if (enemyLabel.isVisible()) {
                if (dropDown) {
                    enemyY += 10 * difficultyLevel;
                    enemySpeed *= -1;
                } else {
                    enemyX += enemySpeed;
                }

                enemyLabel.setLocation(enemyX, enemyY);
            }
        }
    }

    private void endGame() {
        enemyMovementTimer.stop();
        shootTimer.stop();
        JOptionPane.showMessageDialog(this, "Przegrałeś! Twój wynik: " + score);

        String playerName = JOptionPane.showInputDialog("Podaj swój nickname:");
        // Save score to file and update top 10 list

        int choice = JOptionPane.showConfirmDialog(this, "Do you want to play again?");
        if (choice == JOptionPane.YES_OPTION) {
            resetGame();
        } else {
            System.exit(0);
        }
    }

    private void resetGame() {
        gamePanel.removeAll();
        enemyLabels.clear();
        gamePanel.add(playerLabel);
        gamePanel.add(leftButton);
        gamePanel.add(rightButton);
        gamePanel.add(shootButton);
        startGame();
    }


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
        SwingUtilities.invokeLater(() -> {
            SpaceInvadersGame game = new SpaceInvadersGame();
            game.setVisible(true);
        });
    }
}
