package cesar.gui.tables;

import javax.swing.table.AbstractTableModel;

import cesar.hardware.Cpu;
import cesar.utils.Base;

public abstract class TableModel extends AbstractTableModel {
    private static final long serialVersionUID = -124231497089309828L;
    protected final Cpu cpu;

    protected String[] columnNames;
    protected Class<?>[] classNames;
    protected String formatString;
    protected Base currentBase;

    public TableModel(final Cpu cpu) {
        this.cpu = cpu;
        setBase(Base.DECIMAL);
    }

    @Override
    public Class<?> getColumnClass(final int column) {
        return classNames[column];
    }

    public Base getBase() {
        return currentBase;
    }

    public void setBase(final Base base) {
        currentBase = base;
        formatString = base == Base.DECIMAL ? "%d" : "%x";
        fireTableDataChanged();
    }

    protected String formatNumber(final byte number) {
        return String.format(formatString, 0xFF & number);
    }

    protected String formatNumber(final int number) {
        return String.format(formatString, number);
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

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    abstract public String getAddressAsString(final int row);

    abstract public String getValueAsString(final int row);

    public void setValue(final int row, final byte value) {
        cpu.setByte(row, value);
        fireTableRowsUpdated(row, row);
    }
}
