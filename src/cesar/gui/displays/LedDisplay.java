package cesar.gui.displays;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class LedDisplay extends JPanel {
    private static final long serialVersionUID = 7159709799229150768L;

    private static final int WIDTH = 15;
    private static final int HEIGHT = 15;
    private static final BufferedImage[] images;

    static {
        images = new BufferedImage[2];
        try {
            images[0] = ImageIO.read(LedDisplay.class.getResourceAsStream("/cesar/gui/assets/light_off.png"));
            images[1] = ImageIO.read(LedDisplay.class.getResourceAsStream("/cesar/gui/assets/light_on.png"));
        }
        catch (final IOException e) {
            System.err.println("Erro ao ler as imagens do LED.");
            e.printStackTrace();
        }
    }

    private boolean isTurnedOn;

    public LedDisplay() {
        super(true);
        final Dimension dim = new Dimension(WIDTH, HEIGHT);
        setSize(dim);
        setPreferredSize(dim);
        setMinimumSize(dim);
        setMaximumSize(dim);
        isTurnedOn = false;
    }

    public void setTurnedOn(final boolean value) {
        if (isTurnedOn != value) {
            isTurnedOn = value;
            repaint();
        }
    }

    @Override
    public void paintComponent(final Graphics g) {
        super.paintComponent(g);
        g.drawImage(isTurnedOn ? images[1] : images[0], 0, 0, WIDTH, HEIGHT, null);
    }

}
