package config;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;


import Game.Game;

public class Server_cilent {
    private static Socket socket;
    private static PrintWriter out;
    private static BufferedReader in;
    private static boolean isConnected = false;
    

    private static String hwid = "jeerawdawd";
    private static String ip = "127.0.0.1";
    private static int port = 50101;

    private static String player = "";

    public static String getPlayer() {
        return player;
    }

    public static void connect(Game mainF) {
        if (isConnected) return; 

        new Thread(() -> {
            try {
                System.out.println("[CONNECTING] เชื่อมต่อไปยัง " + ip + ":" + port + "...");
                socket = new Socket(ip, port);
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                
                out.println(hwid);
                isConnected = true;
                System.out.println("[SUCCESS] เชื่อมต่อสำเร็จ!");

                String incomingMessage;
                while (isConnected && (incomingMessage = in.readLine()) != null) {
                    String msg = incomingMessage;
                    String[] data = msg.split(":");
                    String method = data[0];
                    System.out.println("[RECEIVED]: " + msg);
                    if (method.equalsIgnoreCase("player")) {
                        player = data[1];
                        mainF.CreatePlayer(player);
                    }
                    
                    // parseCommand(msg);
                }
            } catch (Exception e) {
                System.out.println("[ERROR] Connection Error: " + e.getMessage());
            } finally {
                disconnect();
            }
        }).start();
    }

    public static void toServer(String data) {
        if (out != null && isConnected) {
            String[] row = data.split(":");
            String action = row[0];
            String datas = row[1];
            String sead = hwid+":"+action+":"+datas;
            out.println(sead);
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

    public static String getHwid() {
        return hwid;
    }
    public static void setHwid(String newHwid) { hwid = newHwid; }
    public static void setIp(String newIp) { ip = newIp; }
    public static void setPort(int newPort) { port = newPort; }
    public static boolean isConnected() { return isConnected; }
}