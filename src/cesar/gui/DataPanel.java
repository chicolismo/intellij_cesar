package cesar.gui;

import java.awt.Dimension;

import javax.swing.JScrollPane;

import cesar.gui.tables.DataTable;
import cesar.gui.tables.DataTableModel;

public class DataPanel extends SidePanel {
    private static final long serialVersionUID = -7816298913045696756L;

    private final DataTable table;
    private final DataTableModel model;

    public DataPanel(MainWindow parent, byte[] data) {
        super(parent, "Dados");

        model = new DataTableModel(data, new String[] { "Endereço", "Dado" });
        table = new DataTable(model);

        JScrollPane scrollPane = new JScrollPane(table);

        final Dimension tableSize = table.getPreferredSize();
        final int scrollBarWidth = 15;
        final int maxHeight = 500 > tableSize.height ? tableSize.height : 500;
        final Dimension scrollPaneSize = new Dimension(tableSize.width + scrollBarWidth, maxHeight);
        scrollPane.setPreferredSize(scrollPaneSize);
        scrollPane.getVerticalScrollBar().setSize(new Dimension(scrollBarWidth, 0));

        add(scrollPane);
        pack();
    }

    @Override
    public DataTable getTable() {
        return table;
    }
}
