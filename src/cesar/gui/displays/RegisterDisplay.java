package cesar.gui.displays;

import cesar.utils.Base;
import cesar.utils.Defaults;
import cesar.utils.Shorts;

import javax.swing.*;
import java.awt.*;

public class RegisterDisplay extends JPanel {
    private static final long serialVersionUID = 7050289063551512021L;

    private final DigitalDisplay digitalDisplay;
    private final BinaryDisplay binaryDisplay;

    private final String newValueTitle;
    private final String newValueMessage;

    private final int registerNumber;
    private Base currentBase;

    public RegisterDisplay(final int registerNumber, final String label, final String newValueTitle,
            final String newValueMessage) {
        super(true);

        this.newValueTitle = newValueTitle;
        this.newValueMessage = newValueMessage;

        currentBase = Base.DECIMAL;
        this.registerNumber = registerNumber;
        digitalDisplay = new DigitalDisplay();
        binaryDisplay = new BinaryDisplay();

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

    public Base getBase() {
        return currentBase;
    }

    public void setBase(final Base newBase) {
        if (currentBase != newBase) {
            currentBase = newBase;
            digitalDisplay.setBase(newBase);
            digitalDisplay.repaint();
        }
    }

    public String getMessage() {
        return newValueMessage;
    }

    public int getNumber() {
        return registerNumber;
    }

    public String getTitle() {
        return newValueTitle;
    }

    public void setValue(final short value) {
        final int unsignedValue = Shorts.toUnsignedInt(value);
        digitalDisplay.setValue(unsignedValue);
        binaryDisplay.setValue(unsignedValue);
    }
}
