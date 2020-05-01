package cesar.gui.dialogs;

import java.awt.Component;

import javax.swing.JOptionPane;

import cesar.Properties;

public class GotoDialog {
    private static final String GOTO_DIALOG_TITLE = Properties.getProperty("Goto.title");
    private static final String GOTO_DIALOG_MESSAGE = Properties.getProperty("Goto.message");

    private final Component parent;

    public GotoDialog(final Component parent) {
        this.parent = parent;
    }

    public String showDialog(final String currentValue) {
        return (String) JOptionPane.showInputDialog(parent, GOTO_DIALOG_MESSAGE, GOTO_DIALOG_TITLE,
                JOptionPane.PLAIN_MESSAGE, null, null, currentValue);
    }
}
