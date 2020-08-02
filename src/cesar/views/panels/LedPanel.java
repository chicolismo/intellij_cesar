package cesar.views.panels;

import cesar.utils.Defaults;
import cesar.views.displays.LedDisplay;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class LedPanel extends JPanel {
    private static final long serialVersionUID = 1339366132381996962L;

    private final LedDisplay display;

    public LedPanel(final String label) {
        display = new LedDisplay();
        display.setAlignmentX(Component.CENTER_ALIGNMENT);
        display.setAlignmentY(Component.CENTER_ALIGNMENT);
        initLayout();
        setBorder(Defaults.createTitledBorder(label, TitledBorder.CENTER));
    }

    private void initLayout() {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        add(Box.createHorizontalGlue());
        add(display);
        add(Box.createVerticalGlue());
        add(Box.createHorizontalGlue());
    }

    void setValue(final boolean value) {
        display.setLightOn(value);
    }

}
