package cesar.gui.tables;

import cesar.gui.Base;

public class ProgramTableModel extends TableModel {
    public static final long serialVersionUID = -5373447997057887767L;

    private int pcRow;

    public ProgramTableModel(byte[] data, String[] columnNames) {
        super(data, columnNames);
        classNames = new Class<?>[] { String.class, Integer.class, Byte.class, String.class };
        pcRow = 0;
    }

    @Override
    public Object getValueAt(int row, int column) {
        switch (column) {
            case 0:
                return (row == pcRow) ? " \u279C" : "";
            case 1:
                return String.format(currentBase == Base.DECIMAL ? "%d" : "%x", row);
            case 2:
                return String.format(currentBase == Base.DECIMAL ? "%d" : "%x", 0xFF & data[row]);
            case 3:
            default:
                return " Não implementado";
        }
    }
}
