package cesar.gui.windows;

import javax.swing.JDialog;

import cesar.gui.displays.TextDisplay;
import cesar.hardware.Cpu;

public class TextWindow extends JDialog {
    private static final long serialVersionUID = -1260366562134430928L;

    private final TextDisplay display;

    public TextWindow(final MainWindow parent, final Cpu cpu) {
        super(parent, "Visor");
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
