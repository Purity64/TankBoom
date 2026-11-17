package items;

import java.awt.Color;
import javax.swing.JPanel;

import config.ImageConfig;
import utail.box_img;

public class wall extends box_img {
    int w;
    int h;
    int x;
    int y;

    /**
     * @param parent  container ของเกม (bg_tranparant) — add ลง panel เดียวกับ tank
     *                เพื่อให้ z-order ถูกต้อง (กำแพงไม่บังปุ่ม UI / ไม่บัง tank)
     * @param imageKey ชื่อไฟล์รูปใน /images/ (สุ่มจาก Game ให้แผนที่หลากหลาย)
     */
    public wall(JPanel parent, int w, int h, int x, int y, String imageKey) {
        super(ImageConfig.mapImg.get(imageKey));
        this.w = w;
        this.h = h;

        this.x = x;
        this.y = y;

        setBounds(x, y, w, h);
        setBackground(Color.RED);

        // กำแพงไม่ต้องรับคีย์บอร์ด/เมาส์ กันดูด event ของ panel เกม
        setEnabled(false);
        setFocusable(false);

        parent.add(this);
    }

    public int getW() { return w; }
    public int getH() { return h; }
    public int getX() { return x; }
    public int getY() { return y; }
}
