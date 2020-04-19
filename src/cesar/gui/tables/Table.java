package cesar.gui.tables;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.*;

public abstract class Table extends JTable {
    private static final long serialVersionUID = -8733831578127444505L;
    private final int rowHeight;

    public Table(final TableModel model) {
        super(model);
        setFocusable(false);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setShowGrid(false);
        setShowVerticalLines(true);
        setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        getTableHeader().setReorderingAllowed(false);
        rowHeight = getRowHeight();
        initColumnWidths();
    }

    abstract void initColumnWidths();

    @Override
    public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        JComponent c = (JComponent) super.prepareRenderer(renderer, row, column);
        c.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
        return c;
    }

    public void scrollToRow(final int row) {
        scrollToRow(row, false);
    }

    public void scrollToRow(final int row, final boolean onTop) {
        final Rectangle rect;
        if (onTop) {
            rect = new Rectangle(0, (row - 1) * rowHeight + getParent().getHeight(), getWidth(), rowHeight);
        }
        else {
            rect = getCellRect(row, 0, true);
        }
        scrollRectToVisible(rect);
    }
}
