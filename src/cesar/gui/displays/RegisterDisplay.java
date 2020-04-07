package cesar.gui.displays;

import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;

import cesar.gui.Base;
import cesar.utils.Defaults;
import cesar.utils.Shorts;

public class RegisterDisplay extends JPanel {
    private static final long serialVersionUID = 7050289063551512021L;

    private final DigitalDisplay digitalDisplay;
    private final BinaryDisplay binaryDisplay;
    private Base currentBase;
    private final int registerNumber;

    public RegisterDisplay(final int registerNumber, final String label) {
        super(true);
        this.currentBase = Base.DECIMAL;
        this.registerNumber = registerNumber;
        this.digitalDisplay = new DigitalDisplay();
        this.binaryDisplay = new BinaryDisplay();

        digitalDisplay.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        binaryDisplay.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(Box.createVerticalGlue());
        add(digitalDisplay);
        add(Box.createRigidArea(new Dimension(0, 2)));
        add(binaryDisplay);
        add(Box.createVerticalGlue());

        setBorder(Defaults.createTitledBorder(label));

        doLayout();
        setMinimumSize(getPreferredSize());
    }

    public void setBase(Base newBase) {
        if (currentBase != newBase) {
            currentBase = newBase;
            digitalDisplay.setBase(newBase);
            digitalDisplay.repaint();
        }
    }

    public void setValue(short value) {
        int unsignedValue = Shorts.toUnsignedInt(value);
        digitalDisplay.setValue(unsignedValue);
        binaryDisplay.setValue(unsignedValue);
    }

    public int getNumber() {
        return registerNumber;
    }
}
