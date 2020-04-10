package cesar.gui.tables;

import javax.swing.*;
import javax.swing.table.JTableHeader;
import java.awt.*;

public abstract class Table extends JTable {
    private static final long serialVersionUID = -8733831578127444505L;

    public Table(TableModel model) {
        super(model);
        setFocusable(false);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        final JTableHeader header = getTableHeader();
        header.setReorderingAllowed(false);
    }
}
