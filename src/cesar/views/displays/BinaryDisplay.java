package cesar.views.displays;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class BinaryDisplay extends JPanel {
    private static final long serialVersionUID = -5490249529061282417L;
    private static final int IMAGE_WIDTH = 5;
    private static final int IMAGE_HEIGHT = 5;
    private static final int IMAGE_OFFSET = 5;
    private static final int WIDTH = 5 * 16;
    private static final int HEIGHT = 5;
    private static final int START_X = 75;
    private static final int START_Y = 0;
    private static final int BITS = 16;
    private static final BufferedImage[] LED_IMAGES = new BufferedImage[2];
    private static final String LED_0_PATH = "/cesar/resources/images/mini_led_0.png";
    private static final String LED_1_PATH = "/cesar/resources/images/mini_led_0.png";

    static {
        try {
            LED_IMAGES[0] = ImageIO.read(BinaryDisplay.class.getResourceAsStream(LED_0_PATH));
            LED_IMAGES[1] = ImageIO.read(BinaryDisplay.class.getResourceAsStream(LED_1_PATH));
        }
        catch (final IOException e) {
            System.err.println("Erro ao carregar os mini leds");
            e.printStackTrace();
            System.exit(1);
        }
    }

    private int unsignedValue;

    public BinaryDisplay() {
        super(true);
        final Dimension dim = new Dimension(WIDTH, HEIGHT);
        setSize(dim);
        setPreferredSize(dim);
        setMaximumSize(dim);
        setMinimumSize(dim);
        unsignedValue = 0;
    }

    @Override
    public void paintComponent(final Graphics g) {
        super.paintComponent(g);
        int n = unsignedValue;
        int x = START_X;
        int currentDigit = 0;

        while (n != 0) {
            ++currentDigit;
            final int index = n & 1;
            g.drawImage(LED_IMAGES[index], x, START_Y, IMAGE_WIDTH, IMAGE_HEIGHT, null);
            x -= IMAGE_OFFSET;
            n >>= 1;
        }

        while (currentDigit < BITS) {
            ++currentDigit;
            g.drawImage(LED_IMAGES[0], x, START_Y, IMAGE_WIDTH, IMAGE_HEIGHT, null);
            x -= IMAGE_OFFSET;
        }
    }

    public void setValue(final int newValue) {
        if (unsignedValue != newValue) {
            unsignedValue = newValue;
            repaint();
        }
    }
}
