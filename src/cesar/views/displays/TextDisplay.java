package cesar.views.displays;

import cesar.models.Cpu;
import cesar.utils.Bytes;
import cesar.utils.Integers;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class TextDisplay extends JPanel {
    private static final long serialVersionUID = 1744904008121167731L;
    private static final int DISPLAY_SIZE = 36;
    private static final int CHAR_WIDTH = 20;
    private static final int CHAR_HEIGHT = 28;
    private static final int START_Y = 4;
    private static final int START_X = 1;
    private static final int CHAR_OFFSET = CHAR_WIDTH + 1;
    private static final int WIDTH = CHAR_OFFSET * DISPLAY_SIZE + 2;
    private static final int HEIGHT = CHAR_HEIGHT + 8;
    private static final int N_CHARS = 95;
    private static final int ASCII_DIFFERENCE = 32;
    private static final String CHAR_FORMAT = "/cesar/resources/images/character_%02d.png";
    private static final BufferedImage[] CHAR_IMAGES;

    static {
        CHAR_IMAGES = new BufferedImage[N_CHARS];
        try {
            for (int i = 0; i < N_CHARS; ++i) {
                CHAR_IMAGES[i] = ImageIO.read(TextDisplay.class.getResourceAsStream(String.format(CHAR_FORMAT, i)));
            }
        }
        catch (final IOException e) {
            System.err.println("Erro ao tentar ler as imagens dos TextDisplay.");
            e.printStackTrace();
            System.exit(1);
        }
    }

    private final Cpu cpu;

    public TextDisplay(final Cpu cpu) {
        this.cpu = cpu;
        final Dimension size = new Dimension(WIDTH, HEIGHT);
        setSize(size);
        setMinimumSize(size);
        setMaximumSize(size);
        setPreferredSize(size);
        repaint();
    }

    @Override
    public void paintComponent(final Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, WIDTH, HEIGHT);
        int x = START_X;
        for (int i = 0; i < DISPLAY_SIZE; ++i) {
            g.drawImage(getCharImage(cpu.getDisplayByte(i)), x, START_Y, CHAR_WIDTH, CHAR_HEIGHT, null);
            x += CHAR_OFFSET;
        }
    }

    private static BufferedImage getCharImage(final byte byteValue) {
        final int index = Bytes.toUnsignedInt(byteValue) - ASCII_DIFFERENCE;
        if (Integers.isInInterval(index, 0, N_CHARS, false)) {
            return CHAR_IMAGES[index];
        }
        else {
            return CHAR_IMAGES[0];
        }
    }
}
