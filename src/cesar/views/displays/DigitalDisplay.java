package cesar.views.displays;

import cesar.models.Base;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class DigitalDisplay extends JPanel {
    private static final long serialVersionUID = 7750416402778310401L;

    private static final int DIGIT_WIDTH = 12;
    private static final int DIGIT_HEIGHT = 17;
    private static final int DIGIT_OFFSET = DIGIT_WIDTH + 2;
    private static final int WIDTH = 74 + 4;
    private static final int HEIGHT = 23 + 4;
    private static final int START_X = 59 + 2;
    private static final int START_Y = 3 + 2;

    private static final int N_DIGITS = 16;
    private static final String PATH_FORMAT = "/cesar/resources/images/cesar_%1x.png";
    private static final String NULL_IMAGE_PATH = "/cesar/resources/images/cesar_null.png";
    private static final BufferedImage[] DISPLAY_IMAGES;
    private static final BufferedImage DISPLAY_NULL;

    static {
        BufferedImage[] digits = new BufferedImage[N_DIGITS];
        BufferedImage emptyDigit = null;
        try {
            for (int i = 0; i < N_DIGITS; ++i) {
                digits[i] = ImageIO.read(DigitalDisplay.class.getResourceAsStream(String.format(PATH_FORMAT, i)));
            }
            emptyDigit = ImageIO.read(DigitalDisplay.class.getResourceAsStream(NULL_IMAGE_PATH));
        }
        catch (IllegalArgumentException | IOException e) {
            System.err.println("Erro a ler os dígitos");
            e.printStackTrace();
            System.exit(1);
        }
        DISPLAY_IMAGES = digits;
        DISPLAY_NULL = emptyDigit;
    }

    private int unsignedValue;
    private Base currentBase;
    private int numberOfDigits;

    public DigitalDisplay() {
        super(true);
        currentBase = Base.DECIMAL;
        numberOfDigits = 5;
        final Dimension size = new Dimension(WIDTH, HEIGHT);
        setSize(size);
        setPreferredSize(size);
        setMinimumSize(size);
        setMaximumSize(size);
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
            g.drawImage(DISPLAY_IMAGES[digit], x, START_Y, DIGIT_WIDTH, DIGIT_HEIGHT, null);
            x -= DIGIT_OFFSET;
            ++currentDigit;
            n /= base;
        } while (n > 0);

        while (currentDigit < numberOfDigits) {
            g.drawImage(DISPLAY_NULL, x, START_Y, DIGIT_WIDTH, DIGIT_HEIGHT, null);
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
