package cesar.gui;

import cesar.gui.tables.ProgramTable;
import cesar.gui.tables.ProgramTableModel;
import cesar.hardware.Cpu;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ProgramPanel extends SidePanel {
    public static final long serialVersionUID = 8452878222228144644L;

    private static final String LABEL_FORMAT = "[%s]";

    private final ProgramTable table;
    private final ProgramTableModel model;
    private final JTextField bpField;
    private final JLabel addressLabel;
    private final JTextField valueField;
    private int currentAddress;
    private byte currentValue;

    public ProgramPanel(MainWindow parent, Cpu cpu) {
        super(parent, "Programa");

        model = new ProgramTableModel(cpu, new String[]{"PC", "Endereço", "Dado", "Mnemônico"});
        table = new ProgramTable(model);

        currentAddress = 0;
        currentValue = 0;

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
        bpField.setMinimumSize(bpField.getPreferredSize());
        addressLabel = new JLabel("[0]");
        valueField = new JTextField(6);
        valueField.setMinimumSize(valueField.getPreferredSize());

        final JPanel lowerPanel = new JPanel();
        final GridBagLayout layout = new GridBagLayout();
        layout.columnWeights = new double[]{0.0, 0.0, 1.0, 0.0, 0.0};
        layout.rowWeights = new double[]{1.0};
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

        initEvents();
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

    private void initEvents() {
        // TODO: Tratar o caso de quando o valor do campo de texto é alterado e o
        // usuário aperta [ENTER].
        // O valor da memória no endereço correspondente deve ser alterado, e a próxima
        // linha da
        // tabela deve ser selecionada como se houvesse sido clicada pelo mouse.
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (table.getSelectedRow() >= 0) {
                    final int row = table.getSelectedRow();
                    String address = (String) model.getValueAt(row, 1);
                    String value = (String) model.getValueAt(row, 2);
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
