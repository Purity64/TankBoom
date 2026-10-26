package items.bullet;

import javax.swing.JPanel;

import config.GameConfig;
import items.tank;
import items.wall;
import utail.check_hitbox;

public class ball extends interface_bullet {

    public ball(JPanel mFrame, int enpoint_x, int enpoint_y, int start_x, int start_y , tank tankOwner ) {
        super(mFrame, enpoint_x, enpoint_y, start_x, start_y, tankOwner, "bullet.png", GameConfig.normalBullet);
    }


    @Override
    public void run() {
        while (run) {
            
            if (main_x >= mFrame.getWidth() || main_x <= 0) {
                kill();
                run = false;
                break;
            }

            for (wall a : GameConfig.wallArrayList) {
                if(check_hitbox.isHit(this , a)){
                    kill();
                    run = false;
                    break;
                }
            }
            if (tankOwner != null) {
                for(tank payer : GameConfig.payerArrayList){

                    if ( tankOwner == payer) {
                        continue;
                    }

                    if (check_hitbox.isHit(this, payer)) {
                        kill();
                        run = false;
                        payer.setHP(GameConfig.damage_normal);
                        break;
                    }
                }  
            }


 

            main_x += speed_x;
            main_y += speed_y;
            setLocation();

            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
        }
    }
}
