package cesar.views.tables;

import cesar.models.Cpu;

public class DataTableModel extends TableModel {
    private static final long serialVersionUID = -6517003845553744906L;

    public DataTableModel(final Cpu cpu) {
        super(cpu, new String[]{"Endereço", "Dado"}, new Class<?>[]{Integer.class, Byte.class});
    }

    @Override
    public String getAddressAsString(final int row) {
        return (String) getValueAt(row, 0);
    }

    @Override
    public String getValueAsString(final int row) {
        return (String) getValueAt(row, 1);
    }

    @Override
    public Object getValueAt(final int row, final int col) {
        return col == 0 ? formatNumber(row) : formatNumber(getByte(row));
    }
}
