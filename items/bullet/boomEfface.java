package items.bullet;

import javax.swing.JPanel;

import config.GameConfig;
import config.ImageConfig;
import items.tank;
import utail.box_img;
import utail.check_hitbox;

public class boomEfface extends box_img implements Runnable {
    private JPanel mainFame;
    private tank tankOwner;
    private static final int BOOM_SIZE = 50; 

    public boomEfface(JPanel mainFame, tank tankOwner , int x , int y) {
        super(ImageConfig.mapGif.get("boom.gif"));
        this.mainFame = mainFame;
        this.tankOwner = tankOwner;
        int centerX = x + (GameConfig.BoomBullet / 2);
        int centerY = y + (GameConfig.BoomBullet / 2);

        int boomX = centerX - (BOOM_SIZE / 2);
        int boomY = centerY - (BOOM_SIZE / 2);

        setBounds(boomX, boomY, BOOM_SIZE, BOOM_SIZE);
        
        mainFame.add(this);
        mainFame.setComponentZOrder(this, 0);
        mainFame.revalidate();
        mainFame.repaint();
        
        new Thread(this).start();

    }

    @Override
    public void run() {
        while (true) {
            for(tank payer : GameConfig.payerArrayList){
                if (tankOwner == payer || payer == null) {
                    continue;
                }


                if (check_hitbox.isHit(this, payer)) {
                    payer.setHP(GameConfig.damage_boom);
                }
            }
            try {
                Thread.sleep(1000);
                mainFame.remove(this);
                mainFame.revalidate();
                mainFame.repaint();
                break;
            } catch (InterruptedException e) {
            }
        }
    }
}