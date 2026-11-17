package server;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import Game.Game;
import config.GameConfig;
import items.tank;

public class Server_cilent {
    private static Socket socket;
    private static PrintWriter out;
    private static BufferedReader in;
    private static volatile boolean isConnected = false;
    /** ตัดการเชื่อมต่อเอง (กด Leave / join ไม่ผ่าน) → ไม่ต้องโชว์ dialog หาย connection */
    private static volatile boolean voluntaryDisconnect = false;

    /** true = เครื่องนี้สร้างห้องเป็น host */
    public static volatile boolean isHost = false;

    private static String hwid = UUID.randomUUID().toString().replace("-", "");
    private static String ip = "127.0.0.1";
    private static int port = 50101;

    private static String player = "";
    private static int MaxPlayer;

    public static String getPlayer() {
        return player;
    }

    public static void connect(String ipConnet, int maxPlayer, String Playername) {
        MaxPlayer = maxPlayer;
        if (isConnected) return;

        isHost = (ipConnet == null);
        voluntaryDisconnect = false;

        new Thread(() -> {
            try {
                if (ipConnet != null) {
                    ip = ipConnet;
                }
                System.out.println("[CONNECTING] เชื่อมต่อไปยัง " + ip + ":" + port + "...");
                socket = new Socket(ip, port);
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                out.println(hwid);
                isConnected = true;
                System.out.println("[SUCCESS] เชื่อมต่อสำเร็จ!");
                if (isHost) {
                    toServerC("server:createRoom:" + maxPlayer);
                    toServerC("server:UpDatePlayerDataHost:" + sanitize(Playername) + "," + getHwid());
                } else {
                    toServerC("server:joinRoom:" + getHwid());
                }

                String incomingMessage;
                while (isConnected && (incomingMessage = in.readLine()) != null) {
                    if (incomingMessage.trim().isEmpty()) continue;
                    System.out.println("[RECEIVED]: " + incomingMessage);
                    handle(incomingMessage);
                }
            } catch (Exception e) {
                if (!voluntaryDisconnect) {
                    System.out.println("[ERROR] Connection Error: " + e.getMessage());
                }
            } finally {
                boolean wasVoluntary = voluntaryDisconnect;
                disconnect();
                if (!wasVoluntary) {
                    showConnectionLost();
                }
            }
        }).start();
    }

    /** ตัดอักขระที่ทำ protocol พังออกจากชื่อ */
    private static String sanitize(String s) {
        if (s == null) return "";
        return s.replace(":", " ").replace(";", " ").replace(",", " ").trim();
    }

    // ------------------------------------------------------------------
    // จัดการข้อความจาก server — parse ปลอดภัย ข้อความพังไม่พังทั้ง session
    // ------------------------------------------------------------------
    private static void handle(String msg) {
        // รูปแบบ: <sender>:<method>:<data...> — data มี ":" อะไรก็ได้
        String[] parts = msg.split(":", 3);
        if (parts.length < 2) return;

        String sender = parts[0];
        String method = parts[1];
        String data = parts.length > 2 ? parts[2] : "";

        if (sender.equalsIgnoreCase("server")) {
            handleServerMsg(method, data);
        } else {
            handleGameMsg(sender, method, data);
        }
    }

    private static void handleServerMsg(String method, String data) {
        String m = method.toLowerCase();

        switch (m) {
            case "player": {
                SwingUtilities.invokeLater(() -> GameConfig.mainGame.CreatePlayer(data));
                break;
            }
            case "playeralldata": {
                SwingUtilities.invokeLater(() -> updateLobbySlots(data));
                break;
            }
            case "spownitem": {
                try {
                    String[] row = data.split(",");
                    final int x = Integer.parseInt(row[0].trim());
                    final int y = Integer.parseInt(row[1].trim());
                    final String type = row[2].trim();
                    SwingUtilities.invokeLater(() -> GameConfig.mainGame.spownItem(x, y, type));
                } catch (Exception e) {
                    System.out.println("[WARN] spownitem พัง: " + data);
                }
                break;
            }
            case "countdown": {
                try {
                    GameConfig.mainGame.showCountdown(Integer.parseInt(data.trim()));
                } catch (NumberFormatException ignored) {}
                break;
            }
            case "gamestartcreate": {
                SwingUtilities.invokeLater(() -> {
                    GameConfig.lobby.dispose();
                    GameConfig.mainGame.setVisible(true);
                    GameConfig.mainGame.focusGame();
                });
                break;
            }
            case "game_start": {
                GameConfig.gamestatus = true;
                break;
            }
            case "killed": {
                String victim = data.trim();
                SwingUtilities.invokeLater(() -> {
                    tank t = findTank(victim);
                    if (t != null) t.die();
                    if (victim.equalsIgnoreCase(getHwid())) {
                        GameConfig.mainGame.showDeathOverlay();
                    }
                });
                break;
            }
            case "killmsg": {
                String[] row = data.split(";", 2);
                if (row.length == 2) {
                    GameConfig.mainGame.addKillFeed(row[0].trim(), row[1].trim());
                }
                break;
            }
            case "score": {
                List<String[]> rows = new ArrayList<>();
                try {
                    if (!data.trim().isEmpty()) {
                        for (String entry : data.split(",")) {
                            String[] cols = entry.split(";");
                            if (cols.length >= 3) {
                                rows.add(new String[]{cols[0].trim(), cols[1].trim(), cols[2].trim()});
                            }
                        }
                    }
                } catch (Exception ignored) {}
                GameConfig.endScore = rows;
                break;
            }
            case "game_end": {
                GameConfig.winnerHwid = data.trim();
                GameConfig.gamestatus = false;
                GameConfig.mainGame.uiEndGame(data.trim());
                break;
            }
            case "game_restart": {
                GameConfig.gamestatus = false;
                GameConfig.mainGame.removeEndGame();
                break;
            }
            case "returnjoin": {
                if (data.trim().equalsIgnoreCase("ok")) {
                    if (isHost) break; // host เปิด lobby เองแล้วตั้งแต่กดปุ่ม
                    SwingUtilities.invokeLater(() -> {
                        toServerC("server:UpDatePlayerData:" + sanitize(playerNameForJoin()) + "," + getHwid());
                        GameConfig.lobby.setVisible(true);
                        if (GameConfig.minUi != null) GameConfig.minUi.dispose();
                    });
                } else {
                    voluntaryDisconnect = true;
                    disconnect();
                    SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(null,
                            "เข้าร่วมห้องไม่สำเร็จ (ห้องเต็มหรือเริ่มเกมไปแล้ว)",
                            "Join failed",
                            JOptionPane.ERROR_MESSAGE));
                }
                break;
            }
            case "host_left": {
                voluntaryDisconnect = true;
                disconnect();
                returnToMenuOnEdt("Host ได้ออกจากเกมแล้ว ห้องถูกปิด");
                break;
            }
            case "nothost":
            case "needplayers":
            default:
                // ไม่รู้จัก — ข้ามไป QUIET
                break;
        }
    }

    private static String playerNameForJoin() {
        // ชื่อที่กรอกตอน JOIN เก็บไว้ชั่วคราวจาก MainMenuUI
        return joinName != null ? joinName : "Player";
    }

    private static String joinName = null;

    public static void setJoinName(String n) {
        joinName = sanitize(n);
    }

    private static void handleGameMsg(String senderHwid, String method, String data) {
        switch (method.toLowerCase()) {
            case "move": {
                try {
                    String[] d = data.split(",");
                    int x = Integer.parseInt(d[0].trim());
                    int y = Integer.parseInt(d[1].trim());
                    double angleDeg = d.length > 2 ? Double.parseDouble(d[2].trim()) : 0;
                    tank t = findTank(senderHwid);
                    if (t != null) {
                        t.updateFromNetwork(x, y, angleDeg);
                    }
                } catch (Exception ignored) {}
                break;
            }
            case "shoot": {
                try {
                    String[] d = data.split(",");
                    int x = Integer.parseInt(d[0].trim());
                    int y = Integer.parseInt(d[1].trim());
                    tank t = findTank(senderHwid);
                    if (t != null) {
                        t.shoot(x, y);
                    }
                } catch (Exception ignored) {}
                break;
            }
            default:
                break;
        }
    }

    private static tank findTank(String hwid) {
        for (tank t : GameConfig.payerArrayList) {
            if (t.getHwid().equalsIgnoreCase(hwid)) return t;
        }
        return null;
    }

    private static void updateLobbySlots(String data) {
        try {
            GameConfig.lobby.clearSlots();
            if (data.trim().isEmpty()) return;
            String[] row = data.split(",");
            for (int i = 0; i < row.length && i < 4; i++) {
                String[] playerData = row[i].split(";");
                if (playerData.length >= 3) {
                    GameConfig.lobby.updateSlot(i, playerData[1], Integer.parseInt(playerData[2].trim()));
                }
            }
        } catch (Exception e) {
            System.out.println("[WARN] playerAlldata พัง: " + data);
        }
    }

    // ------------------------------------------------------------------
    // หลุด connection กลางเกม — แจ้ง user แล้วกลับเมนู
    // ------------------------------------------------------------------
    private static void showConnectionLost() {
        returnToMenuOnEdt("การเชื่อมต่อกับ Server หายไป");
    }

    private static void returnToMenuOnEdt(String message) {
        SwingUtilities.invokeLater(() -> {
            try {
                GameConfig.mainGame.setVisible(false);
                GameConfig.lobby.dispose();
            } catch (Exception ignored) {}

            if (message != null && !message.isEmpty()) {
                JOptionPane.showMessageDialog(null, message, "TankBoom",
                        JOptionPane.WARNING_MESSAGE);
            }

            try {
                GameConfig.minUi = new ui.MainMenuUI();
                GameConfig.minUi.setVisible(true);
            } catch (Exception ignored) {}
        });
    }

    // ------------------------------------------------------------------
    // ส่งข้อความ
    // ------------------------------------------------------------------
    public static void toServer(String data) {
        if (out != null && isConnected) {
            // split limit=2: data มี ":" ก็ไม่หาย (เช่น move:100,200:35)
            String[] row = data.split(":", 2);
            String action = row[0];
            String datas = row.length > 1 ? row[1] : "";
            String send;
            if (action.equalsIgnoreCase("server")) {
                // ไม่ควรเกิด แต่กันไว้
                send = data + ":" + hwid;
            } else {
                send = hwid + ":" + action + ":" + datas;
            }
            out.println(send);
        } else {
            System.out.println("[WARNING] ไม่สามารถส่งข้อมูลได้ เนื่องจากยังไม่ได้เชื่อมต่อ Server");
        }
    }

    public static void toServerC(String data) {
        if (out != null && isConnected) {
            out.println(data);
        } else {
            System.out.println("[WARNING] ไม่สามารถส่งข้อมูลได้ เนื่องจากยังไม่ได้เชื่อมต่อ Server");
        }
    }

    public static void disconnect() {
        isConnected = false;
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null && !socket.isClosed()) socket.close();
            System.out.println("[DISCONNECTED] ตัดการเชื่อมต่อเรียบร้อย");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** ตัดการเชื่อมต่อแบบตั้งใจ (กด Leave) — ไม่โชว์ dialog หาย connection */
    public static void disconnectVoluntarily() {
        voluntaryDisconnect = true;
        disconnect();
    }

    public static String getHwid() {
        return hwid;
    }

    public static void setHwid(String newHwid) { hwid = newHwid; }
    public static void setIp(String newIp) { ip = newIp; }
    public static void setPort(int newPort) { port = newPort; }
    public static boolean isConnected() { return isConnected; }
}
