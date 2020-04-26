package cesar.gui.panels;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import cesar.utils.Defaults;

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

    public void clear() {
        label.setText(" ");
    }

    public String getText() {
        return label.getText();
    }

    public void setText(final String text) {
        label.setText(text);
    }
}
