package cesar.views.displays;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

import cesar.models.Base;
import cesar.utils.Properties;

public class DigitalDisplay extends JPanel {
    private static final long serialVersionUID = 7750416402778310401L;

    private static final int DIGIT_WIDTH = 12;
    private static final int DIGIT_HEIGHT = 17;
    private static final int DIGIT_OFFSET = DIGIT_WIDTH + 2;
    private static final int WIDTH = 74 + 4;
    private static final int HEIGHT = 23 + 4;
    private static final int START_X = 59 + 2;
    private static final int START_Y = 3 + 2;

    private static final BufferedImage[] displayImages;
    private static final BufferedImage displayNull;

    static {
        final BufferedImage[] digits = new BufferedImage[16];
        BufferedImage emptyDigit = null;
        try {
            // final String pathFormat = "/cesar/gui/assets/cesar_%1x.png";
            final String pathFormat = Properties.getProperty("DigitalDisplay.imagePathFormat");
            for (int i = 0; i < 16; ++i) {
                digits[i] = ImageIO.read(DigitalDisplay.class.getResourceAsStream(String.format(pathFormat, i)));
            }
            emptyDigit = ImageIO.read(
                    DigitalDisplay.class.getResourceAsStream(Properties.getProperty("DigitalDisplay.nullImagePath")));
        }
        catch (IllegalArgumentException | IOException e) {
            System.err.println("Erro a ler os dígitos");
            e.printStackTrace();
            System.exit(1);
        }
        displayImages = digits;
        displayNull = emptyDigit;
    }

    private int unsignedValue;
    private Base currentBase;
    private int numberOfDigits;

    public DigitalDisplay() {
        super(true);
        currentBase = Base.DECIMAL;
        numberOfDigits = 5;
        final Dimension dim = new Dimension(WIDTH, HEIGHT);
        setSize(dim);
        setPreferredSize(dim);
        setMinimumSize(dim);
        setMaximumSize(dim);
        setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
    }

    @Override
    public void paintComponent(final Graphics g) {
        super.paintComponent(g);

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        int x = START_X;
        int currentDigit = 0;
        int n = unsignedValue;
        final int base = Base.toInt(currentBase);
        do {
            final int digit = n % base;
            g.drawImage(displayImages[digit], x, START_Y, DIGIT_WIDTH, DIGIT_HEIGHT, null);
            x -= DIGIT_OFFSET;
            ++currentDigit;
            n /= base;
        } while (n > 0);

        while (currentDigit < numberOfDigits) {
            g.drawImage(displayNull, x, START_Y, DIGIT_WIDTH, DIGIT_HEIGHT, null);
            x -= DIGIT_OFFSET;
            ++currentDigit;
        }
    }

    public void setBase(final Base newBase) {
        currentBase = newBase;
        numberOfDigits = currentBase == Base.DECIMAL ? 5 : 4;
    }

    public void setValue(final int unsignedValue) {
        if (this.unsignedValue != unsignedValue) {
            this.unsignedValue = unsignedValue;
            repaint();
        }
    }
}