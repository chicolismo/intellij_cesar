package cesar.views.tables;

import javax.swing.table.TableColumn;

public class DataTable extends Table {
    private static final long serialVersionUID = -5256889056472626825L;
    private static final int[] COLUMN_WIDTHS = new int[]{62, 52};

    public DataTable(final DataTableModel model) {
        super(model);
    }

    @Override
    void initColumnWidths() {
        for (int i = 0; i < COLUMN_WIDTHS.length; ++i) {
            final TableColumn column = getColumnModel().getColumn(i);
//            column.setMaxWidth(COLUMN_WIDTHS[i]);
            column.setMinWidth(COLUMN_WIDTHS[i]);
            column.setWidth(COLUMN_WIDTHS[i]);
//            column.setResizable(false);
        }
    }
}
