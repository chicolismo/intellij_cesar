package cesar.utils;

import cesar.Properties;

import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

public class Defaults {
    public static final Font PANEL_FONT;
    public static final Color ARROW_COLOR;

    public static final boolean APPLE = System.getProperty("os.name").equals("Mac OS X");

    static {
        PANEL_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 12);

        int rgb;
        try {
            if (APPLE) {
                rgb = Integer.parseInt(Properties.getProperty("ProgramWindow.pcArrowMacColor"));
            }
            else {
                rgb = Integer.parseInt(Properties.getProperty("ProgramWindow.pcArrowWinColor"));
            }
        }
        catch (final NumberFormatException e) {
            rgb = 0x009600;
        }
        ARROW_COLOR = new Color(rgb);
    }

    public static Border getEmptyBorder() {
        return createEmptyBorder(4);
    }

    public static Border createEmptyBorder(final int padding) {
        return BorderFactory.createEmptyBorder(padding, padding, padding, padding);
    }

    public static JLabel createLabel(final String title) {
        final JLabel label = new JLabel(title);
        label.setFont(PANEL_FONT);
        return label;
    }

    public static Border createTitledBorder(final String title) {
        return createTitledBorder(title, TitledBorder.LEFT);
    }

    public static Border createTitledBorder(final String title, final int align) {
        final TitledBorder border = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), title, align,
                TitledBorder.CENTER);
        border.setTitleFont(PANEL_FONT);
        return border;
    }

    public static boolean isApple() {
        return APPLE;
    }

    private Defaults() {
    }
}
