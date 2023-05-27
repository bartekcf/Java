import javax.swing.*;

public class Player {

    public static final int PLAYER_WIDTH = 50;
    public static final int PLAYER_HEIGHT = 50;
    private JLabel playerLabel;
    private JPanel gamePanel;
    private int playerX;
    private int playerY;

    public Player(JLabel playerLabel, JPanel gamePanel) {
        this.playerLabel = playerLabel;
        this.gamePanel = gamePanel;
    }

    public void positionPlayer() {
        int playerX = (AreaIntruders.BOARD_WIDTH - Player.PLAYER_WIDTH) / 2;
        int playerY = AreaIntruders.BOARD_HEIGHT - 120;
        playerLabel.setLocation(playerX, playerY);
        playerLabel.setVisible(true);
    }

    public void movePlayerLeft() {
        int playerX = playerLabel.getX();
        if (playerX > 0) {
            playerLabel.setLocation(playerX - 10, playerLabel.getY());
        }
    }

    public void movePlayerRight() {
        int playerX = playerLabel.getX();
        if (playerX < AreaIntruders.BOARD_WIDTH - Player.PLAYER_WIDTH) {
            playerLabel.setLocation(playerX + 10, playerLabel.getY());
        }
    }

}
