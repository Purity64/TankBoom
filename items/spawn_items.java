package items;

import java.util.Random;

import javax.swing.JFrame;

import config.GameConfig;
import config.ImageConfig;
import utail.box_img;

/**
 * spawn_items
 */
public class spawn_items extends box_img  implements Runnable {
    private int time = 10;
    private boolean user = false;
    private String type = "";

    private JFrame mainFrame ;
    Random random = new Random();


    public spawn_items(JFrame mainFrame , int x , int y ,String type){
        super(ImageConfig.mapImg.get("box.png"));
        this.type = type;
        this.mainFrame = mainFrame;
        setBounds(x, y, GameConfig.item_size, GameConfig.item_size);

        mainFrame.add(this);
        UpdateUI();
    }



    public String getType() {
        return type;
    }

  

    public void UpdateUI(){
        mainFrame.repaint();
        mainFrame.revalidate();
    }

    public void Kill(){
        user = true;
        mainFrame.remove(this);
        UpdateUI();
    }

    @Override
    public void run() {
        while (!user && time > 0) {
            try {
                Thread.sleep(1000);
                time--;
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
        }
    }
    
    

    
}