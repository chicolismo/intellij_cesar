package cesar.views.dialogs;

import cesar.models.Base;
import cesar.utils.Integers;
import cesar.views.panels.StatusBar;
import cesar.views.windows.MainWindow;

import javax.swing.*;

public class CopyMemoryDialog {
    private static final String[] MESSAGES =
            new String[]{"Digite o endereço inicial da região a copiar:", "Digite o endereço final da região a copiar:",
                    "Digite o endereço de destino:"};
    private static final String ERROR_MESSAGE = "ERRO: Posição de memória inválida (%s)";
    private static final String DIALOG_TITLE = "Copiar memória";
    private static final int DEFAULT_START_ADDRESS = 0;
    private static final int DEFAULT_END_ADDRESS = 0xFFFF;

    private final MainWindow parent;
    private final StatusBar statusBar;
    private int startAddress;
    private int endAddress;
    private int dstAddress;

    public CopyMemoryDialog(final MainWindow parent) {
        this.parent = parent;
        statusBar = parent.getStatusBar();
    }

    public int getDstAddress() {
        return dstAddress;
    }

    public int getEndAddress() {
        return endAddress;
    }

    public int getStartAddress() {
        return startAddress;
    }

    public boolean showDialog(final Base base) {
        final int radix = base.toInt();

        String userInput = null;
        try {
            // Endereço inicial
            userInput = (String) JOptionPane
                    .showInputDialog(parent, MESSAGES[0], DIALOG_TITLE, JOptionPane.PLAIN_MESSAGE, null, null,
                            Integer.toString(DEFAULT_START_ADDRESS, radix).toUpperCase());
            if (userInput == null) {
                return false;
            }
            startAddress = Integer.parseInt(userInput, radix);
            if (!Integers.isInInterval(startAddress, 0, 0xFFFF)) {
                statusBar.setTempMessage(String.format(ERROR_MESSAGE, userInput));
                return false;
            }

            // Endereço final
            userInput = (String) JOptionPane
                    .showInputDialog(parent, MESSAGES[1], DIALOG_TITLE, JOptionPane.PLAIN_MESSAGE, null, null,
                            Integer.toString(DEFAULT_END_ADDRESS, radix).toUpperCase());
            if (userInput == null) {
                return false;
            }
            endAddress = Integer.parseInt(userInput, radix);
            if (!Integers.isInInterval(endAddress, startAddress, 0xFFFF)) {
                statusBar.setTempMessage(String.format(ERROR_MESSAGE, userInput));
                return false;
            }

            // Endereço de destino
            userInput = (String) JOptionPane
                    .showInputDialog(parent, MESSAGES[2], DIALOG_TITLE, JOptionPane.PLAIN_MESSAGE, null, null,
                            Integer.toString(DEFAULT_START_ADDRESS, radix).toUpperCase());
            if (userInput == null) {
                return false;
            }
            dstAddress = Integer.parseInt(userInput, radix);
            if (!Integers.isInInterval(dstAddress, 0, 0xFFFF)) {
                statusBar.setTempMessage(String.format(ERROR_MESSAGE, userInput));
                return false;
            }
        }
        catch (final NumberFormatException exception) {
            statusBar.setTempMessage(String.format(ERROR_MESSAGE, userInput));
            return false;
        }
        return true;
    }

}
