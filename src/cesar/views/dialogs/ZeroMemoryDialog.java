package cesar.views.dialogs;

import java.awt.Component;

import javax.swing.JOptionPane;

import cesar.models.Base;
import cesar.models.Cpu;
import cesar.utils.Properties;
import cesar.views.panels.StatusBar;
import cesar.views.windows.MainWindow;

public class ZeroMemoryDialog {
    private static final String INVALID_MEMORY_POSITION_ERROR_FORMAT = Properties.getProperty(
            "Memory.invalidPositionErrorFormat");
    private static final String ZERO_MEMORY_TITLE = Properties.getProperty("ZeroMemory.title");
    private static final String ZERO_MEMORY_START_MESSAGE = Properties.getProperty("ZeroMemory.startMessage");
    private static final String ZERO_MEMORY_END_MESSAGE = Properties.getProperty("ZeroMemory.endMessage");

    private final MainWindow parent;

    private final int minimumAddress;
    private final int maximumAddress;
    private Base currentBase;
    private final StatusBar statusBar;
    private int startAddress, endAddress;

    public ZeroMemoryDialog(final MainWindow parent) {
        this(parent, Cpu.FIRST_ADDRESS, Cpu.KEYBOARD_INPUT_ADDRESS);
    }

    public ZeroMemoryDialog(final MainWindow parent, final int minimumAddress, final int maximumAddress) {
        this.parent = parent;
        statusBar = parent.getStatusBar();
        this.minimumAddress = minimumAddress;
        this.maximumAddress = maximumAddress;
        currentBase = Base.DECIMAL;
    }

    public int getStartAddress() {
        return startAddress;
    }

    public int getEndAddress() {
        return endAddress;
    }

    private String getUserInput(final String message, final String initialValue) {
        return (String) JOptionPane.showInputDialog(parent, message, ZeroMemoryDialog.ZERO_MEMORY_TITLE,
                JOptionPane.PLAIN_MESSAGE, null, null, initialValue);
    }

    public void setBase(final Base base) {
        currentBase = base;
    }

    public boolean showZeroMemoryDialog() {
        boolean result = true;
        final int radix = currentBase.toInt();
        String input = null;
        try {
            input = getUserInput(ZERO_MEMORY_START_MESSAGE, Integer.toString(minimumAddress, radix));
            if (input == null) {
                result = false;
            }
            else {
                startAddress = Integer.parseInt(input, radix);
                if (!Cpu.isValidAddress(startAddress)) {
                    statusBar.setTempMessage(String.format(INVALID_MEMORY_POSITION_ERROR_FORMAT, input));
                    result = false;
                }
                else {
                    input = getUserInput(ZERO_MEMORY_END_MESSAGE, Integer.toString(maximumAddress, radix));
                    if (input == null) {
                        result = false;
                    }
                    else {
                        endAddress = Integer.parseInt(input, radix);
                        if (!Cpu.isValidAddress(endAddress) || endAddress < startAddress) {
                            statusBar.setTempMessage(String.format(INVALID_MEMORY_POSITION_ERROR_FORMAT, input));
                            result = false;
                        }
                    }
                }
            }
        }
        catch (final NumberFormatException event) {
            statusBar.setTempMessage(String.format(INVALID_MEMORY_POSITION_ERROR_FORMAT, input));
        }
        return result;
    }
}
