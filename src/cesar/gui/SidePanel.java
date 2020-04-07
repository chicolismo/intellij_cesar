package cesar.gui;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import cesar.gui.tables.Table;

public abstract class SidePanel extends JDialog {
    private static final long serialVersionUID = 3602114587032491724L;

    protected MainWindow parent;

    public SidePanel(MainWindow parent, String title) {
//        super(parent);
        setTitle(title);
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
