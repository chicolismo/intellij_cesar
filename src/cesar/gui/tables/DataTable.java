package cesar.gui.tables;

import javax.swing.table.TableColumn;

public class DataTable extends Table {
    private static final long serialVersionUID = -5256889056472626825L;

    public DataTable(final DataTableModel model) {
        super(model);
    }

    @Override
    void initColumnWidths() {
        final int[] columnWidths = new int[] { 62, 52 };
        for (int i = 0; i < columnWidths.length; ++i) {
            final TableColumn column = getColumnModel().getColumn(i);
            column.setMaxWidth(columnWidths[i]);
            column.setMinWidth(columnWidths[i]);
        }
    }
}
