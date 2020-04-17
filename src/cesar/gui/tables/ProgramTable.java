package cesar.gui.tables;

import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class ProgramTable extends Table {
    private static final long serialVersionUID = -8843361396327035069L;

    public ProgramTable(final ProgramTableModel model) {
        super(model);

        final TableColumnModel columnModel = getColumnModel();

        TableColumn column = columnModel.getColumn(0);
        column.setMaxWidth(30);
        column.setMinWidth(30);

        column = columnModel.getColumn(1);
        column.setMinWidth(60);
        column.setMaxWidth(60);

        column = columnModel.getColumn(2);
        column.setMinWidth(60);
        column.setMaxWidth(60);

        column = columnModel.getColumn(3);
        column.setMinWidth(130);
        column.setMaxWidth(130);
    }
}

