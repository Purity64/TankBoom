package items;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;

import config.GameConfig;
import config.ImageConfig;
import utail.box_img;

/**
 * spawn_items — กล่องไอเทมที่ server สั่ง spawn
 * หมดเวลาแล้วหายจากจอจริงๆ และแย่งชิงกันได้อย่างปลอดภัย (tryTake)
 */
public class spawn_items extends box_img implements Runnable {
    private int time = 10;
    private volatile boolean taken = false;
    private String type;

    private box_img mainFrame;

    public spawn_items(box_img mainFrame, int x, int y, String type) {
        super(ImageConfig.mapImg.get("box.png"));
        this.type = type;
        this.mainFrame = mainFrame;
        setBounds(x, y, GameConfig.item_size, GameConfig.item_size);

        mainFrame.add(this);
        UpdateUI();
    }

    public String getType() {
        return type;
    }

    public boolean isTaken() {
        return taken;
    }

    /** เก็บสำเร็จหรือไม่ — คันแรกเท่านั้นที่ได้ (กัน 2 คันเก็บพร้อมกัน) */
    public synchronized boolean tryTake() {
        if (taken) return false;
        taken = true;
        removeFromUI();
        return true;
    }

    /** หายไป (ถูกเก็บ / หมดเวลา / server spawn อันใหม่มาแทน) */
    public void Kill() {
        taken = true;
        removeFromUI();
    }

    private void removeFromUI() {
        mainFrame.remove(this);
        UpdateUI();
    }

    public void UpdateUI() {
        mainFrame.repaint();
        mainFrame.revalidate();
    }

    @Override
    public void run() {
        // นับถอยหลัง — หมดเวลาแล้วหายจากจอ
        try {
            while (!taken && time > 0) {
                Thread.sleep(1000);
                time--;
                repaint();
            }
            if (!taken) {
                Kill();
            }
        } catch (InterruptedException e) {
            Kill();
        }
    }

    /** วาดสัญลักษณ์บนกล่องให้รู้ว่าเป็นไอเทมอะไร */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        String label;
        Color color;
        switch (type) {
            case "hp":
                label = "+";
                color = new Color(46, 204, 113);
                break;
            case "boom_bullet":
                label = "B";
                color = new Color(230, 126, 34);
                break;
            case "shield":
                label = "S";
                color = new Color(52, 152, 219);
                break;
            default:
                label = "?";
                color = Color.GRAY;
        }

        Graphics2D g2 = (Graphics2D) g;
        int s = getWidth();
        g2.setColor(new Color(0, 0, 0, 120));
        g2.fillOval(6, 6, s - 12, s - 12);
        g2.setColor(color);
        g2.fillOval(9, 9, s - 18, s - 18);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 22));
        java.awt.FontMetrics fm = g2.getFontMetrics();
        int tx = (s - fm.stringWidth(label)) / 2;
        int ty = (s - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(label, tx, ty);
    }
}
