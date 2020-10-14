package cesar.views.displays;

import cesar.models.Base;
import cesar.utils.Defaults;
import cesar.utils.Integers;
import cesar.utils.Shorts;

import javax.swing.*;
import java.awt.*;

public class RegisterDisplay extends JPanel {
    private static final long serialVersionUID = 7050289063551512021L;
    private static final String NEW_REGISTER_VALUE_ERROR_FORMAT = "ERRO: Valor inválido (%s)";

    private final DigitalDisplay digitalDisplay;
    private final BinaryDisplay binaryDisplay;

    private final String newValueTitle;
    private final String newValueMessage;
    private final int registerNumber;
    private short value;
    private boolean error;
    private String errorMessage;
    private Base currentBase;

    public RegisterDisplay(final int registerNumber, final String label, final String newValueTitle,
                           final String newValueMessage) {
        super(true);

        value = 0;
        error = false;
        errorMessage = "";

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

    public void setBase(final Base newBase) {
        if (currentBase != newBase) {
            currentBase = newBase;
            digitalDisplay.setBase(newBase);
            digitalDisplay.repaint();
        }
    }

    public int getNumber() {
        return registerNumber;
    }

    public short getValueAsShort() {
        return Integers.clampToShort(value);
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean hasError() {
        return error;
    }

    public boolean showDialog() {
        error = false;
        boolean result = true;
        String input = getInput();
        if (input != null) {
            try {
                int temp = Integer.parseInt(input, currentBase.toInt());
                if (Shorts.isValidShort(temp)) {
                    setValue(Shorts.fromInt(temp));
                }
            }
            catch (final NumberFormatException e) {
                error = true;
                errorMessage = String.format(NEW_REGISTER_VALUE_ERROR_FORMAT, input);
                result = false;
            }
        }
        else {
            result = false;
        }
        return result;
    }

    private String getInput() {
        return (String) JOptionPane
                .showInputDialog(this, getMessage(), getTitle(), JOptionPane.PLAIN_MESSAGE, null, null,
                        Integer.toString(value, currentBase.toInt()));
    }

    public void setValue(final short newValue) {
        value = newValue;
        int intValue = Shorts.toUnsignedInt(newValue);
        digitalDisplay.setValue(intValue);
        binaryDisplay.setValue(intValue);
    }

    public String getMessage() {
        return newValueMessage;
    }

    public String getTitle() {
        return newValueTitle;
    }
}
