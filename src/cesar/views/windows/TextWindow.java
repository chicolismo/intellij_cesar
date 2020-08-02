package cesar.views.windows;

import cesar.models.Cpu;
import cesar.views.displays.TextDisplay;

import javax.swing.*;

public class TextWindow extends JDialog {
    private static final long serialVersionUID = -1260366562134430928L;
    private static final String TITLE = "Visor";

    private final TextDisplay display;

    public TextWindow(final MainWindow parent, final Cpu cpu) {
        super(parent, TITLE);
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
