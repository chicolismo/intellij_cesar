package cesar.gui.panels;

import cesar.gui.displays.LedDisplay;
import cesar.utils.Defaults;

import javax.swing.*;
import javax.swing.border.TitledBorder;

public class LedPanel extends JPanel {
    private static final long serialVersionUID = 1339366132381996962L;

    private final LedDisplay display;

    public LedPanel(String label) {
        display = new LedDisplay();
        display.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        display.setAlignmentY(JComponent.CENTER_ALIGNMENT);
        BoxLayout box = new BoxLayout(this, BoxLayout.X_AXIS);
        setLayout(box);
        add(Box.createHorizontalGlue());
        add(display);
        add(Box.createVerticalGlue());
        add(Box.createHorizontalGlue());
        setBorder(Defaults.createTitledBorder(label, TitledBorder.CENTER));
    }

    void setValue(boolean value) {
        display.setTurnedOn(value);
    }

}
