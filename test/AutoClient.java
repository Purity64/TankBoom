import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.SwingUtilities;
import javax.swing.Timer;

import config.GameConfig;
import items.tank;
import server.Server_cilent;
import ui.LobbyUI;

/**
 * Auto test driver — รัน client ตัวจริง (GUI) เป็น host แล้วให้ bot 2 ตัวต่อเข้ามาเล่น
 * จำลอง: สร้างห้อง, เริ่มเกม, เดิน, ยิง, โดนฆ่า, kill feed, จอจบเกม, restart, message พัง
 * ทุกขั้นตอนพิมพ์ [TEST] ออก console เพื่อตรวจ log
 */
public class AutoClient {

    static class Bot {
        Socket socket;
        BufferedReader in;
        PrintWriter out;
        String hwid;
        List<String> received = Collections.synchronizedList(new ArrayList<>());

        Bot(String hwid) throws Exception {
            this.hwid = hwid;
            socket = new Socket("127.0.0.1", 50101);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            out.println(hwid);
            Thread t = new Thread(() -> {
                try {
                    String line;
                    while ((line = in.readLine()) != null) {
                        received.add(line);
                        System.out.println("  [BOT " + hwid + "] <-- " + line);
                    }
                } catch (IOException ignored) {}
            });
            t.setDaemon(true);
            t.start();
        }

        void send(String msg) {
            System.out.println("  [BOT " + hwid + "] --> " + msg);
            out.println(msg);
        }

        boolean has(String needle) {
            synchronized (received) {
                for (String m : received) if (m.contains(needle)) return true;
            }
            return false;
        }
    }

    static Bot b2, b3;

    static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }

    public static void main(String[] args) throws Exception {
        System.out.println("[TEST] ==== เริ่ม AutoClient test ====");

        // ---- 1. host ตัวจริง (client ตัวจริง connect) ----
        SwingUtilities.invokeLater(() -> {
            LobbyUI lobby = GameConfig.lobby;
            lobby.setmaxPlayers(3);
            lobby.updateSlot(0, "AutoHost", 1);
            lobby.setVisible(true);
        });
        Server_cilent.connect(null, 3, "AutoHost");
        sleep(1500);

        System.out.println("[TEST] 1.1 host connected = " + Server_cilent.isConnected());
        System.out.println("[TEST] 1.2 host isHost flag = " + Server_cilent.isHost);

        // ---- 2. bot 2 ตัวเข้าห้อง ----
        b2 = new Bot("BOT_TWO_HWID");
        b3 = new Bot("BOT_THREE_HWID");
        b2.send("server:joinRoom:BOT_TWO_HWID");
        b3.send("server:joinRoom:BOT_THREE_HWID");
        b2.send("server:UpDatePlayerData:BotTwo,BOT_TWO_HWID");
        b3.send("server:UpDatePlayerData:BotThree,BOT_THREE_HWID");
        b2.send("server:setTank:2,BOT_TWO_HWID");
        sleep(1500);

        System.out.println("[TEST] 2.1 b2 join ok = " + b2.has("returnJoin:ok"));
        System.out.println("[TEST] 2.2 lobby ได้รับ playerAlldata = " + b2.has("AutoHost;"));

        // ---- 3. ทดสอบ message พัง (client ต้องไม่ตาย) ----
        b2.send("garbage-no-colon");
        b2.send("a:b");
        b2.send("server:player:");
        b2.send("server:spownitem:notanumber,xx,bad");
        b2.send("server:myhp:abc");
        System.out.println("[TEST] 3. ส่ง message พัง 5 อันแล้ว — client ยังอยู่ไหม (เดี๋ยวเช็คตอนท้าย)");

        // ---- 4. เริ่มเกม (host ตัวจริงสั่ง) ----
        Server_cilent.toServerC("server:GameStart");
        sleep(500);
        System.out.println("[TEST] 4.1 GameStartCreate = " + b2.has("GameStartCreate"));
        System.out.println("[TEST] 4.2 countdown = " + b2.has("countdown:3"));

        // รอ countdown จบ (~4 วิ)
        sleep(4500);
        System.out.println("[TEST] 4.3 gamestatus หลัง countdown = " + GameConfig.gamestatus);
        System.out.println("[TEST] 4.4 จำนวน tank บนจอ = " + GameConfig.payerArrayList.size());

        tank me = findMe();
        System.out.println("[TEST] 4.5 หา me tank = " + (me != null) + " hp=" + (me != null ? me.getHp() : -1));

        // ---- 5. บอทเดิน/ยิง/หัน (ตัวอื่นต้องเห็น) ----
        b2.send("BOT_TWO_HWID:move:300,300,0");
        b2.send("BOT_TWO_HWID:shoot:600,350");
        b2.send("BOT_TWO_HWID:aim:45");
        b2.send("BOT_TWO_HWID:aim:notanumber"); // พัง — ต้องไม่ crash
        sleep(500);

        // ---- 6. ฉันเดิน + ยิง ----
        final tank meF = me;
        if (meF != null) {
            SwingUtilities.invokeLater(() -> meF.keyPressedDir("d"));
            sleep(1200);
            SwingUtilities.invokeLater(() -> meF.keyReleasedDir("d"));
            sleep(200);
            System.out.println("[TEST] 6.1 เดินแล้ว x เปลี่ยน (เดิม 20) = " + meF.getX());
            final int cx = meF.getX(), cy = meF.getY();
            SwingUtilities.invokeLater(() -> meF.shootServer(600, 350));
            sleep(400);
        }

        // ---- 7. ฆ่ากัน: b3 ฆ่า b2 → kill feed; จากนั้น host ฆ่า b3 → ชนะ ----
        b3.send("server:playerkill:BOT_TWO_HWID,BOT_THREE_HWID");
        sleep(800);
        System.out.println("[TEST] 7.1 b2 ถูก killed = " + b2.has("killed:BOT_TWO_HWID"));
        System.out.println("[TEST] 7.2 killmsg = " + b2.has("killmsg:BOT_THREE_HWID;BOT_TWO_HWID"));
        System.out.println("[TEST] 7.3 tank b2 หายจากจอ = " + (findTank("BOT_TWO_HWID") == null));

        Server_cilent.toServerC("server:playerkill:BOT_THREE_HWID," + Server_cilent.getHwid());
        sleep(1000);
        System.out.println("[TEST] 7.4 game_end = " + b2.has("game_end:" + Server_cilent.getHwid()));
        System.out.println("[TEST] 7.5 score = " + b2.has("server:score:"));
        System.out.println("[TEST] 7.6 gamestatus หลังจบ = " + GameConfig.gamestatus);

        // ---- 8. restart (host) ----
        Server_cilent.toServerC("server:ReStartGame:1");
        sleep(6000); // รอ restart + countdown ใหม่
        System.out.println("[TEST] 8.1 game_restart = " + b2.has("game_restart"));
        System.out.println("[TEST] 8.2 จำนวน tank หลัง restart = " + GameConfig.payerArrayList.size());
        System.out.println("[TEST] 8.3 countdown รอบใหม่ = " + count(b2, "countdown:3"));

        // ---- 9. รอ item spawn จาก server แล้วให้ฉันเก็บ (ยิงไม่ได้ก็ทับเอง) ----
        System.out.println("[TEST] 9.1 รอ spownitem (จะมาทุก 10 วิ)...");
        long waitStart = System.currentTimeMillis();
        boolean gotItem = false;
        while (System.currentTimeMillis() - waitStart < 12000) {
            if (GameConfig.item != null) { gotItem = true; break; }
            sleep(300);
        }
        System.out.println("[TEST] 9.2 มีไอเทมบนจอ = " + gotItem
                + (GameConfig.item != null ? " type=" + GameConfig.item.getType() : ""));

        // ---- 10. จบเกม: b2 ฆ่า b3 ก่อน แล้วฆ่าฉัน → b2 ชนะ (เหลือคนเดียว) ----
        String meHwid = Server_cilent.getHwid();
        b2.send("server:playerkill:BOT_THREE_HWID,BOT_TWO_HWID");
        sleep(400);
        b2.send("server:playerkill:" + meHwid + ",BOT_TWO_HWID");
        sleep(800);
        System.out.println("[TEST] 10.1 game_end b2 = " + b2.has("game_end:BOT_TWO_HWID"));
        System.out.println("[TEST] 10.2 winnerHwid = " + GameConfig.winnerHwid);

        // ---- 11. ออกจากเกม ----
        Server_cilent.toServerC("server:LeaveGame:" + Server_cilent.getHwid());
        Server_cilent.disconnectVoluntarily();
        sleep(800);

        System.out.println("[TEST] ==== จบ AutoClient test (ดู [TEST] + Exception ใน log) ====");
        System.exit(0);
    }

    static tank findMe() {
        for (tank t : GameConfig.payerArrayList) {
            if (t.getHwid().equalsIgnoreCase(Server_cilent.getHwid())) return t;
        }
        return null;
    }

    static tank findTank(String hwid) {
        for (tank t : GameConfig.payerArrayList) {
            if (t.getHwid().equalsIgnoreCase(hwid)) return t;
        }
        return null;
    }

    static int count(Bot b, String needle) {
        int n = 0;
        synchronized (b.received) {
            for (String m : b.received) if (m.contains(needle)) n++;
        }
        return n;
    }
}
