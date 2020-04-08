package cesar.gui.tables;

import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class DataTable extends Table {
    private static final long serialVersionUID = -5256889056472626825L;

    public DataTable(DataTableModel model) {
        super(model);

        TableColumnModel columnModel = getColumnModel();

        TableColumn column = columnModel.getColumn(0);
        column.setMinWidth(60);

        column = columnModel.getColumn(1);
        column.setMinWidth(60);
    }
}
