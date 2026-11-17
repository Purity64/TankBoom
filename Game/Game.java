package Game;
import javax.swing.*;

import config.GameConfig;
import config.ImageConfig;
import items.spawn_items;
import items.tank;
import items.wall;
import server.Server_cilent;
import utail.Button_img;
import utail.box_img;

import java.awt.*;
import java.awt.event.*;

import java.util.ArrayList;
import java.util.List;


public class Game extends JFrame {
    ArrayList<wall> arr = GameConfig.wallArrayList;
    List<tank> payerArr = GameConfig.payerArrayList;
    box_img bg_tranparant;
    int mainSizeW = 1200;
    int mainSizeH = 700;
    tank me;

    // องค์ประกอบ overlay
    Button_img reStartBtn;
    JPanel endOverlay;
    JLabel countdownLabel;
    JLabel deathLabel;
    JPanel killFeedPanel;
    javax.swing.Timer countdownTimer;

    public Game() {
        setTitle("TankBoom");
        setSize(mainSizeW, mainSizeH);
        setLayout(null);
        ImageConfig img = new ImageConfig();
        img.lode_image();
        bg_tranparant = new box_img(ImageConfig.mapImg.get("Background.png"));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        bg_tranparant.setLayout(null);
        bg_tranparant.setBounds(0, 0, mainSizeW, mainSizeH);
        reStartBtn = new Button_img(ImageConfig.mapImg.get("Restart_btn.png"));

        // ---- แผนที่: บ้าน/กำแพง 9 ก้อน ไม่ซ้อนกัน มุมทั้ง 4 มุมว่างไว้ให้เกิด ----
        arr.clear();
        wall w1 = new wall(bg_tranparant, 70, 70, 260, 130, "House.png");
        wall w2 = new wall(bg_tranparant, 90, 70, 560, 90, "House.png");
        wall w3 = new wall(bg_tranparant, 70, 70, 880, 130, "House.png");
        wall w4 = new wall(bg_tranparant, 70, 70, 120, 320, "House.png");
        wall w5 = new wall(bg_tranparant, 90, 90, 555, 310, "House.png");
        wall w6 = new wall(bg_tranparant, 70, 70, 1010, 320, "House.png");
        wall w7 = new wall(bg_tranparant, 70, 70, 260, 500, "House.png");
        wall w8 = new wall(bg_tranparant, 90, 70, 560, 540, "House.png");
        wall w9 = new wall(bg_tranparant, 70, 70, 880, 500, "House.png");

        arr.add(w1); arr.add(w2); arr.add(w3);
        arr.add(w4); arr.add(w5); arr.add(w6);
        arr.add(w7); arr.add(w8); arr.add(w9);

        // ---- คลิก = ยิง ----
        bg_tranparant.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (me != null && !me.isDead() && GameConfig.gamestatus) {
                    me.shootServer(e.getX(), e.getY());
                }
            }
        });

        // ---- เล็ง: หัน tank ตามเมาส์ ----
        bg_tranparant.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (me != null && !me.isDead()) {
                    me.aimAt(e.getX(), e.getY());
                }
            }
        });

        // ---- แผง kill feed มุมขวาบน ----
        killFeedPanel = new JPanel();
        killFeedPanel.setLayout(new BoxLayout(killFeedPanel, BoxLayout.Y_AXIS));
        killFeedPanel.setOpaque(false);
        killFeedPanel.setBounds(mainSizeW - 320, 10, 310, 160);
        bg_tranparant.add(killFeedPanel);

        add(bg_tranparant);
        sendBackgroundToBack();
    }

    // ------------------------------------------------------------------
    // ไอเทม
    // ------------------------------------------------------------------
    public void spownItem(int x, int y, String type) {
        synchronized (GameConfig.class) {
            // อันเก่า (ถ้ามี) หายไปก่อน กัน object ค้างบนจอ
            spawn_items old = GameConfig.item;
            if (old != null) old.Kill();

            spawn_items item1 = new spawn_items(bg_tranparant, x, y, type);
            GameConfig.item = item1;
            new Thread(item1, "Item").start();
        }
    }

    private void sendBackgroundToBack() {
        Container cp = getContentPane();
        if (bg_tranparant.getParent() == cp) {
            cp.setComponentZOrder(bg_tranparant, cp.getComponentCount() - 1);
        }
    }

    // ------------------------------------------------------------------
    // สร้าง/จัดการ tank ตามรายชื่อจาก server (reconcile ตาม hwid — ไม่สร้างซ้ำ)
    // ------------------------------------------------------------------
    public void CreatePlayer(String playerString) {
        if (playerString == null || playerString.trim().isEmpty()) return;

        String[] players = playerString.split(",");
        int slot = 0;
        for (String p : players) {
            slot++;
            String[] row = p.split(";");
            if (row.length < 3) continue;

            String hwid = row[0].trim();
            String name = row[1].trim();
            int tankIdx;
            try {
                tankIdx = Integer.parseInt(row[2].trim());
            } catch (NumberFormatException e) {
                tankIdx = 1;
            }
            tankIdx = Math.max(1, Math.min(6, tankIdx));

            GameConfig.playerNameByHwid.put(hwid, name);

            tank existing = findTank(hwid);
            if (existing != null) {
                existing.setName(name); // มีอยู่แล้ว — อัปเดตชื่ออย่างเดียว
                continue;
            }
            player(hwid, name, tankIdx, slot);
        }
    }

    private tank findTank(String hwid) {
        for (tank t : payerArr) {
            if (t.getHwid().equalsIgnoreCase(hwid)) return t;
        }
        return null;
    }

    private void player(String hwid, String name, int tankIdx, int slot) {
        int size = GameConfig.payer_size;
        int margin = 20;

        int w = bg_tranparant.getWidth() > 0 ? bg_tranparant.getWidth() : mainSizeW;
        int h = bg_tranparant.getHeight() > 0 ? bg_tranparant.getHeight() : mainSizeH;

        int left = margin;
        int top = margin;
        int right = w - size - margin;
        int bottom = h - size - margin;

        int location_x = left;
        int location_y = top;
        if (slot == 2) { location_x = right; }
        if (slot == 3) { location_y = bottom; }
        if (slot == 4) { location_x = right; location_y = bottom; }

        tank t1 = new tank(bg_tranparant, ImageConfig.mapImg.get("Tank" + tankIdx + ".png"),
                location_x, location_y, arr, hwid, "Tank" + tankIdx, bg_tranparant);
        t1.setName(name);

        payerArr.add(t1);
        new Thread(t1, "Tank-" + hwid).start();

        // ให้ kill feed อยู่บนสุดเสมอ (ไม่โดน tank บัง)
        bg_tranparant.setComponentZOrder(killFeedPanel, 0);

        if (hwid.equalsIgnoreCase(Server_cilent.getHwid())) {
            t1.setMe(true);
            me = t1;
            installKeyListener();
        }
    }

    // ------------------------------------------------------------------
    // Countdown 3-2-1 ก่อนเริ่มเกม
    // ------------------------------------------------------------------
    public void showCountdown(int seconds) {
        SwingUtilities.invokeLater(() -> {
            if (countdownTimer != null) countdownTimer.stop();

            GameConfig.gamestatus = false; // ยังขยับไม่ได้จนกว่าจบนับ
            if (countdownLabel == null) {
                countdownLabel = new JLabel("", SwingConstants.CENTER);
                countdownLabel.setFont(new Font("Arial", Font.BOLD, 120));
                countdownLabel.setForeground(Color.YELLOW);
            }
            int w = bg_tranparant.getWidth() > 0 ? bg_tranparant.getWidth() : mainSizeW;
            int h = bg_tranparant.getHeight() > 0 ? bg_tranparant.getHeight() : mainSizeH;
            countdownLabel.setBounds(0, h / 2 - 90, w, 180);
            bg_tranparant.add(countdownLabel);
            bg_tranparant.setComponentZOrder(countdownLabel, 0);

            final int[] remaining = { Math.max(1, seconds) };
            countdownLabel.setText(String.valueOf(remaining[0]));

            countdownTimer = new javax.swing.Timer(1000, null);
            countdownTimer.addActionListener(ev -> {
                remaining[0]--;
                if (remaining[0] > 0) {
                    countdownLabel.setText(String.valueOf(remaining[0]));
                } else {
                    countdownLabel.setText("GO!");
                    GameConfig.gamestatus = true;
                    javax.swing.Timer hide = new javax.swing.Timer(700, e2 -> {
                        countdownLabel.setVisible(false);
                        bg_tranparant.remove(countdownLabel);
                        bg_tranparant.repaint();
                    });
                    hide.setRepeats(false);
                    hide.start();
                    countdownTimer.stop();
                }
            });
            countdownTimer.start();
        });
    }

    // ------------------------------------------------------------------
    // Kill feed
    // ------------------------------------------------------------------
    public void addKillFeed(String killerHwid, String victimHwid) {
        SwingUtilities.invokeLater(() -> {
            String killer = GameConfig.playerNameByHwid.getOrDefault(killerHwid, "???");
            String victim = GameConfig.playerNameByHwid.getOrDefault(victimHwid, "???");

            JLabel feed = new JLabel(" * " + killer + " ->  " + victim, SwingConstants.LEFT);
            feed.setFont(new Font("Arial", Font.BOLD, 13));
            feed.setForeground(Color.WHITE);
            feed.setOpaque(true);
            feed.setBackground(new Color(0, 0, 0, 160));
            feed.setAlignmentX(Component.LEFT_ALIGNMENT);
            feed.setMaximumSize(new Dimension(300, 24));

            killFeedPanel.add(feed);
            killFeedPanel.revalidate();
            killFeedPanel.repaint();

            // ให้ข้อความไหลหายไปหลัง 3.5 วิ
            javax.swing.Timer t = new javax.swing.Timer(3500, e -> {
                killFeedPanel.remove(feed);
                killFeedPanel.revalidate();
                killFeedPanel.repaint();
            });
            t.setRepeats(false);
            t.start();
        });
    }

    // ------------------------------------------------------------------
    // จอตาย (เฉพาะตัวเอง)
    // ------------------------------------------------------------------
    public void showDeathOverlay() {
        SwingUtilities.invokeLater(() -> {
            if (deathLabel == null) {
                deathLabel = new JLabel("YOU DIED!", SwingConstants.CENTER);
                deathLabel.setFont(new Font("Arial", Font.BOLD, 60));
                deathLabel.setForeground(Color.RED);
                deathLabel.setOpaque(true);
                deathLabel.setBackground(new Color(0, 0, 0, 140));
            }
            int w = bg_tranparant.getWidth() > 0 ? bg_tranparant.getWidth() : mainSizeW;
            int h = bg_tranparant.getHeight() > 0 ? bg_tranparant.getHeight() : mainSizeH;
            deathLabel.setBounds(w / 2 - 250, h / 2 - 60, 500, 120);
            bg_tranparant.add(deathLabel);
            bg_tranparant.setComponentZOrder(deathLabel, 0);
            bg_tranparant.repaint();
        });
    }

    // ------------------------------------------------------------------
    // จอจบเกม: ผู้ชนะ + ตารางคะแนน + ปุ่ม restart (host เท่านั้น)
    // ------------------------------------------------------------------
    public void uiEndGame(String winnerHwid) {
        SwingUtilities.invokeLater(() -> {
            GameConfig.winnerHwid = winnerHwid;
            GameConfig.gamestatus = false;
            for (tank t : payerArr) t.clearMove();

            int w = bg_tranparant.getWidth() > 0 ? bg_tranparant.getWidth() : mainSizeW;
            int h = bg_tranparant.getHeight() > 0 ? bg_tranparant.getHeight() : mainSizeH;

            endOverlay = new JPanel(null) {
                @Override
                protected void paintComponent(Graphics g) {
                    g.setColor(new Color(0, 0, 0, 170));
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            };
            endOverlay.setOpaque(false);
            endOverlay.setBounds(0, 0, w, h);

            boolean iWon = Server_cilent.getHwid().equalsIgnoreCase(winnerHwid);
            String title;
            Color titleColor;
            if (winnerHwid == null || winnerHwid.equalsIgnoreCase("draw") || winnerHwid.isEmpty()) {
                title = "DRAW!";
                titleColor = Color.ORANGE;
            } else {
                String winnerName = GameConfig.playerNameByHwid.getOrDefault(winnerHwid, "???");
                title = "" + winnerName + " WINS!";
                titleColor = new Color(255, 215, 0);
            }

            JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
            lblTitle.setFont(new Font("Arial", Font.BOLD, 56));
            lblTitle.setForeground(titleColor);
            lblTitle.setBounds(0, h / 2 - 180, w, 80);
            endOverlay.add(lblTitle);

            // ตารางคะแนน (เรียง kill มากไปน้อย)
            List<String[]> score = new ArrayList<>(GameConfig.endScore);
            score.sort((a, b) -> {
                try {
                    return Integer.compare(Integer.parseInt(b[2]), Integer.parseInt(a[2]));
                } catch (Exception e) {
                    return 0;
                }
            });
            int y = h / 2 - 80;
            int rank = 1;
            for (String[] row : score) {
                String name = row[1].isEmpty() ? "???" : row[1];
                JLabel line = new JLabel(rank + ".  " + name + "  —  " + row[2] + " kills", SwingConstants.CENTER);
                line.setFont(new Font("Arial", Font.PLAIN, 20));
                line.setForeground(Server_cilent.getHwid().equalsIgnoreCase(row[0]) ? Color.YELLOW : Color.WHITE);
                line.setBounds(0, y, w, 28);
                endOverlay.add(line);
                y += 30;
                rank++;
            }

            // ปุ่ม restart — แสดงเฉพาะ host (server อนุญาตเฉพาะ host อยู่แล้ว)
            if (Server_cilent.isHost) {
                reStartBtn.setBounds(w / 2 - 60, y + 20, 120, 50);
                for (ActionListener al : reStartBtn.getActionListeners()) {
                    reStartBtn.removeActionListener(al);
                }
                reStartBtn.addActionListener(e -> {
                    Server_cilent.toServerC("server:ReStartGame:1");
                });
                endOverlay.add(reStartBtn);
            }

            bg_tranparant.add(endOverlay);
            bg_tranparant.setComponentZOrder(endOverlay, 0);
            bg_tranparant.repaint();
        });
    }

    public void removeEndGame() {
        SwingUtilities.invokeLater(() -> {
            if (endOverlay != null) {
                bg_tranparant.remove(endOverlay);
                endOverlay = null;
            }
            if (deathLabel != null) {
                bg_tranparant.remove(deathLabel);
            }
            killFeedPanel.removeAll();
            killFeedPanel.revalidate();

            for (tank t : payerArr) {
                t.setRun();
                bg_tranparant.remove(t);
            }
            payerArr.clear();
            me = null;
            GameConfig.winnerHwid = null;
            GameConfig.endScore = new ArrayList<>();
            GameConfig.gamestatus = false; // รอ countdown ของรอบใหม่ก่อน
            bg_tranparant.repaint();
            bg_tranparant.revalidate();
        });
    }

    public void focusGame() {
        bg_tranparant.requestFocusInWindow();
    }

    // ------------------------------------------------------------------
    // คีย์บอร์ด: กดค้างเดินต่อเรื่อยๆ ปล่อยเมื่อ keyReleased
    // ------------------------------------------------------------------
    private void installKeyListener() {
        bg_tranparant.setFocusable(true);
        bg_tranparant.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (me == null || me.isDead()) return;
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_W: case KeyEvent.VK_UP:    me.keyPressedDir("w"); break;
                    case KeyEvent.VK_S: case KeyEvent.VK_DOWN:  me.keyPressedDir("s"); break;
                    case KeyEvent.VK_A: case KeyEvent.VK_LEFT:  me.keyPressedDir("a"); break;
                    case KeyEvent.VK_D: case KeyEvent.VK_RIGHT: me.keyPressedDir("d"); break;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (me == null) return;
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_W: case KeyEvent.VK_UP:    me.keyReleasedDir("w"); break;
                    case KeyEvent.VK_S: case KeyEvent.VK_DOWN:  me.keyReleasedDir("s"); break;
                    case KeyEvent.VK_A: case KeyEvent.VK_LEFT:  me.keyReleasedDir("a"); break;
                    case KeyEvent.VK_D: case KeyEvent.VK_RIGHT: me.keyReleasedDir("d"); break;
                }
            }
        });
    }

    public static void main(String[] args) {
        Game f = new Game();
        f.setVisible(true);
    }
}
