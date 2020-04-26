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

        final BoxLayout verticalBox = new BoxLayout(this, BoxLayout.X_AXIS);
        setLayout(verticalBox);
        add(negative);
        add(Box.createHorizontalGlue());
        add(zero);
        add(Box.createHorizontalGlue());
        add(overflow);
        add(Box.createHorizontalGlue());
        add(carry);
    }

    public void setCarry(final boolean value) {
        carry.setValue(value);
    }

    public void setNegative(final boolean value) {
        negative.setValue(value);
    }

    public void setOverflow(final boolean value) {
        overflow.setValue(value);
    }

    public void setZero(final boolean value) {
        zero.setValue(value);
    }
}
