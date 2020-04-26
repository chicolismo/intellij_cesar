package cesar.gui.tables;

import javax.swing.table.AbstractTableModel;

import cesar.hardware.Cpu;
import cesar.utils.Base;

public abstract class TableModel extends AbstractTableModel {
    private static final long serialVersionUID = -124231497089309828L;
    protected final Cpu cpu;

    private final String[] columnNames;
    private final Class<?>[] classNames;
    private String formatString;
    private Base currentBase;

    public TableModel(final Cpu cpu, final String[] columnNames, final Class<?>[] classNames) {
        this.cpu = cpu;
        this.columnNames = columnNames;
        this.classNames = classNames;
        setBase(Base.DECIMAL);
    }

    protected String formatNumber(final byte number) {
        return String.format(formatString, 0xFF & number).toUpperCase();
    }

    protected String formatNumber(final int number) {
        return String.format(formatString, number).toUpperCase();
    }

    abstract public String getAddressAsString(final int row);

    public Base getBase() {
        return currentBase;
    }

    @Override
    public Class<?> getColumnClass(final int column) {
        return classNames[column];
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(final int col) {
        return columnNames[col];
    }

    @Override
    public int getRowCount() {
        return Cpu.MEMORY_SIZE;
        // return cpu.getMemory().length;
    }

    abstract public String getValueAsString(final int row);

    public void setBase(final Base base) {
        currentBase = base;
        formatString = base == Base.DECIMAL ? "%d" : "%x";
        fireTableDataChanged();
    }

    public void setValue(final int row, final byte value) {
        cpu.setByte(row, value);
        fireTableRowsUpdated(row, row);
    }
}
