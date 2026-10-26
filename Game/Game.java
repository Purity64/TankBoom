package Game;
import javax.swing.*;

import config.GameConfig;
import config.ImageConfig;
import items.spawn_items;
import items.tank;
import items.wall;
import server.Server_cilent;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class Game extends JFrame {
    ArrayList<wall> arr = GameConfig.wallArrayList;
    List<tank> payerArr = GameConfig.payerArrayList;
    JPanel bg_tranparant =  new JPanel();
    int mainSizeW = 1000;
    int mainSizeH = 700;
    boolean isStart = true;
    Random random = new Random();
    tank me;
    public  Game(){
        setTitle("TankBoom");
        setSize(mainSizeW , mainSizeH);
        setLayout(null);
        ImageConfig img = new ImageConfig();
        img.lode_image();
        
        bg_tranparant.setLayout(null);
        bg_tranparant.setBounds(0, 0, mainSizeW, mainSizeH);
        bg_tranparant.setOpaque(false);


        
        
        wall w1 = new wall(this, 200, 200, 300, 300);
        // wall w2 = new wall(this, 100, 100, 200, 50);
        // wall w3 = new wall(this, 50, 50, 700, 450);
        // wall w4 = new wall(this, 100, 20, 200, 600);
        
         arr.add(w1);
        // arr.add(w2);
        // arr.add(w3);
        // arr.add(w4);
        
        Server_cilent.connect(this);



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






    }

    public void spownItem(int x , int y , String type){
        if(GameConfig.item != null) GameConfig.item.Kill();

        spawn_items item1 = new spawn_items(Game.this, x, y,type );
        GameConfig.item = item1;
        new Thread(item1).start();
    }
    public void CreatePlayer(String playeString){
        System.out.println(playeString);
        String[] Players = playeString.split(",");
        int i = 1;
        for(String p : Players){
            if (isStart) {

                tank t1 = new tank(this ,ImageConfig.mapImg.get("Tank"+ i + ".png") , 10 , 100, arr , p ,"Tank"+i, bg_tranparant );
                payerArr.add(t1);
                new Thread(t1).start();
                if (p.equalsIgnoreCase(Server_cilent.getHwid())) {
                    me = t1;
                }
            }else{
                for(tank t : payerArr){
                    if (!p.equalsIgnoreCase(t.getHwid())) {
                        tank t1 = new tank(this ,ImageConfig.mapImg.get("Tank"+ i + ".png") , 10 , 100, arr , p ,"Tank"+i , bg_tranparant);
                        payerArr.add(t1);
                        new Thread(t1).start();
                    }
                }
            }

            System.out.println(p);

            
            i++;
            
        }
        isStart = false;
    }
    public String randomType(){
        int randomNumber = random.nextInt(3) + 1;
        String type = "";
        switch (randomNumber) {
            case 1: type = "boom_bullet";break;
            case 2: type = "hp";break;
            case 3: type = "shield";break;
            default: type = "boom_bullet";break;
        }
        return type;
    }
    public static void main(String[] args) {
        Game f =  new Game();
        f.setVisible(true);
    }
}