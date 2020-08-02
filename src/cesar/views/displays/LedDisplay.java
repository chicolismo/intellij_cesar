package cesar.views.displays;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class LedDisplay extends JPanel {
    private static final long serialVersionUID = 7159709799229150768L;

    private static final int WIDTH = 15;
    private static final int HEIGHT = 15;
    private static final String LIGHT_ON_PATH = "/cesar/resources/images/light_on.png";
    private static final String LIGHT_OFF_PATH = "/cesar/resources/images/light_off.png";
    private static final BufferedImage[] IMAGES;

    static {
        IMAGES = new BufferedImage[2];
        try {
            IMAGES[0] = ImageIO.read(LedDisplay.class.getResourceAsStream(LIGHT_OFF_PATH));
            IMAGES[1] = ImageIO.read(LedDisplay.class.getResourceAsStream(LIGHT_ON_PATH));
        }
        catch (final IOException exception) {
            System.err.println("Erro ao ler as imagens do LED.");
            exception.printStackTrace(System.err);
            System.exit(1);
        }
    }

    private boolean isLightOn;

    public LedDisplay() {
        super(true);
        final Dimension size = new Dimension(WIDTH, HEIGHT);
        setSize(size);
        setPreferredSize(size);
        setMinimumSize(size);
        setMaximumSize(size);
        isLightOn = false;
    }

    @Override
    public void paintComponent(final Graphics g) {
        super.paintComponent(g);
        g.drawImage(isLightOn ? IMAGES[1] : IMAGES[0], 0, 0, WIDTH, HEIGHT, null);
    }

    public void setLightOn(final boolean value) {
        if (isLightOn != value) {
            isLightOn = value;
            repaint();
        }
    }

}
