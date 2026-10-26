package utail;

import javax.swing.JComponent;
import java.awt.Rectangle;
public  class check_hitbox {

    public static boolean isHit(JComponent a, JComponent b) {
        Rectangle boundsA = a.getBounds();
        Rectangle boundsB = b.getBounds();
        
        return boundsA.intersects(boundsB);
    }
}