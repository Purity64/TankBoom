package items;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.event.*;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;
import javax.swing.JPanel;

import config.GameConfig;
import config.ImageConfig;
import config.Server_cilent;
import items.bullet.ball;
import items.bullet.ballBoom;
import utail.box_img;
import utail.check_hitbox;

public class tank extends box_img implements Runnable{
    int payer_size = GameConfig.payer_size;
    int bulletCount = GameConfig.MaxBullet;
    String hwid ;

    int max_window_y ;
    int max_window_x ;

    JFrame jf;

    String location ;
    ArrayList<wall> arr ;

    String my_item = "";
    int shield_time = 0;
    int maxHp = GameConfig.TankHP;
    int hp = GameConfig.TankHP - 50;

    boolean run = true;
    boolean isrelode = false;
    BufferedImage mainImage ;
    BufferedImage shieldImage;
    String nameImage ;

    String bullet = "normal";

    public tank( JFrame jf,BufferedImage mainImage , int x , int y, ArrayList<wall> arr , String hwid , String nameImage ){
        super(mainImage);
        this.mainImage = mainImage;
        shieldImage = ImageConfig.mapImg.get(nameImage+"_shield.png");
        this.nameImage = nameImage;
        
        setBounds(x, y, payer_size, payer_size);
        this.hwid = hwid;

        max_window_y = jf.getHeight();
        max_window_x = jf.getWidth();
        this.arr = arr;
        this.jf = jf;
        

        if (hwid.equalsIgnoreCase(Server_cilent.getHwid())) {
            addKeyListener(new KeyListener() {
                @Override
                public void keyTyped(KeyEvent e) {
                }

                @Override
                public void keyPressed(KeyEvent e) {
                    location =  KeyEvent.getKeyText(e.getKeyCode());
                }

                @Override
                public void keyReleased(KeyEvent e) { }
            });

            requestFocusInWindow();
        }
      
        jf.add(this);
    }

    public boolean checkBox(int next_x, int next_y, int tank_w, int tank_h ) {
       
        for (wall w : arr) {
            int wall_x = w.getX();
            int wall_x_Max = wall_x + w.getW();
            int wall_y = w.getY();
            int wall_y_Max = wall_y + w.getH();

            int tank_x = next_x;
            int tank_y = next_y;
            int tank_x_Max = tank_x + tank_w;
            int tank_y_Max = next_y + tank_h;

            if (location.equalsIgnoreCase("w")) {
                if (tank_y <= wall_y_Max && tank_x_Max > wall_x && tank_x < wall_x_Max && tank_y_Max > wall_y) {
                    return false;
                }
                 
            }else if (location.equalsIgnoreCase("s")) {
                if (tank_x_Max > wall_x && tank_x < wall_x_Max && tank_y_Max >= wall_y && tank_y < wall_y_Max ) {
                    return false;
                }
            }else if (location.equalsIgnoreCase("d")) {
                if (tank_x_Max >= wall_x && tank_x < wall_x_Max && tank_y < wall_y_Max && tank_y_Max > wall_y  ) {
                    return false;
                }
            }else if (location.equalsIgnoreCase("a")){
                if (tank_x <= wall_x_Max && tank_x_Max > wall_x && tank_y < wall_y_Max && tank_y_Max > wall_y  ) {
                    return false;
                }
            }

        }
        return true;
    }

    private void useItem_hp(){
        if (hp + 30 >= maxHp) {
            hp = maxHp;
        }else{
            hp += 30;
        }
    }

    private void useShield(){
        setBackImage(shieldImage);
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            
            public void run() {
                if (shield_time > 0) {
                    shield_time--;
                }else{
                    setBackImage(mainImage);
                    return ;
                }
            }
        }, 0, 1000);
    }

    public int getHp() {
        return hp;
    }
    public void setHP(int damage){
        if (shield_time <= 0) {
            if ((hp - damage) <= 0) {
                kill();
                hp = 0;
            }else{
                hp -= damage; 
            }
        }
        
    }
    public void setMy_item(String my_item) {
        this.my_item = my_item;
    }
    public void Move(int x , int y){
        Server_cilent.toServer("move:"+x + ","+ y);
        this.setLocation(x, y);
        
    }
    private void kill(){
        run = false;
        GameConfig.payerArrayList.remove(this);
        jf.remove(this);
        jf.repaint();
        jf.revalidate();
    }
    public String getHwid() {
        return hwid;
    }
    public void shoot(JPanel bg_tranparant ,int enpoint_x , int enpoint_y ){
        if (bulletCount > 0) {
            if (bullet.equalsIgnoreCase("boom")) {
                ballBoom b = new ballBoom(bg_tranparant, enpoint_x, enpoint_y , this.getX() , this.getY() , this );
                this.bullet = "";
            }else{
                ball b = new ball(bg_tranparant, enpoint_x, enpoint_y , this.getX() , this.getY() , this );
            }

            bulletCount--;
            if (bulletCount == 0) {
                isrelode = true;
                Timer timer = new Timer();
                timer.schedule(new java.util.TimerTask() {
                    @Override
                    public void run() {
                        bulletCount = GameConfig.MaxBullet;
                        isrelode = false;
                        timer.cancel(); 
                    }
                }, 3000);
            }
        }

        

    }
    @Override
    public void run() {
        if (hwid.equalsIgnoreCase(Server_cilent.getHwid())) {
            this.setFocusable(true);
        }

        while(run) {
            int x = this.getX();
            int y = this.getY();
            if (max_window_y <= 0 && max_window_x <= 0) continue;
            if (location != null ) {
                if (checkBox(x, y, payer_size, payer_size)) {
                    if (location.equalsIgnoreCase("w") && y - 5 > 0 ) {
                        Move(x , y - 5);
                    }else if (location.equalsIgnoreCase("s") && y + 30 + payer_size <  max_window_y ){
                        Move(x , y + 5);
                    }else if (location.equalsIgnoreCase("a") && x - 5 > 0){
                        Move(x - 5 , y);
                    }else if (location.equalsIgnoreCase("d") && x + 20 + payer_size <  max_window_x){
                        Move(x + 5 , y);
                    }

                }


                

                location = null;
                this.revalidate();
                this.repaint();
            }
            spawn_items items = GameConfig.item;
            if (items != null && check_hitbox.isHit(this, items)) {
                my_item = items.getType();
                items.Kill();
                GameConfig.item = null;

            }

            if (my_item != null) {
                if (my_item.equalsIgnoreCase("hp")) {
                    my_item = null;
                    useItem_hp();
                    
                }else if (my_item.equalsIgnoreCase("boom_bullet")) {
                    my_item = null;
                    this.bullet = "boom";
                }else if(my_item.equalsIgnoreCase("shield")){
                    my_item = null;
                    shield_time = GameConfig.shield_time;
                    useShield();
                }
            }
           
            
            try {
                Thread.sleep(16); 
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void paintComponent(Graphics  g2){
        super.paintComponent(g2);

        int barWidth = payer_size;
        int barHeight = 6;
        int hpWidth = (int) ((double) hp / maxHp * barWidth);

        g2.setColor(Color.RED);
        g2.fillRect(0, 0, barWidth, barHeight);

        g2.setColor(Color.GREEN);
        g2.fillRect(0, 0, hpWidth, barHeight);

        g2.setColor(Color.BLACK);
        g2.drawRect(0, 0, barWidth, barHeight);

        if (isrelode) {
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRect(0, barHeight + 1, barWidth, 12);

        // ข้อความสีเหลืองขนาดเล็ก
        g2.setColor(Color.YELLOW);
        g2.setFont(g2.getFont().deriveFont(9.0f)); // ปรับขนาดฟอนต์เป็น 9pt
        g2.drawString("Reloading...", 2, barHeight + 10);
        }
    }
}
