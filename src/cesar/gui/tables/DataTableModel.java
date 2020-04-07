package cesar.gui.tables;

import cesar.gui.Base;

public class DataTableModel extends TableModel {
    private static final long serialVersionUID = -6517003845553744906L;

    public DataTableModel(byte[] data, String[] columnNames) {
        super(data, columnNames);
        classNames = new Class<?>[] { Integer.class, Byte.class };
    }

    @Override
    public Object getValueAt(int row, int col) {
        switch (col) {
            case 0:
                return String.format(currentBase == Base.DECIMAL ? "%d" : "%x", row);
            case 1:
            default:
                return String.format(currentBase == Base.DECIMAL ? "%d" : "%x", 0xFF & data[row]);
        }
    }
}
