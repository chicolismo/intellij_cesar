package cesar.views.dialogs;

import cesar.models.Base;
import cesar.models.Cpu;
import cesar.utils.Defaults;
import cesar.views.panels.StatusBar;
import cesar.views.windows.MainWindow;

import javax.swing.*;

public class GotoDialog {
    private static final String INVALID_MEMORY_POSITION_ERROR_FORMAT = "ERRO: Posição da memória inválida (%s)";

    private static final String GOTO_DIALOG_TITLE = "Ir para";
    private static final String GOTO_DIALOG_MESSAGE = "Digite a posição de memória a ir";

    private final MainWindow parent;
    private final Cpu cpu;
    private final StatusBar statusBar;
    private Base currentBase;
    private int address;

    public GotoDialog(final MainWindow parent) {
        this.parent = parent;
        this.cpu = parent.getCpu();
        this.statusBar = parent.getStatusBar();
        currentBase = Defaults.DEFAULT_BASE;
    }

    public int getAddress() {
        return address;
    }

    public void setBase(final Base newBase) {
        currentBase = newBase;
    }

    public boolean showDialog() {
        final int radix = currentBase.toInt();
        String currentValue = Integer.toString(cpu.getProgramCounter(), radix);
        final String input = getInput(currentValue);
        boolean success = false;
        try {
            address = Integer.parseInt(input, radix);
            if (Cpu.isValidAddress(address)) {
                success = true;
            }
            else {
                statusBar.setTempMessage(String.format(INVALID_MEMORY_POSITION_ERROR_FORMAT, input));
            }
        }
        catch (final NumberFormatException event) {
            statusBar.setTempMessage(String.format(INVALID_MEMORY_POSITION_ERROR_FORMAT, input));
        }
        return success;
    }

    public String getInput(final String currentValue) {
        return (String) JOptionPane
                .showInputDialog(parent, GOTO_DIALOG_MESSAGE, GOTO_DIALOG_TITLE, JOptionPane.PLAIN_MESSAGE, null, null,
                        currentValue);
    }
}
