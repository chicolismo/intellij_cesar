package cesar.views.utils;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;

public class Components {
    private Components() {
    }

    public static void centerComponent(final Component c) {
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        final Dimension componentSize = c.getSize();
        final int x = (int) ((screenSize.getWidth() - componentSize.getWidth()) / 2);
        final int y = (int) ((screenSize.getHeight() - componentSize.getHeight()) / 2);
        c.setLocation(x, y);
    }
}
