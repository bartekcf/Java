import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

class BackgroundImage extends JPanel {
    private BufferedImage image;

    public BackgroundImage(String imagepath) {
        try {
            image = ImageIO.read(new File(imagepath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        setOpaque(false);
        setLayout(new GridBagLayout());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, this.getWidth(), this.getHeight(), this);
    }
}