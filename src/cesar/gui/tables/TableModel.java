package cesar.gui.tables;

import cesar.gui.Base;
import cesar.hardware.Cpu;

import javax.swing.table.AbstractTableModel;

public abstract class TableModel extends AbstractTableModel {
    /**
     *
     */
    private static final long serialVersionUID = -124231497089309828L;
    protected Base currentBase;
    protected String[] columnNames;
    // protected byte[] data;
    protected Cpu cpu;
    protected Class<?>[] classNames;

    public TableModel(Cpu cpu, String[] columnNames) {
        this.currentBase = Base.DECIMAL;
        // this.data = data;
        this.cpu = cpu;
        this.columnNames = columnNames;
    }

    @Override
    public Class<?> getColumnClass(int column) {
        return classNames[column];
    }

    public Base getBase() {
        return currentBase;
    }

    public void setBase(Base base) {
        currentBase = base;
        fireTableDataChanged();
    }

    @Override
    public String getColumnName(int col) {
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
}
