package cesar.gui.dialogs;

import cesar.Properties;
import cesar.utils.Base;

import javax.swing.*;
import java.awt.*;

public class ZeroMemoryDialog {
    private static final String ZERO_MEMORY_TITLE = Properties.getProperty("ZeroMemory.title");
    private static final String ZERO_MEMORY_START_MESSAGE = Properties.getProperty("ZeroMemory.startMessage");
    private static final String ZERO_MEMORY_END_MESSAGE = Properties.getProperty("ZeroMemory.endMessage");

    private final Component parent;

    private final int minimumAddress;
    private final int maximumAddress;
    private Base currentBase;

    public ZeroMemoryDialog(final Component parent, final int minimumAddress, final int maximumAddress) {
        this.parent = parent;
        this.minimumAddress = minimumAddress;
        this.maximumAddress = maximumAddress;
        currentBase = Base.DECIMAL;
    }

    public void setBase(final Base base) {
        currentBase = base;
    }

    public String showEndAddressDialog() {
        final int radix = currentBase.toInt();
        return getUserInput(parent, ZERO_MEMORY_END_MESSAGE, Integer.toString(maximumAddress, radix));
    }

    private static String getUserInput(final Component parent, final String message, final String initialValue) {
        return (String) JOptionPane.showInputDialog(parent, message, ZeroMemoryDialog.ZERO_MEMORY_TITLE,
                JOptionPane.PLAIN_MESSAGE, null, null, initialValue);
    }

    public String showStartAddressDialog() {
        final int radix = currentBase.toInt();
        return getUserInput(parent, ZERO_MEMORY_START_MESSAGE, Integer.toString(minimumAddress, radix));
    }
}
