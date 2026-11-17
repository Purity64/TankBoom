package utail;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class box_img extends JPanel {
    private BufferedImage backImage;
    private ImageIcon icon;
    private int width;
    private int height;

    /** มุมหมุนของ sprite (เรเดียน) 0 = วาดตามปกติ */
    protected volatile double angle = 0;

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
        repaint();
    }

    public void setAngle(double rad) {
        angle = rad;
        repaint();
    }

    public double getAngle() {
        return angle;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Image imgToDraw = (backImage != null) ? backImage : (icon != null ? icon.getImage() : null);

        if (imgToDraw != null) {
            int drawWidth = (width > 0) ? width : getWidth();
            int drawHeight = (height > 0) ? height : getHeight();

            if (angle != 0) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.rotate(angle, getWidth() / 2.0, getHeight() / 2.0);
                g2.drawImage(imgToDraw, 0, 0, drawWidth, drawHeight, this);
                g2.dispose();
            } else {
                g.drawImage(imgToDraw, 0, 0, drawWidth, drawHeight, this);
            }
        }
    }
}
