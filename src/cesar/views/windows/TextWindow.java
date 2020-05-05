package cesar.views.windows;

import javax.swing.JDialog;

import cesar.models.Cpu;
import cesar.utils.Properties;
import cesar.views.displays.TextDisplay;

public class TextWindow extends JDialog {
    private static final long serialVersionUID = -1260366562134430928L;

    private final TextDisplay display;

    public TextWindow(final MainWindow parent, final Cpu cpu) {
        super(parent, Properties.getProperty("TextWindow.title"));
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        setFocusable(true);
        display = new TextDisplay(cpu);
        add(display);
        pack();
        setResizable(false);
    }

    public TextDisplay getDisplay() {
        return display;
    }
}
