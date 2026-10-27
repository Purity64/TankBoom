package items;

import java.awt.Color;
import javax.swing.JFrame;
import javax.swing.JPanel;

import config.ImageConfig;
import utail.box_img;

public class wall extends box_img {
    int w;
    int h;
    int x; 
    int y; 
    
    public wall(JFrame jf, int w, int h, int x, int y){
        super(ImageConfig.mapImg.get("House.png"));
        this.w = w;
        this.h = h;

        this.x = x;
        this.y = y;

        setBounds(x, y, w, h);
        setBackground(Color.RED);

        jf.add(this);
    }

    public int getW() { return w; }
    public int getH() { return h; }
    public int getX() { return x; }
    public int getY() { return y; }
}
