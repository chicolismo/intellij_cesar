package cesar.gui.windows;

import cesar.gui.tables.Table;

import javax.swing.*;

public abstract class SideWindow extends JDialog {
    public static final long serialVersionUID = 3602114587032491724L;

    protected MainWindow parent;

    public SideWindow(MainWindow parent, String title) {
        super(parent, title);
        setFocusable(false);
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

        this.parent = parent;
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        BoxLayout layout = new BoxLayout(panel, BoxLayout.Y_AXIS);
        panel.setLayout(layout);
        setContentPane(panel);
    }

    abstract public Table getTable();
}
