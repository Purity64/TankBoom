package Game;
import javax.swing.*;

import config.GameConfig;
import config.ImageConfig;
import items.spawn_items;
import items.tank;
import items.wall;
import server.Server_cilent;
import utail.box_img;

import java.awt.Container;
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
    JButton reStartBtn = new JButton("ReStart");
    public  Game(){
        setTitle("TankBoom");
        setSize(mainSizeW , mainSizeH);
        setLayout(null);
        ImageConfig img = new ImageConfig();
        img.lode_image();
        bg_tranparant = new box_img(ImageConfig.mapImg.get("Background.png"));
        
        bg_tranparant.setLayout(null);
        bg_tranparant.setBounds(0, 0, mainSizeW, mainSizeH);
        

        
        
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
            if (isStart) {
                player(p ,i);
                System.out.println("fist Player : " + p);
            }else{
                for(tank t : payerArr){
                    if (!p.equalsIgnoreCase(t.getHwid())) {
                        player(p ,i);
                        System.out.println("new Player : " + p);
                    }
                }
            }
            
            i++;
            
        }
        isStart = false;
    }

    private void player(String p, int i) {
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

        if (i == 2) { location_x = right; }
        if (i == 3) { location_y = bottom; }
        if (i == 4) { location_x = right; location_y = bottom; }

        tank t1 = new tank(bg_tranparant, ImageConfig.mapImg.get("Tank" + i + ".png"),location_x, location_y, arr, p, "Tank" + i, bg_tranparant);

        payerArr.add(t1);
        new Thread(t1).start();

        if (p.equalsIgnoreCase(Server_cilent.getHwid())) {
            me = t1;
            installKeyBindings();
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

    private void installKeyBindings() {
        InputMap im = bg_tranparant.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = bg_tranparant.getActionMap();
        int[] keys = {KeyEvent.VK_W, KeyEvent.VK_UP, KeyEvent.VK_S, KeyEvent.VK_DOWN,
                    KeyEvent.VK_A, KeyEvent.VK_LEFT, KeyEvent.VK_D, KeyEvent.VK_RIGHT};
        String[] dirs = {"w","w","s","s","a","a","d","d"};
        for (int i = 0; i < keys.length; i++) {
            final String dir = dirs[i];
            im.put(KeyStroke.getKeyStroke(keys[i], 0), "move_" + dirs[i] + "_" + keys[i]);
            am.put("move_" + dirs[i] + "_" + keys[i], new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    if (me != null) me.setMoveDirection(dir);
                }
            });
        }
    }
    public static void main(String[] args) {
        Game f =  new Game();
        f.setVisible(true);
    }

}