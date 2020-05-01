package cesar.gui.tables;

import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class DataTable extends Table {
    private static final long serialVersionUID = -5256889056472626825L;
    private static final int[] COLUMN_WIDTHS = new int[] { 62, 52 };

    public DataTable(final DataTableModel model) {
        super(model);
    }

    @Override
    void initColumnWidths() {
        final TableColumnModel columnModel = getColumnModel();
        for (int i = 0; i < COLUMN_WIDTHS.length; ++i) {
            final TableColumn column = columnModel.getColumn(i);
            column.setMaxWidth(COLUMN_WIDTHS[i]);
            column.setMinWidth(COLUMN_WIDTHS[i]);
            column.setResizable(false);
        }
    }
}
