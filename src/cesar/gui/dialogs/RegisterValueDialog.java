package cesar.gui.dialogs;

import javax.swing.JOptionPane;

import cesar.Properties;
import cesar.gui.displays.RegisterDisplay;
import cesar.utils.Shorts;

public class RegisterValueDialog {
    public static class RegisterValueDialogException extends Exception {
        private static final long serialVersionUID = -5688453742853723066L;

        public RegisterValueDialogException(final String message) {
            super(message);
        }
    }

    private static final String NEW_REGISTER_VALUE_ERROR_FORMAT = Properties.getProperty("RegisterValue.errorFormat");

    public short showDialog(final RegisterDisplay display, final short registerValue)
            throws RegisterValueDialogException {
        final int radix = display.getBase().toInt();
        final String currentValue = Integer.toString(Shorts.toUnsignedInt(registerValue), radix);
        final String input = (String) JOptionPane.showInputDialog(display, display.getMessage(), display.getTitle(),
                JOptionPane.PLAIN_MESSAGE, null, null, currentValue);
        if (input == null) {
            return registerValue;
        }
        try {
            final short value = Shorts.fromInt(Integer.parseInt(input, radix));
            if (!Shorts.isValidShort(value)) {
                throw new RegisterValueDialogException(String.format(NEW_REGISTER_VALUE_ERROR_FORMAT, input));
            }
            return value;
        }
        catch (final NumberFormatException e) {
            throw new RegisterValueDialogException(String.format(NEW_REGISTER_VALUE_ERROR_FORMAT, input));
        }
    }
}
