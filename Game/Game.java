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
import java.util.Random;


public class Game extends JFrame {
    ArrayList<wall> arr = GameConfig.wallArrayList;
    List<tank> payerArr = GameConfig.payerArrayList;
    box_img bg_tranparant ;
    int mainSizeW = 1200;
    int mainSizeH = 700;
    boolean isStart = true;
    Random random = new Random();
    tank me;
    //Restart_btn
    Button_img reStartBtn ;


    public  Game(){
        setTitle("TankBoom");
        setSize(mainSizeW , mainSizeH);
        setLayout(null);
        ImageConfig img = new ImageConfig();
        img.lode_image();
        bg_tranparant = new box_img(ImageConfig.mapImg.get("Background.png"));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        bg_tranparant.setLayout(null);
        bg_tranparant.setBounds(0, 0, mainSizeW, mainSizeH);
        reStartBtn = new Button_img(ImageConfig.mapImg.get("Restart_btn.png"));

        wall w1 = new wall(this, 200, 200, 300, 300);
        wall w2 = new wall(this, 100, 100, 200, 50);
        wall w3 = new wall(this, 50, 50, 700, 450);
        wall w4 = new wall(this, 100, 20, 200, 600);
        
        arr.add(w1);
        arr.add(w2);
        arr.add(w3);
        arr.add(w4);
        
        



        bg_tranparant.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (me != null) {
                    me.shootServer(e.getX(), e.getY());
                }
                
            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }
            
        });

        
        add(bg_tranparant);
        sendBackgroundToBack();





    }

    public void spownItem(int x , int y , String type){
        if(GameConfig.item != null) GameConfig.item.Kill();

        spawn_items item1 = new spawn_items(bg_tranparant, x, y,type );
        GameConfig.item = item1;
        new Thread(item1).start();
    }

    private void sendBackgroundToBack() {
        Container cp = getContentPane();
        if (bg_tranparant.getParent() == cp) {
            cp.setComponentZOrder(bg_tranparant, cp.getComponentCount() - 1);
        }
    }
    public void CreatePlayer(String playeString){
        String[] Players = playeString.split(",");
        
        int i = 1;
        for(String p : Players){
            String[] PlayersRow = p.split(";");
            if (isStart) {
                player(PlayersRow[0] ,Integer.parseInt(PlayersRow[2]), i);
                System.out.println("fist Player : " + PlayersRow[0]);
            }else{
                for(tank t : payerArr){
                    if (!p.equalsIgnoreCase(t.getHwid())) {
                        player(PlayersRow[0] ,Integer.parseInt(PlayersRow[2]) , i);
                        System.out.println("new Player : " + PlayersRow[0]);
                    }
                }
            }
            
            i++;
            
        }
        isStart = false;
    }

    private void player(String p, int i , int slot) {
        int tank = GameConfig.payer_size;
        int margin = 20;

        int w = bg_tranparant.getWidth();
        int h = bg_tranparant.getHeight();

        int left   = margin;
        int top    = margin;
        int right  = w - tank - margin;
        int bottom = h - tank - margin;

        int location_x = left;
        int location_y = top;

        if (slot == 2) { location_x = right; }
        if (slot == 3) { location_y = bottom; }
        if (slot == 4) { location_x = right; location_y = bottom; }

        tank t1 = new tank(bg_tranparant, ImageConfig.mapImg.get("Tank" + i + ".png"),location_x, location_y, arr, p, "Tank" + i, bg_tranparant);

        payerArr.add(t1);
        new Thread(t1).start();

        if (p.equalsIgnoreCase(Server_cilent.getHwid())) {
            me = t1;
            installKeyListener();
        }
    }

    public void uiEndGame(){
        
        reStartBtn.setBounds((bg_tranparant.getWidth() / 2) - 50, (bg_tranparant.getHeight() / 2) - 50, 100, 40);
        reStartBtn.addActionListener(e -> {
            String msg = "server:ReStartGame:1";
            Server_cilent.toServer(msg);
            bg_tranparant.remove(reStartBtn);
        });
        bg_tranparant.add(reStartBtn);
    }

    public void removeEndGame(){
        bg_tranparant.remove(reStartBtn);
        for(tank t : payerArr){
            t.setRun();
            bg_tranparant.remove(t);
        }

        payerArr.clear();
        isStart = true;
        me = null;
        GameConfig.gamestatus = true;
    }
    public void focusGame() {
        bg_tranparant.requestFocusInWindow();
    }
    private void installKeyListener() {
        bg_tranparant.setFocusable(true);
        bg_tranparant.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (me == null) return;
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_W: case KeyEvent.VK_UP:    me.setMoveDirection("w"); break;
                    case KeyEvent.VK_S: case KeyEvent.VK_DOWN:  me.setMoveDirection("s"); break;
                    case KeyEvent.VK_A: case KeyEvent.VK_LEFT:  me.setMoveDirection("a"); break;
                    case KeyEvent.VK_D: case KeyEvent.VK_RIGHT: me.setMoveDirection("d"); break;
                }
            }
        });
    }
    public static void main(String[] args) {
        Game f =  new Game();
        f.setVisible(true);
    }

}