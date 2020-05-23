package cesar.views.dialogs;

import java.awt.Component;

import javax.swing.JOptionPane;

import cesar.models.Base;
import cesar.models.Cpu;
import cesar.utils.Defaults;
import cesar.utils.Properties;
import cesar.utils.Shorts;
import cesar.views.panels.StatusBar;
import cesar.views.windows.MainWindow;

public class GotoDialog {
    private static final String INVALID_MEMORY_POSITION_ERROR_FORMAT = Properties.getProperty(
            "Memory.invalidPositionErrorFormat");
    private static final String GOTO_DIALOG_TITLE = Properties.getProperty("Goto.title");
    private static final String GOTO_DIALOG_MESSAGE = Properties.getProperty("Goto.message");

    private final MainWindow parent;
    private Base currentBase;
    private final Cpu cpu;
    private final StatusBar statusBar;
    private int address;

    public GotoDialog(final MainWindow parent) {
        this.parent = parent;
        this.cpu = parent.getCpu();
        this.statusBar = parent.getStatusBar();
        currentBase = Defaults.DEFAULT_BASE;
    }

    public String getInput(final String currentValue) {
        return (String) JOptionPane.showInputDialog(parent, GOTO_DIALOG_MESSAGE, GOTO_DIALOG_TITLE,
                JOptionPane.PLAIN_MESSAGE, null, null, currentValue);
    }

    public int getAddress() {
        return address;
    }

    public void setBase(final Base newBase) {
        currentBase = newBase;
    }

    public boolean showDialog() {
        int radix = currentBase.toInt();
        boolean result = false;
        String currentValue = Integer.toString(cpu.getProgramCounter(), radix);
        final String input = getInput(currentValue);
        try {
            address = Integer.parseInt(input, radix);
            if (Cpu.isValidAddress(address)) {
                result = true;
            }
            else {
                statusBar.setTempMessage(String.format(INVALID_MEMORY_POSITION_ERROR_FORMAT, input));
            }
        }
        catch (final NumberFormatException event) {
            statusBar.setTempMessage(String.format(INVALID_MEMORY_POSITION_ERROR_FORMAT, input));
        }
        return result;
    }
}
