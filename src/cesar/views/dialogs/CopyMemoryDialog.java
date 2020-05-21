package cesar.views.dialogs;

import javax.swing.JOptionPane;

import cesar.models.Base;
import cesar.utils.Integers;
import cesar.views.panels.StatusBar;
import cesar.views.windows.MainWindow;

public class CopyMemoryDialog {
    private static final String[] MESSAGES = new String[] { "Digite o endereço inicial da região a copiar:",
        "Digite o endereço final da região a copiar:", "Digite o endereço de destino:" };
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

    public boolean showCopyDialog(final Base base) {
        final int radix = base.toInt();

        String input = null;
        try {
            // Endereço inicial
            input = (String) JOptionPane.showInputDialog(parent, MESSAGES[0], DIALOG_TITLE, JOptionPane.PLAIN_MESSAGE,
                    null, null, Integer.toString(DEFAULT_START_ADDRESS, radix).toUpperCase());
            if (input == null) {
                return false;
            }
            startAddress = Integer.parseInt(input, radix);
            if (!Integers.isInInterval(startAddress, 0, 0xFFFF)) {
                statusBar.setTempMessage(String.format(ERROR_MESSAGE, input));
                return false;
            }

            // Endereço final
            input = (String) JOptionPane.showInputDialog(parent, MESSAGES[1], DIALOG_TITLE, JOptionPane.PLAIN_MESSAGE,
                    null, null, Integer.toString(DEFAULT_END_ADDRESS, radix).toUpperCase());
            if (input == null) {
                return false;
            }
            endAddress = Integer.parseInt(input, radix);
            if (!Integers.isInInterval(endAddress, startAddress, 0xFFFF)) {
                statusBar.setTempMessage(String.format(ERROR_MESSAGE, input));
                return false;
            }

            // Endereço de destino
            input = (String) JOptionPane.showInputDialog(parent, MESSAGES[2], DIALOG_TITLE, JOptionPane.PLAIN_MESSAGE,
                    null, null, Integer.toString(DEFAULT_START_ADDRESS, radix).toUpperCase());
            if (input == null) {
                return false;
            }
            dstAddress = Integer.parseInt(input, radix);
            if (!Integers.isInInterval(dstAddress, 0, 0xFFFF)) {
                statusBar.setTempMessage(String.format(ERROR_MESSAGE, input));
                return false;
            }
        }
        catch (final NumberFormatException exception) {
            statusBar.setTempMessage(String.format(ERROR_MESSAGE, input));
            return false;
        }
        return true;
    }

}
