package cesar.gui.tables;

import javax.swing.table.AbstractTableModel;

import cesar.hardware.Cpu;
import cesar.utils.Base;
import cesar.utils.Bytes;

public abstract class TableModel extends AbstractTableModel {
    private static final long serialVersionUID = -124231497089309828L;
    private final static String DECIMAL_FORMAT = "%d";

    private final static String HEXADECIMAL_FORMAT = "%x";

    private final Cpu cpu;
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
        formatString = base == Base.DECIMAL ? DECIMAL_FORMAT : HEXADECIMAL_FORMAT;
        fireTableDataChanged();
    }

    public void setValue(final int row, final byte value) {
        cpu.setByte(row, value);
        fireTableRowsUpdated(row, row);
    }

    protected String formatNumber(final byte number) {
        return String.format(formatString, Bytes.toUnsignedInt(number)).toUpperCase();
    }

    protected String formatNumber(final int number) {
        return String.format(formatString, number).toUpperCase();
    }

    protected byte getByte(final int address) {
        return cpu.getByte(address);
    }

    protected String getMnemonic(final int address) {
        return cpu.getMnemonic(address);
    }

    protected void setByte(final int address, final byte value) {
        cpu.setByte(address, value);
    }
}
