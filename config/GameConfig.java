package config;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import Game.Game;
import items.spawn_items;
import items.tank;
import items.wall;
import ui.LobbyUI;
import ui.MainMenuUI;

/**
 * Config
 */
public class GameConfig {

    public static ArrayList<wall> wallArrayList = new ArrayList<>() ;
    public static List<tank> payerArrayList = new CopyOnWriteArrayList<>();

    /** ชื่อผู้เล่นแบบ hwid -> name (มาจาก server:player) ใช้ทำ kill feed / จอผู้ชนะ */
    public static Map<String, String> playerNameByHwid = new HashMap<>();

    public static String[] playerNames = {null, null, null, null};
    public static int[] playerTanks = {1, 0, 0, 0};

    public static final int MySlot = 0;
    public static final int payer_size = 50;
    public static final int TankHP = 100;
    public static final int shield_time = 5;

    public static LobbyUI lobby = new LobbyUI();
    public static MainMenuUI minUi = new MainMenuUI();
    public static Game mainGame = new Game();
    //items
    public static volatile spawn_items item;
    public final static int  item_size = 50;


    //bullet
    public static final int MaxBullet = 10;
    public static final int cooldown = 3;
    public static final int normalBullet = 10;
    public static final int BoomBullet = 20;
    public static final int damage_normal  = 5;
    public static final int damage_boom  = 10;
    /** พักสั้นๆ ระหว่างนัดยิง (มิลลิวินาที) กันการรัวเร็วเหมือนปืนกล */
    public static final int shootCooldownMs = 350;

    //server
    public static final int PORT = 50101;
    public static volatile boolean gamestatus = false;

    // ผลจบเกม (จาก server:score / server:game_end)
    public static volatile String winnerHwid = null;
    /** แถวผลคะแนน: [0]=hwid [1]=ชื่อ [2]=kill */
    public static volatile List<String[]> endScore = new ArrayList<>();

}
