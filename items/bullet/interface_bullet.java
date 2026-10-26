package items.bullet;

import javax.swing.JPanel;

import config.ImageConfig;
import items.tank;
import utail.box_img;

abstract class interface_bullet extends box_img implements Runnable {
    protected tank tankOwner ;
    
    protected double main_x; 
    protected double main_y;
    
    protected int speedMain = 5;
    protected double speed_x = 0;
    protected double speed_y = 0;
    protected JPanel mFrame;
    protected boolean run = true;

    protected interface_bullet(JPanel mFrame, int enpoint_x, int enpoint_y, int start_x, int start_y , tank tankOwner , String img , int Size){
        super(ImageConfig.mapImg.get(img));
        this.mFrame = mFrame;
        this.tankOwner = tankOwner;
        this.main_x = start_x;
        this.main_y = start_y;

        setBounds(start_x, start_y, Size, Size);

        mFrame.add(this);
        mFrame.revalidate();
        mFrame.repaint();

        double dx = enpoint_x - start_x;
        double dy = enpoint_y - start_y;

        double distance = Math.hypot(dx, dy);
        if (distance > 0) {
            this.speed_x = (dx / distance) * speedMain;
            this.speed_y = (dy / distance) * speedMain;
        }

        new Thread(this).start();
    }
    
    protected  void setLocation() {
        setLocation((int) Math.round(main_x), (int) Math.round(main_y));
        UpdateUI();
    }

    protected  void kill(){
        mFrame.remove(this);
        UpdateUI();
    }

    protected  void UpdateUI() {
        mFrame.revalidate();
        mFrame.repaint();
    }
}
