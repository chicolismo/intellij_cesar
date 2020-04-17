package cesar.gui.panels;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import cesar.gui.displays.LedDisplay;
import cesar.utils.Defaults;

public class LedPanel extends JPanel {
    private static final long serialVersionUID = 1339366132381996962L;

    private final LedDisplay display;

    public LedPanel(final String label) {
        display = new LedDisplay();
        display.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        display.setAlignmentY(JComponent.CENTER_ALIGNMENT);
        final BoxLayout box = new BoxLayout(this, BoxLayout.X_AXIS);
        setLayout(box);
        add(Box.createHorizontalGlue());
        add(display);
        add(Box.createVerticalGlue());
        add(Box.createHorizontalGlue());
        setBorder(Defaults.createTitledBorder(label, TitledBorder.CENTER));
    }

    void setValue(final boolean value) {
        display.setTurnedOn(value);
    }

}
