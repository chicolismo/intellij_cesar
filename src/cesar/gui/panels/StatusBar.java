package cesar.gui.panels;

import cesar.utils.Defaults;

import javax.swing.*;

public class StatusBar extends JPanel {
    private static final long serialVersionUID = 1408669317780545642L;

    private final JLabel label;

    public StatusBar() {
        super(true);
        label = new JLabel();
        label.setFont(Defaults.DEFAULT_FONT);
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        this.add(label);
        this.add(Box.createHorizontalGlue());
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    }

    public void setText(final String text) {
        label.setText(text);
    }

    public String getText() {
        return label.getText();
    }

    public void clear() {
        label.setText(" ");
    }
}
