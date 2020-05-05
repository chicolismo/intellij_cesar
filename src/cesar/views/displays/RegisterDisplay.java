package cesar.views.displays;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import cesar.models.Base;
import cesar.utils.Defaults;
import cesar.utils.Shorts;

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

        digitalDisplay.setAlignmentX(Component.CENTER_ALIGNMENT);
        binaryDisplay.setAlignmentX(Component.CENTER_ALIGNMENT);
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

    public String getMessage() {
        return newValueMessage;
    }

    public int getNumber() {
        return registerNumber;
    }

    public String getTitle() {
        return newValueTitle;
    }

    public void setBase(final Base newBase) {
        if (currentBase != newBase) {
            currentBase = newBase;
            digitalDisplay.setBase(newBase);
            digitalDisplay.repaint();
        }
    }

    public void setValue(final short value) {
        final int unsignedValue = Shorts.toUnsignedInt(value);
        digitalDisplay.setValue(unsignedValue);
        binaryDisplay.setValue(unsignedValue);
    }
}
