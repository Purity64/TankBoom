package utail;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.JButton;

public class Button_img extends JButton {
    private final BufferedImage backgroundImage;

    public Button_img(BufferedImage backgroundImage) {
        this.backgroundImage = backgroundImage;
        
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false); 
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
        
        super.paintComponent(g);
    }
}
