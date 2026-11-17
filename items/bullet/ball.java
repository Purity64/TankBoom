package items.bullet;

import javax.swing.JPanel;

import config.GameConfig;
import items.tank;
import items.wall;
import utail.check_hitbox;

public class ball extends interface_bullet {

    public ball(JPanel mFrame, int enpoint_x, int enpoint_y, int start_x, int start_y, tank tankOwner) {
        super(mFrame, enpoint_x, enpoint_y, start_x, start_y, tankOwner, "bullet.png", GameConfig.normalBullet);
        startBullet();
    }

    @Override
    public void run() {
        while (run) {
            // เดินหน้าก่อน แล้วค่อยตรวจชนที่ตำแหน่งใหม่
            main_x += speed_x;
            main_y += speed_y;

            int w = mFrame.getWidth();
            int h = mFrame.getHeight();

            // หลุดขอบจอ (ทั้งแกน x และ y) → หาย + จบ thread
            if (main_x <= -40 || main_x >= w + 40 || main_y <= -40 || main_y >= h + 40) {
                kill();
                return;
            }

            // โดนกำแพง → หายทันที (ไม่ทะลุ)
            for (wall a : GameConfig.wallArrayList) {
                if (check_hitbox.isHit(this, a)) {
                    kill();
                    return;
                }
            }

            // โดน tank คนอื่น
            if (tankOwner != null) {
                for (tank payer : GameConfig.payerArrayList) {
                    if (tankOwner == payer) {
                        continue;
                    }
                    if (check_hitbox.isHit(this, payer)) {
                        kill();
                        payer.setHP(GameConfig.damage_normal, tankOwner.getHwid());
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
