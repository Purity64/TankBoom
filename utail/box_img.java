package utail;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class box_img extends JPanel {
    private BufferedImage backImage;
    private ImageIcon icon;
    private int width;
    private int height;

    public box_img(BufferedImage image) {
        this.backImage = image;
        setOpaque(false); 
    }

    public box_img(ImageIcon image) {
        this.icon = image;
        setOpaque(false);
    }

    public box_img(BufferedImage image, int width, int height) {
        this(image);
        this.width = width;
        this.height = height;
    }

    public BufferedImage getBackImage() {
        return backImage;
    }

    public void setBackImage(BufferedImage backImage) {
        this.backImage = backImage;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Image imgToDraw = (backImage != null) ? backImage : (icon != null ? icon.getImage() : null);

        if (imgToDraw != null) {
            int drawWidth = (width > 0) ? width : getWidth();
            int drawHeight = (height > 0) ? height : getHeight();

            g.drawImage(imgToDraw, 0, 0, drawWidth, drawHeight, this);
        }
    }
}