import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

public class show extends JFrame {
    private JLabel label = new JLabel();
    private ImageIcon image;
    private int width, height;

    public show(String imgName) {
        try {
            BufferedImage sourceImage = ImageIO.read(new File(imgName));
            width = sourceImage.getWidth();
            height = sourceImage.getHeight();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        setSize(width, height);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null); // 将窗口置于屏幕中央

        image = new ImageIcon(imgName);
        Image img = image.getImage().getScaledInstance(width, height, Image.SCALE_DEFAULT);
        image.setImage(img);
        label.setIcon(image);

        add(label);
        setVisible(true);
    }
}
