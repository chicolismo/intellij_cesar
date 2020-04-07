package cesar.utils;

import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

public class Defaults {
    public static final Font DEFAULT_FONT;

    static {
        DEFAULT_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
    }

    private Defaults() {
    }

    public static Border createEmptyBorder() {
        return createEmptyBorder(4);
    }

    public static Border createEmptyBorder(int padding) {
        return BorderFactory.createEmptyBorder(padding, padding, padding, padding);
    }

    public static Border createTitledBorder(String title) {
        return createTitledBorder(title, TitledBorder.LEFT);
    }

    public static Border createTitledBorder(String title, int align) {
        TitledBorder border = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), title, align,
            TitledBorder.CENTER);
        border.setTitleFont(DEFAULT_FONT);
        return border;
    }

    public static JLabel createLabel(String title) {
        JLabel label = new JLabel(title);
        label.setFont(DEFAULT_FONT);
        return label;
    }
}
