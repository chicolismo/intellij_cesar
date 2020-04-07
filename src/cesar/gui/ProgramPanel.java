package cesar.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import cesar.gui.tables.ProgramTable;
import cesar.gui.tables.ProgramTableModel;

public class ProgramPanel extends SidePanel {
    private static final long serialVersionUID = 8452878222228144644L;

    private final ProgramTable table;
    private final ProgramTableModel tableModel;
    private final JTextField bpField;
    private final JLabel addressLabel;
    private final JTextField valueField;

    public ProgramPanel(MainWindow parent, byte[] data) {
        super(parent, "Programa");

        tableModel = new ProgramTableModel(data, new String[] { "PC", "Endereço", "Dado", "Mnemônico" });
        table = new ProgramTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(table);

        final Dimension tableSize = table.getPreferredSize();
        final int scrollBarWidth = 15;
        final Dimension scrollPaneSize = new Dimension(tableSize.width + scrollBarWidth, tableSize.height);
        scrollPane.setPreferredSize(scrollPaneSize);
        scrollPane.getVerticalScrollBar().setSize(new Dimension(scrollBarWidth, 0));

        add(scrollPane);

        final JLabel bpLabel = new JLabel("BP:");
        bpLabel.setForeground(Color.RED);
        bpField = new JTextField(4);
        addressLabel = new JLabel("[0]");
        valueField = new JTextField(5);

        final JPanel lowerPanel = new JPanel();
        final GridBagLayout layout = new GridBagLayout();
        layout.columnWeights = new double[] { 0.0, 0.0, 1.0, 0.0, 0.0 };
        layout.rowWeights = new double[] { 1.0 };
        lowerPanel.setLayout(layout);

        final GridBagConstraints c = new GridBagConstraints();
        c.ipadx = 4;
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        lowerPanel.add(bpLabel, c);

        c.gridx = 1;
        lowerPanel.add(bpField, c);

        c.gridx = 2;
        lowerPanel.add(Box.createHorizontalGlue(), c);

        c.gridx = 3;
        c.anchor = GridBagConstraints.EAST;
        lowerPanel.add(addressLabel, c);

        c.gridx = 4;
        lowerPanel.add(valueField, c);

        add(Box.createVerticalStrut(4));
        add(lowerPanel);

        pack();
    }

    @Override
    public ProgramTable getTable() {
        return table;
    }

    public JLabel getAddressLabel() {
        return addressLabel;
    }

    public JTextField getValueField() {
        return valueField;
    }

    public JTextField getBreakPointField() {
        return bpField;
    }
}
