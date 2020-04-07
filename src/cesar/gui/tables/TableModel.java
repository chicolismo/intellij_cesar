package cesar.gui.tables;

import javax.swing.table.AbstractTableModel;

import cesar.gui.Base;

public abstract class TableModel extends AbstractTableModel {
    /**
     * 
     */
    private static final long serialVersionUID = -124231497089309828L;
    protected Base currentBase;
    protected String[] columnNames;
    protected byte[] data;
    protected Class<?>[] classNames;

    public TableModel(byte[] data, String[] columnNames) {
        this.currentBase = Base.DECIMAL;
        this.data = data;
        this.columnNames = columnNames;
    }

    @Override
    public Class<?> getColumnClass(int column) {
        return classNames[column];
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
        return data.length;
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }
}
