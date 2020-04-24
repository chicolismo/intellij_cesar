package cesar.gui.windows;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.Box;
import javax.swing.JPanel;

import cesar.gui.tables.DataTable;
import cesar.gui.tables.DataTableModel;
import cesar.hardware.Cpu;

public class DataWindow extends SideWindow<DataTable, DataTableModel> {
    public static final long serialVersionUID = -7816298913045696756L;

    public DataWindow(final MainWindow parent, final Cpu cpu) {
        super(parent, "Dados", cpu);
        initLayout();
    }

    @Override
    protected void initLayout() {
        super.initLayout();

        final JPanel lowerPanel = new JPanel(getGridLayout(new double[] { 1.0 }, new double[] { 1.0, 0.0, 0.0 }));

        final GridBagConstraints c_0 = new GridBagConstraints();
        c_0.ipadx = 4;
        c_0.gridx = 0;
        c_0.gridy = 0;
        c_0.anchor = GridBagConstraints.WEST;
        lowerPanel.add(Box.createHorizontalGlue(), c_0);

        final GridBagConstraints c_1 = new GridBagConstraints();
        c_1.ipadx = 4;
        c_1.gridx = 1;
        c_1.gridy = 0;
        c_1.anchor = GridBagConstraints.EAST;
        lowerPanel.add(addressLabel, c_1);

        final GridBagConstraints c_2 = new GridBagConstraints();
        c_2.ipadx = 4;
        c_2.gridx = 2;
        c_2.gridy = 0;
        c_2.anchor = GridBagConstraints.EAST;
        lowerPanel.add(valueField, c_2);

        add(Box.createVerticalStrut(4));
        add(lowerPanel);

        pack();
    }

    @Override
    protected void initTable(final Cpu cpu) {
        model = new DataTableModel(cpu);
        table = new DataTable(model);
    }
}