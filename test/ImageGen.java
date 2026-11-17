import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Random;
import javax.imageio.ImageIO;

/**
 * เครื่องมือวาด sprite สิ่งกีดขวางสำหรับเกม TankBoom
 * สไตล์: การ์ตูนพาสเทลหวานๆ ตรงธีมกับ tank แมวชมพู — วาดด้วย Java2D แล้วเซฟเป็น PNG โปร่งใส
 * รัน: java -cp bin ImageGen  (จาก root ของโปรเจค)
 */
public class ImageGen {
    static final int SIZE = 220; // ขนาด canvas (จะถูกย่อ/ขยายตามกำแพงจริงตอนเล่น)
    static final Color PINK = new Color(244, 154, 194);
    static final Color PINK_LIGHT = new Color(252, 208, 226);
    static final Color CREAM = new Color(252, 236, 214);
    static final Color MINT = new Color(168, 216, 185);
    static final Color MINT_LIGHT = new Color(214, 240, 222);
    static final Color KHAKI = new Color(214, 190, 148);
    static final Color KHAKI_LIGHT = new Color(236, 218, 184);
    static final Color WOOD = new Color(198, 148, 100);
    static final Color WOOD_LIGHT = new Color(226, 182, 138);
    static final Color OUTLINE = new Color(120, 80, 100);
    static final Color WHITE_SOFT = new Color(255, 250, 250);

    public static void main(String[] args) throws Exception {
        File dir = new File("images");
        dir.mkdirs();
        drawRock(new File(dir, "obstacle_rock.png"));
        drawTree(new File(dir, "obstacle_tree.png"));
        drawCrate(new File(dir, "obstacle_crate.png"));
        drawSandbag(new File(dir, "obstacle_sandbag.png"));
        System.out.println("OK — สร้าง 4 ภาพเรียบร้อยใน images/");
    }

    static BufferedImage newCanvas() {
        BufferedImage img = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
        return img;
    }

    static Graphics2D start(BufferedImage img) {
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        return g;
    }

    /** หัวใจจุดเล็กๆ ตกแต่ง */
    static void heart(Graphics2D g, int x, int y, int s, Color c) {
        g.setColor(c);
        Ellipse2D.Double l = new Ellipse2D.Double(x - s / 2.0, y - s / 2.0, s, s);
        Ellipse2D.Double r = new Ellipse2D.Double(x, y - s / 2.0, s, s);
        g.fill(l);
        g.fill(r);
        Path2D.Double tri = new Path2D.Double();
        tri.moveTo(x - s / 2.0, y + s * 0.25);
        tri.lineTo(x + s * 1.5, y + s * 0.25);
        tri.lineTo(x + s * 0.75, y + s);
        tri.closePath();
        g.fill(tri);
    }

    /** ดาวจุดเล็กๆ */
    static void star(Graphics2D g, int cx, int cy, int r, Color c) {
        Path2D.Double p = new Path2D.Double();
        for (int i = 0; i < 10; i++) {
            double ang = Math.PI * 2 * i / 10 - Math.PI / 2;
            double rad = (i % 2 == 0) ? r : r * 0.45;
            double px = cx + Math.cos(ang) * rad;
            double py = cy + Math.sin(ang) * rad;
            if (i == 0) p.moveTo(px, py); else p.lineTo(px, py);
        }
        p.closePath();
        g.setColor(c);
        g.fill(p);
    }

    /** วาดก้อนหลักพร้อมไฮไลต์และเส้นขอบแบบการ์ตูน */
    static void blob(Graphics2D g, Shape s, Color base, Color outline) {
        g.setColor(base);
        g.fill(s);
        g.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(outline);
        g.draw(s);
    }

    // ------------------------------------------------------------------
    // 1. กองหินกลมๆ พาสเทล
    // ------------------------------------------------------------------
    static void drawRock(File out) throws Exception {
        BufferedImage img = newCanvas();
        Graphics2D g = start(img);

        // หินหลักกลาง
        blob(g, new Ellipse2D.Double(30, 60, 150, 130), PINK_LIGHT, OUTLINE);
        // หินรองซ้อน
        blob(g, new Ellipse2D.Double(110, 30, 90, 80), MINT_LIGHT, OUTLINE);
        blob(g, new Ellipse2D.Double(20, 140, 80, 65), CREAM, OUTLINE);
        blob(g, new Ellipse2D.Double(125, 140, 75, 62), PINK, OUTLINE);

        // ไฮไลต์มันเงา
        g.setColor(WHITE_SOFT);
        g.fill(new Ellipse2D.Double(55, 80, 40, 24));
        g.fill(new Ellipse2D.Double(140, 48, 26, 14));

        // ตกแต่งหัวใจกับดาว
        heart(g, 105, 110, 16, PINK);
        heart(g, 45, 175, 11, PINK_LIGHT);
        star(g, 160, 170, 11, new Color(255, 214, 102));
        star(g, 75, 45, 9, new Color(255, 214, 102));

        g.dispose();
        ImageIO.write(img, "png", out);
    }

    // ------------------------------------------------------------------
    // 2. พุ่มต้นไม้มองจากบน (ใบไม้ + ดอกชมพู)
    // ------------------------------------------------------------------
    static void drawTree(File out) throws Exception {
        BufferedImage img = newCanvas();
        Graphics2D g = start(img);

        blob(g, new Ellipse2D.Double(15, 40, 170, 150), MINT, OUTLINE);
        blob(g, new Ellipse2D.Double(110, 15, 95, 85), MINT_LIGHT, OUTLINE);
        blob(g, new Ellipse2D.Double(60, 130, 100, 80), MINT_LIGHT, OUTLINE);

        // ใบเนื้อแน่นตรงกลาง
        g.setColor(new Color(140, 198, 165));
        g.fill(new Ellipse2D.Double(70, 70, 90, 80));

        // ดอกไม้ชมพู 5 กลีบ
        for (int[] flower : new int[][]{{115, 105}, {50, 70}, {170, 60}, {90, 170}}) {
            int fx = flower[0], fy = flower[1];
            g.setColor(PINK_LIGHT);
            for (int i = 0; i < 5; i++) {
                double a = Math.PI * 2 * i / 5;
                g.fill(new Ellipse2D.Double(fx + Math.cos(a) * 10 - 6, fy + Math.sin(a) * 10 - 6, 12, 12));
            }
            g.setColor(new Color(255, 214, 102));
            g.fill(new Ellipse2D.Double(fx - 4, fy - 4, 8, 8));
        }

        g.setColor(WHITE_SOFT);
        g.fill(new Ellipse2D.Double(40, 55, 34, 18));

        g.dispose();
        ImageIO.write(img, "png", out);
    }

    // ------------------------------------------------------------------
    // 3. ลังไม้ + กล่องของขวัญริบบิ้นชมพู
    // ------------------------------------------------------------------
    static void drawCrate(File out) throws Exception {
        BufferedImage img = newCanvas();
        Graphics2D g = start(img);

        // ลังไม้ใหญ่ (มองจากบน = ฝาลัง)
        blob(g, new RoundRectangle2D.Double(15, 45, 115, 115, 14, 14), WOOD_LIGHT, OUTLINE);
        // เส้นไม้บนฝา
        g.setStroke(new BasicStroke(5f));
        g.setColor(WOOD);
        g.draw(new RoundRectangle2D.Double(28, 58, 89, 89, 8, 8));
        g.draw(new Line2D.Double(28, 103, 117, 103));
        g.draw(new Line2D.Double(72, 58, 72, 147));

        // กล่องของขวัญชมพู
        blob(g, new RoundRectangle2D.Double(120, 25, 85, 85, 12, 12), PINK, OUTLINE);
        // ริบบิ้นกางเขน
        g.setColor(WHITE_SOFT);
        g.setStroke(new BasicStroke(9f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
        g.draw(new Line2D.Double(162, 25, 162, 110));
        g.draw(new Line2D.Double(120, 67, 205, 67));
        // โบว์
        g.setColor(WHITE_SOFT);
        g.fillOval(148, 14, 13, 10);
        g.fillOval(163, 14, 13, 10);
        g.setColor(PINK_LIGHT);
        g.fill(new Ellipse2D.Double(156, 17, 11, 9));

        // กล่องมินต์มุมล่าง
        blob(g, new RoundRectangle2D.Double(125, 125, 75, 75, 12, 12), MINT_LIGHT, OUTLINE);
        g.setColor(MINT);
        g.setStroke(new BasicStroke(6f));
        g.draw(new Line2D.Double(125, 162, 200, 162));

        heart(g, 55, 100, 14, PINK);
        star(g, 100, 78, 9, new Color(255, 214, 102));

        g.dispose();
        ImageIO.write(img, "png", out);
    }

    // ------------------------------------------------------------------
    // 4. กองทราย (แนวป้องกัน) — ถุงทรายซ้อนเป็นแถว
    // ------------------------------------------------------------------
    static void drawSandbag(File out) throws Exception {
        BufferedImage img = newCanvas();
        Graphics2D g = start(img);

        // แถวบน 3 ถุง
        blob(g, new Ellipse2D.Double(18, 30, 80, 62), KHAKI, OUTLINE);
        blob(g, new Ellipse2D.Double(78, 22, 82, 66), KHAKI_LIGHT, OUTLINE);
        blob(g, new Ellipse2D.Double(140, 30, 76, 62), KHAKI, OUTLINE);
        // แถวล่าง 3 ถุงเยื้อง
        blob(g, new Ellipse2D.Double(35, 95, 80, 64), KHAKI_LIGHT, OUTLINE);
        blob(g, new Ellipse2D.Double(98, 92, 82, 66), KHAKI, OUTLINE);
        blob(g, new Ellipse2D.Double(160, 98, 55, 58), KHAKI_LIGHT, OUTLINE);
        // แถวสามชิ้นล่างสุด
        blob(g, new Ellipse2D.Double(60, 160, 78, 52), KHAKI, OUTLINE);
        blob(g, new Ellipse2D.Double(122, 158, 60, 54), KHAKI_LIGHT, OUTLINE);

        // เงาเส้นบนถุงทราย
        g.setStroke(new BasicStroke(4f));
        g.setColor(new Color(180, 155, 115));
        g.draw(new Arc2D.Double(30, 42, 56, 30, 20, 140, Arc2D.OPEN));
        g.draw(new Arc2D.Double(95, 35, 50, 28, 20, 140, Arc2D.OPEN));

        // ตกแต่งหัวใจบนถุงกลาง
        heart(g, 138, 122, 13, PINK);
        star(g, 72, 130, 9, new Color(255, 214, 102));

        g.dispose();
        ImageIO.write(img, "png", out);
    }
}
