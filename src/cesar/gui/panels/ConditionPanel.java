package cesar.gui.panels;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

public class ConditionPanel extends JPanel {
    public static final long serialVersionUID = -595687570078206074L;

    private final LedPanel negative;
    private final LedPanel zero;
    private final LedPanel overflow;
    private final LedPanel carry;

    public ConditionPanel() {
        negative = new LedPanel("N");
        zero = new LedPanel("Z");
        overflow = new LedPanel("V");
        carry = new LedPanel("C");

        final BoxLayout vbox = new BoxLayout(this, BoxLayout.X_AXIS);
        setLayout(vbox);
        add(negative);
        add(Box.createHorizontalGlue());
        add(zero);
        add(Box.createHorizontalGlue());
        add(overflow);
        add(Box.createHorizontalGlue());
        add(carry);
    }

    public void setNegative(boolean value) {
        negative.setValue(value);
    }

    public void setZero(boolean value) {
        zero.setValue(value);
    }

    public void setOverflow(boolean value) {
        overflow.setValue(value);
    }

    public void setCarry(boolean value) {
        carry.setValue(value);
    }
}
