package cesar.gui;

import java.awt.Dimension;

import javax.swing.JScrollPane;

import cesar.gui.tables.ProgramTable;
import cesar.gui.tables.ProgramTableModel;

public class ProgramPanel extends SidePanel {
    private static final long serialVersionUID = 8452878222228144644L;

    private final ProgramTable table;
    private final ProgramTableModel tableModel;

    public ProgramPanel(MainWindow parent, byte[] data) {
        super(parent, "Programa");

        tableModel = new ProgramTableModel(data, new String[] { "PC", "Endereço", "Dado", "Mnemônico" });
        table = new ProgramTable(tableModel);

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
    public ProgramTable getTable() {
        return table;
    }
}
