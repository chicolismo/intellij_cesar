package cesar.gui;

import cesar.gui.tables.DataTable;
import cesar.gui.tables.DataTableModel;
import cesar.hardware.Cpu;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class DataPanel extends SidePanel {
    public static final long serialVersionUID = -7816298913045696756L;
    private static final String LABEL_FORMAT = "[%s]";

    private final DataTable table;
    private final DataTableModel model;
    private final JLabel addressLabel;
    private final JTextField valueField;
    private int currentAddress;
    private byte currentValue;

    public DataPanel(MainWindow parent, Cpu cpu) {
        super(parent, "Dados");

        model = new DataTableModel(cpu, new String[]{"Endereço", "Dado"});
        table = new DataTable(model);

        currentAddress = 0;
        currentValue = 0;

        JScrollPane scrollPane = new JScrollPane(table);

        final Dimension tableSize = table.getPreferredSize();
        final int scrollBarWidth = 15;
        final Dimension scrollPaneSize = new Dimension(tableSize.width + scrollBarWidth, tableSize.height);
        scrollPane.setPreferredSize(scrollPaneSize);
        scrollPane.getVerticalScrollBar().setSize(new Dimension(scrollBarWidth, 0));

        add(scrollPane);

        addressLabel = new JLabel("[0]");
        valueField = new JTextField(6);
        valueField.setMinimumSize(valueField.getPreferredSize());

        final JPanel lowerPanel = new JPanel();
        final GridBagLayout layout = new GridBagLayout();
        layout.columnWeights = new double[]{1.0, 0.0, 0.0};
        layout.rowWeights = new double[]{1.0};
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
        initEvents();
    }

    @Override
    public DataTable getTable() {
        return table;
    }

    private void initEvents() {
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (table.getSelectedRow() >= 0) {
                    final int row = table.getSelectedRow();
                    String address = (String) model.getValueAt(row, 0);
                    String value = (String) model.getValueAt(row, 1);
                    addressLabel.setText(String.format(LABEL_FORMAT, address));
                    valueField.setText(value);
                    valueField.requestFocus();

                    final int radix = Base.toInt(model.getBase());
                    currentAddress = Integer.parseInt(address, radix);
                    currentValue = (byte) Integer.parseInt(value, radix);
                }
            }
        });
    }

    public void setBase(Base base) {
        final int radix = Base.toInt(base);
        model.setBase(base);
        addressLabel.setText(String.format(LABEL_FORMAT, Integer.toString(currentAddress, radix)));
        valueField.setText(Integer.toString(currentValue, radix));
    }
}
