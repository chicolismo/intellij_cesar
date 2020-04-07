package cesar.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import cesar.gui.tables.DataTable;
import cesar.gui.tables.DataTableModel;

public class DataPanel extends SidePanel {
    private static final long serialVersionUID = -7816298913045696756L;

    private final DataTable table;
    private final DataTableModel model;
    private final JLabel addressLabel;
    private final JTextField valueField;

    public DataPanel(MainWindow parent, byte[] data) {
        super(parent, "Dados");

        model = new DataTableModel(data, new String[] { "Endereço", "Dado" });
        table = new DataTable(model);

        JScrollPane scrollPane = new JScrollPane(table);

        final Dimension tableSize = table.getPreferredSize();
        final int scrollBarWidth = 15;
        final Dimension scrollPaneSize = new Dimension(tableSize.width + scrollBarWidth, tableSize.height);
        scrollPane.setPreferredSize(scrollPaneSize);
        scrollPane.getVerticalScrollBar().setSize(new Dimension(scrollBarWidth, 0));

        add(scrollPane);

        addressLabel = new JLabel("[0]");
        valueField = new JTextField(5);

        final JPanel lowerPanel = new JPanel();
        final GridBagLayout layout = new GridBagLayout();
        layout.columnWeights = new double[] { 1.0, 0.0, 0.0 };
        layout.rowWeights = new double[] { 1.0 };
        lowerPanel.setLayout(layout);

        final GridBagConstraints c = new GridBagConstraints();
        c.ipadx = 4;
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        lowerPanel.add(Box.createHorizontalGlue(), c);

        c.gridx = 1;
        c.anchor = GridBagConstraints.EAST;
        lowerPanel.add(addressLabel, c);

        c.gridx = 2;
        lowerPanel.add(valueField, c);

        add(Box.createVerticalStrut(4));
        add(lowerPanel);

        pack();
    }

    @Override
    public DataTable getTable() {
        return table;
    }
}
