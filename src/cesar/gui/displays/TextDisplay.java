package cesar.gui.displays;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import cesar.hardware.Cpu;

public class TextDisplay extends JPanel {
    private static final long serialVersionUID = 1744904008121167731L;

    private static final int SIZE = 36;
    private static final int CHAR_WIDTH = 20;
    private static final int CHAR_HEIGHT = 28;
    private static final int START_Y = 4;
    private static final int START_X = 1;
    private static final int CHAR_OFFSET = CHAR_WIDTH + 1;
    private static final int WIDTH = CHAR_OFFSET * SIZE + 2;
    private static final int HEIGHT = CHAR_HEIGHT + 8;
    private static final int NUMBER_OF_CHARACTERS = 95;
    private static final int START_ADDRESS = Cpu.BEGIN_DISPLAY_ADDRESS;

    private static final BufferedImage[] charImages;

    static {
        charImages = new BufferedImage[NUMBER_OF_CHARACTERS];
        final String format = "/cesar/gui/assets/character_%02d.png";
        try {
            for (int i = 0; i < NUMBER_OF_CHARACTERS; ++i) {
                charImages[i] = ImageIO.read(TextDisplay.class.getResourceAsStream(String.format(format, i)));
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
        for (int i = 0; i < SIZE; ++i) {
            // int index = (memory[START_ADDRESS + i] - 32);
            final int index = cpu.getByte(START_ADDRESS + i) - 32;
            if (index >= 0 && index < NUMBER_OF_CHARACTERS) {
                g.drawImage(charImages[index], x, START_Y, CHAR_WIDTH, CHAR_HEIGHT, null);
            }
            else {
                g.drawImage(charImages[0], x, START_Y, CHAR_WIDTH, CHAR_HEIGHT, null);
            }
            x += CHAR_OFFSET;
        }
    }
}
