package cesar.views.windows;

import cesar.models.Cpu;
import cesar.views.tables.DataTable;
import cesar.views.tables.DataTableModel;

import javax.swing.*;
import java.awt.*;


public class DataWindow extends SideWindow<DataTable, DataTableModel> {
    public static final long serialVersionUID = -7816298913045696756L;
    private static final String TITLE = "Dados";

    public DataWindow(final MainWindow parent, final Cpu cpu) {
        super(parent, TITLE, cpu);
        initLayout();
    }

    @Override
    protected void initTable(final Cpu cpu) {
        model = new DataTableModel(cpu);
        table = new DataTable(model);
    }

    @Override
    protected void initLayout() {
        super.initLayout();

        final JPanel lowerPanel = new JPanel(getGridLayout(new double[]{1.0}, new double[]{1.0, 0.0, 0.0}));

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
}
