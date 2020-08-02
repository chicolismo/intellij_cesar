package cesar.views.dialogs;

import cesar.models.Base;
import cesar.models.Cpu;
import cesar.views.panels.StatusBar;
import cesar.views.windows.MainWindow;

import javax.swing.*;

public class ZeroMemoryDialog {
    private static final String ERROR_FORMAT = "ERRO: Posição da memória inválida (%s)";
    private static final String TITLE = "Zerar memória";
    private static final String INITIAL_ADDRESS_MESSAGE = "Digite o endereço inicial";
    private static final String FINAL_ADDRESS_MESSAGE = "Digite o endereço final";

    private final MainWindow parent;
    private final int minimumAddress;
    private final int maximumAddress;
    private final StatusBar statusBar;
    private Base currentBase;
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

    public void setBase(final Base base) {
        currentBase = base;
    }

    public boolean showZeroMemoryDialog() {
        final int radix = currentBase.toInt();
        String userInput = null;
        try {
            userInput = getUserInput(INITIAL_ADDRESS_MESSAGE, Integer.toString(minimumAddress, radix));
            if (userInput == null) {
                return false;
            }

            startAddress = Integer.parseInt(userInput, radix);
            if (!Cpu.isValidAddress(startAddress)) {
                statusBar.setTempMessage(String.format(ERROR_FORMAT, userInput));
                return false;
            }

            userInput = getUserInput(FINAL_ADDRESS_MESSAGE, Integer.toString(maximumAddress, radix));
            if (userInput == null) {
                return false;
            }

            endAddress = Integer.parseInt(userInput, radix);
            if (!Cpu.isValidAddress(endAddress) || endAddress < startAddress) {
                statusBar.setTempMessage(String.format(ERROR_FORMAT, userInput));
                return false;
            }
        }
        catch (final NumberFormatException event) {
            statusBar.setTempMessage(String.format(ERROR_FORMAT, userInput));
        }
        return true;
    }

    private String getUserInput(final String message, final String initialValue) {
        return (String) JOptionPane
                .showInputDialog(parent, message, ZeroMemoryDialog.TITLE, JOptionPane.PLAIN_MESSAGE, null, null,
                        initialValue);
    }
}
