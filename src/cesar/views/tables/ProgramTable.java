package cesar.views.tables;

import cesar.utils.Defaults;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;

public class ProgramTable extends Table {
    private static final long serialVersionUID = -8843361396327035069L;

    private static final int[] COLUMN_WIDTHS = new int[]{35, 62, 40, 160};

    public ProgramTable(final ProgramTableModel model) {
        super(model);
        getColumnModel().getColumn(0).setCellRenderer(new FirstColumnRenderer());
    }

    @Override
    void initColumnWidths() {
        TableColumn column = getColumnModel().getColumn(0);
        column.setMaxWidth(COLUMN_WIDTHS[0]);
        column.setMinWidth(COLUMN_WIDTHS[0]);
        for (int i = 1; i < COLUMN_WIDTHS.length; ++i) {
            column = getColumnModel().getColumn(i);
            column.setWidth(COLUMN_WIDTHS[i]);
            column.setMinWidth(COLUMN_WIDTHS[i]);
//            column.setResizable(false);
            column.setResizable(true);
        }
    }

    private static class FirstColumnRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = -1687928120154541133L;

        @Override
        public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected,
                                                       final boolean hasFocus, final int row, final int col) {
            final JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            c.setForeground(Defaults.ARROW_COLOR);
            c.setHorizontalAlignment(SwingConstants.CENTER);
            c.setBorder(Defaults.createEmptyBorder(0));
            return c;
        }
    }
}
