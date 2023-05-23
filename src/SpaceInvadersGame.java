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

    private String playerName;
    private int score;
    private int enemySpeed;
    private int enemyLines;
    private int enemiesPerLine;
    private boolean alternateControlsEnabled;
    private long startTime;


    public SpaceInvadersGame() {
        setTitle("Space Invaders");
        setSize(BOARD_WIDTH, BOARD_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

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

        // Start button
        startButton = new JButton("Start");
        startButton.addActionListener(this);

        // Player label
        playerLabel = new JLabel();
        playerLabel.setSize(PLAYER_WIDTH, PLAYER_HEIGHT);
        playerLabel.setIcon(new ImageIcon("src/player.png"));
        playerLabel.setVisible(false);

        gamePanel.add(playerLabel);

        add(gamePanel, BorderLayout.CENTER);
        add(scorePanel, BorderLayout.NORTH);
        add(startButton, BorderLayout.SOUTH);

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

        enemySpeed = rand.nextInt(7) + 2;
        enemyLines = 3;
        enemiesPerLine = 6;
        alternateControlsEnabled = false;

        createEnemies();
        positionPlayer();

        gamePanel.requestFocus();
        gamePanel.repaint();

        enemyMovementTimer.start();
        shootTimer.start();
    }

    private void createEnemies() {
        int totalEnemies = 20;
        int initialY = -ENEMY_HEIGHT - 10;
        int enemySpacing = (BOARD_WIDTH - totalEnemies * ENEMY_WIDTH) / (totalEnemies - 1);
        Random random = new Random();

        for (int i = 0; i < totalEnemies; i++) {
            JLabel enemyLabel = new JLabel();
            enemyLabel.setSize(ENEMY_WIDTH, ENEMY_HEIGHT);
            enemyLabel.setIcon(new ImageIcon("src/enemy.png"));

            int enemyX = i * (ENEMY_WIDTH + enemySpacing);
            int enemyY = initialY - random.nextInt(200);

            enemyLabel.setLocation(enemyX, enemyY);
            gamePanel.add(enemyLabel);
            enemyLabels.add(enemyLabel);
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

        Timer bulletTimer = new Timer(20, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
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

        for (JLabel enemyLabel : enemyLabels) {
            int enemyX = enemyLabel.getX();
            int enemyY = enemyLabel.getY();

            if (enemyLabel.isVisible()) {
                if (enemyY < playerLabel.getY()) {
                    enemyY += enemySpeed;  // Poruszanie w dół
                }

                enemyLabel.setLocation(enemyX, enemyY);

                if (enemyY >= playerLabel.getY()) {
                    endGame();
                }
            }
        }
    }

    private void endGame() {
        enemyMovementTimer.stop();
        shootTimer.stop();
        JOptionPane.showMessageDialog(this, "Przegrałeś! Twój wynik: " + score);

        String playerName = JOptionPane.showInputDialog("Enter your name:");
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
        startGame();
    }


    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == startButton) {
            startGame();
        } else if (e.getSource() == enemyMovementTimer) {
            moveEnemies();
        }
    }



    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                SpaceInvadersGame game = new SpaceInvadersGame();
                game.setVisible(true);
            }
        });
    }
}
