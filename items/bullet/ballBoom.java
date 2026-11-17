package items.bullet;

import javax.swing.JPanel;

import config.GameConfig;
import items.tank;
import items.wall;
import utail.check_hitbox;

public class ballBoom extends interface_bullet {

    public ballBoom(JPanel mFrame, int enpoint_x, int enpoint_y, int start_x, int start_y, tank tankOwner) {
        super(mFrame, enpoint_x, enpoint_y, start_x, start_y, tankOwner, "big_bullet.png", GameConfig.BoomBullet);
        startBullet();
    }

    @Override
    public void run() {
        while (run) {
            main_x += speed_x;
            main_y += speed_y;

            int w = mFrame.getWidth();
            int h = mFrame.getHeight();

            // หลุดขอบจอ (ทั้งแกน x และ y)
            if (main_x <= -40 || main_x >= w + 40 || main_y <= -40 || main_y >= h + 40) {
                kill();
                return;
            }

            // โดนกำแพง → ระเบิด
            for (wall a : GameConfig.wallArrayList) {
                if (check_hitbox.isHit(this, a)) {
                    kill();
                    new boomEfface(mFrame, tankOwner, (int) main_x, (int) main_y);
                    return;
                }
            }

            // โดน tank คนอื่น → ระเบิด AoE
            if (tankOwner != null) {
                for (tank payer : GameConfig.payerArrayList) {
                    if (tankOwner == payer) {
                        continue;
                    }
                    if (check_hitbox.isHit(this, payer)) {
                        kill();
                        new boomEfface(mFrame, tankOwner, (int) main_x, (int) main_y);
                        return;
                    }
                }
            }

            setLocation();

            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {
                return;
            }
        }
    }
}
