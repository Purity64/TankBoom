package items;

import java.awt.Color;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class wall extends JPanel {
    int w;
    int h;
    int x; 
    int y; 
    
    public wall(JFrame jf, int w, int h, int x, int y){
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
