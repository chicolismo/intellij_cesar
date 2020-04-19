package cesar.utils;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class Defaults {
    public static final Font DEFAULT_FONT;
    public static final Color ARROW_COLOR;

    static {
        DEFAULT_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 12);

        if (System.getProperty("os.name").equals("Mac OS X")) {
            ARROW_COLOR = new Color(0, 96, 0);
        }
        else {
            ARROW_COLOR = Color.GREEN;
        }
    }

    private Defaults() {
    }

    public static Border createEmptyBorder() {
        return createEmptyBorder(4);
    }

    public static Border createEmptyBorder(final int padding) {
        return BorderFactory.createEmptyBorder(padding, padding, padding, padding);
    }

    public static Border createTitledBorder(final String title) {
        return createTitledBorder(title, TitledBorder.LEFT);
    }

    public static Border createTitledBorder(final String title, final int align) {
        final TitledBorder border = BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), title, align, TitledBorder.CENTER);
        border.setTitleFont(DEFAULT_FONT);
        return border;
    }

    public static JLabel createLabel(final String title) {
        final JLabel label = new JLabel(title);
        label.setFont(DEFAULT_FONT);
        return label;
    }
}
