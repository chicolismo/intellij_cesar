package cesar.views.tables;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

import cesar.utils.Defaults;

public class ProgramTable extends Table {
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

    private static final long serialVersionUID = -8843361396327035069L;

    private static final int[] COLUMN_WIDTHS = new int[] { 35, 62, 55, 135 };

    public ProgramTable(final ProgramTableModel model) {
        super(model);
        getColumnModel().getColumn(0).setCellRenderer(new FirstColumnRenderer());
    }

    @Override
    void initColumnWidths() {
        for (int i = 0; i < COLUMN_WIDTHS.length; ++i) {
            final TableColumn column = getColumnModel().getColumn(i);
            column.setMaxWidth(COLUMN_WIDTHS[i]);
            column.setMinWidth(COLUMN_WIDTHS[i]);
            column.setResizable(false);
        }
    }
}
