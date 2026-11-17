package items;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.event.*;

import java.util.ArrayDeque;
import java.util.ArrayList;

import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import config.GameConfig;
import config.ImageConfig;
import items.bullet.ball;
import items.bullet.ballBoom;
import server.Server_cilent;
import utail.box_img;
import utail.check_hitbox;

public class tank extends box_img implements Runnable {
    int payer_size = GameConfig.payer_size;
    int bulletCount = GameConfig.MaxBullet;
    String hwid;
    String name = "";

    int max_window_y;
    int max_window_x;

    box_img jf;

    /** ทิศที่กดค้างอยู่ (ลำดับ: ปุ่มล่าสุดชนะ) */
    ArrayDeque<String> heldDirs = new ArrayDeque<>();

    ArrayList<wall> arr;

    String my_item = "";
    int shield_time = 0;
    int maxHp = GameConfig.TankHP;
    int hp = GameConfig.TankHP;

    volatile boolean run = true;
    /** ตายหรือยัง (กันตายซ้ำ / กันยิงหลังตาย) */
    volatile boolean dead = false;
    boolean isrelode = false;
    BufferedImage mainImage;
    BufferedImage shieldImage;
    String nameImage;
    JPanel bg_tranparant;
    Boolean isUserShield = false;

    String bullet = "normal";
    /** เวลา (ms) ที่ยิงนัดล่าสุด — ใช้ทำ cooldown ต่อนัด */
    long lastShotAt = 0;
    int hpReportCounter = 0;
    boolean isMe = false;

    public tank(box_img jf, BufferedImage mainImage, int x, int y, ArrayList<wall> arr, String hwid, String nameImage, JPanel bg_tranparant) {
        super(mainImage);
        this.mainImage = mainImage;
        shieldImage = ImageConfig.mapImg.get(nameImage + "_shield.png");
        this.nameImage = nameImage;
        this.bg_tranparant = bg_tranparant;
        setBounds(x, y, payer_size, payer_size);
        this.hwid = hwid;

        max_window_y = jf.getHeight();
        max_window_x = jf.getWidth();
        this.arr = arr;
        this.jf = jf;

        jf.add(this);
    }

    // ------------------------------------------------------------------
    // ข้อมูลพื้นฐาน
    // ------------------------------------------------------------------
    public String getHwid() {
        return hwid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMe(boolean me) {
        this.isMe = me;
    }

    public boolean isDead() {
        return dead;
    }

    public int getHp() {
        return hp;
    }

    // ------------------------------------------------------------------
    // การเคลื่อนที่ (กดค้างได้ — ปุ่มล่าสุดที่กดชนะ)
    // ------------------------------------------------------------------
    public void keyPressedDir(String dir) {
        synchronized (heldDirs) {
            heldDirs.remove(dir);
            heldDirs.addLast(dir);
        }
    }

    public void keyReleasedDir(String dir) {
        synchronized (heldDirs) {
            heldDirs.remove(dir);
        }
    }

    public void clearMove() {
        synchronized (heldDirs) {
            heldDirs.clear();
        }
    }

    /** หยุด loop ของ tank (ใช้ตอนเคลียร์จอตอน restart) */
    public void setRun() {
        run = false;
    }

    /** เดินได้ตรง (nx,ny) ไหม — เช็คสี่เหลี่ยม "ตำแหน่งถัดไป" ชนกำแพงไหม */
    private boolean canStandAt(int nx, int ny) {
        Rectangle me = new Rectangle(nx, ny, payer_size, payer_size);
        if (arr == null) return true;
        for (wall w : arr) {
            if (me.intersects(new Rectangle(w.getX(), w.getY(), w.getW(), w.getH()))) {
                return false;
            }
        }
        return true;
    }

    public void Move(int x, int y) {
        // ส่งตำแหน่ง + มุมหันไปด้วย เพื่อให้คนอื่นเห็น tank เราหันถูกทาง
        Server_cilent.toServer("move:" + x + "," + y + "," + (int) Math.round(Math.toDegrees(angle)));
        setLocation(x, y);
    }

    /** อัปเดตจาก network (tank ของคนอื่น) — พร้อม interpolate นิดๆ กันกระตุก */
    public void updateFromNetwork(int x, int y, double angleDeg) {
        if (isMe || dead) return;
        setAngle(Math.toRadians(angleDeg));
        setLocation(x, y);
        revalidate();
        repaint();
    }

    // ------------------------------------------------------------------
    // หันตามจุดเล็ง
    // ------------------------------------------------------------------
    public void aimAt(int px, int py) {
        double cx = getX() + payer_size / 2.0;
        double cy = getY() + payer_size / 2.0;
        // รูป tank หัน "ลง" เป็นทิศฐาน (ลำกล้องชี้ลง) → ต้องหักลบมุมฐาน 90° ออก
        // atan2 ให้มุมจากแกน x บวก แต่ sprite ฐานคือมุม +90° (หันลง)
        double a = Math.atan2(py - cy, px - cx) - Math.PI / 2;
        setAngle(a);
    }

    // ------------------------------------------------------------------
    // ไอเทม
    // ------------------------------------------------------------------
    private void useItem_hp() {
        if (hp + 30 >= maxHp) {
            hp = maxHp;
        } else {
            hp += 30;
        }
        repaint();
    }

    private void useShield() {
        if (isUserShield || !run || dead) {
            return;
        }

        setBackImage(shieldImage);
        isUserShield = true;
        Timer timerShield = new Timer();
        timerShield.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                if (!run || dead) {
                    timerShield.cancel();
                    return;
                }
                if (shield_time > 0) {
                    shield_time--;
                    if (shield_time == 0) {
                        setBackImage(mainImage);
                    }
                } else {
                    timerShield.cancel();
                    isUserShield = false;
                }
            }
        }, 0, 1000);
    }

    // ------------------------------------------------------------------
    // ดาเมจ / ตาย
    // ------------------------------------------------------------------
    /**
     * โดนดาเมจ — killerHwid คือ hwid คนยิง (ส่งให้ server ทำ kill feed)
     * การตายเป็นฝ่าย server ตัดสิน (broadcast server:killed) ทุกเครื่องเห็นพร้อมกัน
     */
    public void setHP(int damage, String killerHwid) {
        if (dead || !run) return;
        if (shield_time > 0) {
            return;
        }

        hp -= damage;
        repaint();

        if (hp <= 0) {
            hp = 0;
            // ขอให้ server ตัดสินการตาย (server จะ dedupe ให้เอง)
            Server_cilent.toServerC("server:playerkill:" + hwid + "," + (killerHwid == null ? "" : killerHwid));
            die();
        }
    }

    /** ตาย (เรียกจาก server:killed หรือจากการโดนดาเมจในเครื่องตัวเอง) — เรียกซ้ำได้ปลอดภัย */
    public void die() {
        if (dead) return;
        dead = true;
        run = false;
        clearMove();

        GameConfig.payerArrayList.remove(this);

        final tank self = this;
        final int fx = getX(), fy = getY();
        SwingUtilities.invokeLater(() -> {
            // เอฟเฟกต์ระเบิดตอนตาย (ไม่สร้างดาเมจ — owner null)
            new items.bullet.boomEfface(bg_tranparant, null, fx, fy);
            jf.remove(self);
            jf.revalidate();
            jf.repaint();
        });

        if (isMe) {
        }
    }

    // ------------------------------------------------------------------
    // ยิง (มี cooldown ต่อนัด)
    // ------------------------------------------------------------------
    public void shoot(int enpoint_x, int enpoint_y) {
        if (dead || !GameConfig.gamestatus) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now < lastShotAt + GameConfig.shootCooldownMs) {
            return; // cooldown ต่อนัด
        }
        if (bulletCount <= 0) {
            return;
        }

        lastShotAt = now;
        aimAt(enpoint_x, enpoint_y);

        if (bullet.equalsIgnoreCase("boom")) {
            new ballBoom(bg_tranparant, enpoint_x, enpoint_y, getX(), getY(), this);
            this.bullet = "";
        } else {
            new ball(bg_tranparant, enpoint_x, enpoint_y, getX(), getY(), this);
        }

        bulletCount--;
        if (bulletCount == 0) {
            isrelode = true;
            Timer timer = new Timer();
            timer.schedule(new java.util.TimerTask() {
                @Override
                public void run() {
                    bulletCount = GameConfig.MaxBullet;
                    isrelode = false;
                    timer.cancel();
                    repaint();
                }
            }, GameConfig.cooldown * 1000);
        }
        repaint();
    }

    public void shootServer(int enpoint_x, int enpoint_y) {
        Server_cilent.toServer("shoot:" + enpoint_x + "," + enpoint_y);
        shoot(enpoint_x, enpoint_y);
    }

    // ------------------------------------------------------------------
    // Game loop ของ tank แต่ละคัน
    // ------------------------------------------------------------------
    @Override
    public void run() {
        while (run) {
            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {
                return;
            }

            try {
                // ขนาดจออาจยังไม่พร้อมตอน constructor — ค่อยๆ อ่านใหม่
                if (max_window_x <= 0 || max_window_y <= 0) {
                    max_window_x = jf.getWidth();
                    max_window_y = jf.getHeight();
                }

                String dir;
                synchronized (heldDirs) {
                    dir = heldDirs.isEmpty() ? null : heldDirs.peekLast();
                }

                if (dir != null && GameConfig.gamestatus && !dead
                        && max_window_x > 0 && max_window_y > 0) {

                    int step = 5;
                    int x = getX(), y = getY();
                    int nx = x, ny = y;
                    switch (dir) {
                        case "w": ny -= step; break;
                        case "s": ny += step; break;
                        case "a": nx -= step; break;
                        case "d": nx += step; break;
                    }

                    // กันหลุดขอบจอ
                    if (nx < 0) nx = 0;
                    if (ny < 0) ny = 0;
                    if (nx + payer_size > max_window_x) nx = max_window_x - payer_size;
                    if (ny + payer_size > max_window_y) ny = max_window_y - payer_size;

                    if ((nx != x || ny != y) && canStandAt(nx, ny)) {
                        Move(nx, ny);
                    }
                }

                // ---- เก็บไอเทม (กันแย่งชิงด้วย tryTake) ----
                spawn_items items = GameConfig.item;
                if (items != null && !items.isTaken() && check_hitbox.isHit(this, items)) {
                    if (items.tryTake()) {
                        GameConfig.item = null;
                        my_item = items.getType();
                    }
                }

                if (my_item != null) {
                    if (my_item.equalsIgnoreCase("hp")) {
                        my_item = null;
                        useItem_hp();
                    } else if (my_item.equalsIgnoreCase("boom_bullet")) {
                        my_item = null;
                        this.bullet = "boom";
                    } else if (my_item.equalsIgnoreCase("shield")) {
                        my_item = null;
                        shield_time = GameConfig.shield_time;
                        useShield();
                    }
                }

                // ---- รายงาน HP ขึ้น server ทุก ~1 วิ (ใช้ตัดสินตอนหมดเวลา) ----
                if (isMe && GameConfig.gamestatus && !dead) {
                    hpReportCounter++;
                    if (hpReportCounter >= 62) {
                        hpReportCounter = 0;
                        Server_cilent.toServerC("server:myhp:" + hp);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // ------------------------------------------------------------------
    // วาด HP bar + สถานะโหลด
    // ------------------------------------------------------------------
    @Override
    public void paintComponent(Graphics g2) {
        super.paintComponent(g2);

        int barWidth = payer_size;
        int barHeight = 6;
        int hpWidth = (int) ((double) hp / maxHp * barWidth);

        g2.setColor(Color.RED);
        g2.fillRect(0, 0, barWidth, barHeight);

        g2.setColor(Color.GREEN);
        g2.fillRect(0, 0, hpWidth, barHeight);

        g2.setColor(Color.BLACK);
        g2.drawRect(0, 0, barWidth, barHeight);

        if (isrelode) {
            g2.setColor(new Color(0, 0, 0, 180));
            g2.fillRect(0, barHeight + 1, barWidth, 12);

            g2.setColor(Color.YELLOW);
            g2.setFont(g2.getFont().deriveFont(9.0f));
            g2.drawString("Reloading...", 2, barHeight + 10);
        } else if (isMe) {
            // แถบ cooldown การยิงต่อนัด
            long remain = lastShotAt + GameConfig.shootCooldownMs - System.currentTimeMillis();
            if (remain > 0) {
                int cdW = (int) ((double) remain / GameConfig.shootCooldownMs * barWidth);
                g2.setColor(new Color(255, 200, 0, 200));
                g2.fillRect(0, payer_size - 3, cdW, 3);
            }
        }
    }
}
