package utail;

import java.awt.*;
import java.awt.image.BufferedImage;

import javax.swing.JButton;

public class Button_img extends JButton {
    BufferedImage backgroundImage;
    public Button_img(BufferedImage backgroundImage){
        this.backgroundImage = backgroundImage;
    }
    @Override
    protected void paintComponent(Graphics g) {
        g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
    }
}
