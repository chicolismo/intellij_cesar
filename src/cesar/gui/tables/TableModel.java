package cesar.gui.tables;

import cesar.hardware.Cpu;
import cesar.utils.Base;
import cesar.utils.Bytes;
import cesar.utils.Defaults;

import javax.swing.table.AbstractTableModel;

public abstract class TableModel extends AbstractTableModel {
    private static final long serialVersionUID = -124231497089309828L;
    private final static String DECIMAL_FORMAT = "%d";

    private final static String HEXADECIMAL_FORMAT = "%x";

    private final Cpu cpu;
    private final String[] columnNames;
    private final Class<?>[] classNames;
    private String formatString;

    public TableModel(final Cpu cpu, final String[] columnNames, final Class<?>[] classNames) {
        this.cpu = cpu;
        this.columnNames = columnNames;
        this.classNames = classNames;
        setBase(Defaults.DEFAULT_BASE);
    }

    public void setBase(final Base base) {
        formatString = base == Base.DECIMAL ? DECIMAL_FORMAT : HEXADECIMAL_FORMAT;
        fireTableDataChanged();
    }

    abstract public String getAddressAsString(final int row);

    @Override
    public String getColumnName(final int col) {
        return columnNames[col];
    }

    @Override
    public Class<?> getColumnClass(final int column) {
        return classNames[column];
    }

    @Override
    public int getRowCount() {
        return Cpu.MEMORY_SIZE;
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    abstract public String getValueAsString(final int row);

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
}
