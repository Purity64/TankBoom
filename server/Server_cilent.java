package server;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

import Game.Game;
import config.GameConfig;
import items.tank;

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
                    String player = data[0];
                    String method = data[1];
                    String maindata = data[2]; 
                    System.out.println("[RECEIVED]: " + msg);
                    if (player.equalsIgnoreCase("server")) {
                        if (method.equalsIgnoreCase("player")) {
                            mainF.CreatePlayer(maindata);
                        }else if(method.equalsIgnoreCase("spownitem")){
                            String[] row = maindata.split(",");
                            int x = Integer.parseInt(row[0]);
                            int y = Integer.parseInt(row[1]);
                            String type =row[2];
                            int name = Integer.parseInt(row[3]);
                            mainF.spownItem(x, y, type);
                        }

                    }else if (method.equalsIgnoreCase("move")) {
                        List<tank> arr = GameConfig.payerArrayList;

                        for(tank t : arr){
                            if (player.equalsIgnoreCase(t.getHwid())) {
                                String[] d = maindata.split(",");
                                int x = Integer.parseInt(d[0]);
                                int y = Integer.parseInt(d[1]);
                                t.setLocation(x, y);

                            }
                        }
                    }else if (method.equalsIgnoreCase("shoot")) {
                        List<tank> arr = GameConfig.payerArrayList;

                        for(tank t : arr){
                            if (player.equalsIgnoreCase(t.getHwid())) {
                                String[] d = maindata.split(",");
                                int x = Integer.parseInt(d[0]);
                                int y = Integer.parseInt(d[1]);
                                t.shootServer(x, y);
                            }
                        }
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