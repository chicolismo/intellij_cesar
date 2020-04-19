package cesar.gui.tables;

import cesar.utils.Defaults;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;

public class ProgramTable extends Table {
    private static final long serialVersionUID = -8843361396327035069L;

    public ProgramTable(final ProgramTableModel model) {
        super(model);

        getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            private static final long serialVersionUID = -1783966912103512015L;

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int col) {
                JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                c.setForeground(Defaults.ARROW_COLOR);
                c.setHorizontalAlignment(JLabel.CENTER);
                c.setBorder(Defaults.createEmptyBorder(0));
                return c;
            }
        });
    }

    @Override
    void initColumnWidths() {
        final int[] COLUMN_WIDTHS = new int[] { 30, 62, 62, 130 };
        for (int i = 0; i < COLUMN_WIDTHS.length; ++i) {
            final TableColumn column = getColumnModel().getColumn(i);
            column.setMaxWidth(COLUMN_WIDTHS[i]);
            column.setMinWidth(COLUMN_WIDTHS[i]);
        }
    }
}

