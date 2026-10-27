package config;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import items.spawn_items;
import items.tank;
import items.wall;
import ui.LobbyUI;

/**
 * Config
 */
public class GameConfig {

    public static ArrayList<wall> wallArrayList = new ArrayList<>() ;
    public static List<tank> payerArrayList = new CopyOnWriteArrayList<>();

    public static String[] playerNames = {null, null, null, null};
    public static int[] playerTanks = {1, 0, 0, 0};

    public static final int MySlot = 0;
    public static final int payer_size = 50;
    public static final int TankHP = 100;
    public static final int shield_time = 5;
    
    public static LobbyUI lobby = new LobbyUI();
    //items
    public static spawn_items item;
    public final static int  item_size = 50;


    //bullet
    public static final int MaxBullet = 10;
    public static final int cooldown = 3;
    public static final int normalBullet = 10;
    public static final int BoomBullet = 20;
    public static final int damage_normal  = 5;
    public static final int damage_boom  = 10;

    //server
    public static final int PORT = 50101;
    public static boolean gamestatus = false;

}