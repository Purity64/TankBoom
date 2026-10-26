package config;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class ImageConfig {
    private final String[] image_path_arr = {"Tank1.png","Tank2.png","Tank3.png","Tank4.png","Tank5.png","Tank6.png","box.png","bullet.png" ,"big_bullet.png" , "Background.png" , "Tank1_shield.png","Tank2_shield.png","Tank3_shield.png","Tank4_shield.png","Tank5_shield.png","Tank6_shield.png"};
    
    public static final HashMap<String, BufferedImage> mapImg = new HashMap<>();
    
    public static final HashMap<String, ImageIcon> mapGif = new HashMap<>();

    public boolean lode_image() {
        for (String name : image_path_arr) {
            String path = "/images/" + name;
            try (InputStream is = getClass().getResourceAsStream(path)) {
                if (is != null) {
                    BufferedImage img = ImageIO.read(is);
                    mapImg.put(name, img);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        URL boomUrl = getClass().getResource("/images/boom.gif");
        if (boomUrl != null) {
            mapGif.put("boom.gif", new ImageIcon(boomUrl));
        }

        return true;
    }
}