package cesar.gui.panels;

import cesar.Properties;
import cesar.utils.Defaults;

import javax.swing.*;

public class StatusBar extends JPanel {
    private static final long serialVersionUID = 1408669317780545642L;
    private static final String INITIAL_VALUE = Properties.getProperty("StatusBar.initialValue");

    private final JLabel label;

    public StatusBar() {
        super(true);
        label = new JLabel(INITIAL_VALUE);
        label.setFont(Defaults.PANEL_FONT);
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        this.add(label);
        this.add(Box.createHorizontalGlue());
        setMinimumSize(getPreferredSize());
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    }

    public void clear() {
        label.setText(" ");
    }

    /**
     * Escreve uma mensagem temporária na barra de status.
     *
     * @param message A mensagem a ser escrita na barra de status.
     */
    public void setTempMessage(final String message) {
        final long milliseconds = 3000;
        final String currentText = getText();

        final Thread tempThread = new Thread(new Runnable() {
            @Override
            public void run() {
                setText(message);
                try {
                    Thread.sleep(milliseconds);
                }
                catch (final InterruptedException e) {
                    e.printStackTrace();
                }
                setText(currentText);
            }
        });
        tempThread.start();
    }

    public String getText() {
        return label.getText();
    }

    public void setText(final String text) {
        label.setText(text);
    }
}
