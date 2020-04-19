package cesar.gui.tables;

import cesar.hardware.Cpu;

public class ProgramTableModel extends TableModel {
    public static final long serialVersionUID = -5373447997057887767L;
    private static final String ARROW = " \u279C";

    private int programCounterRow;

    public ProgramTableModel(final Cpu cpu) {
        super(cpu, new String[] { "PC", "Endereço", "Dado", "Mnemônico" },
                new Class<?>[] { String.class, Integer.class, Byte.class, String.class });
        setProgramCounterRow(0);
    }

    @Override
    public Object getValueAt(final int row, final int column) {
        switch (column) {
            case 0:
                return getProgramCounterRowAsString(row);
            case 1:
                return formatNumber(row);
            case 2:
                return formatNumber(cpu.getByte(row));
            case 3:
            default:
                return cpu.getMnemonic(row);
        }
    }

    private String getProgramCounterRowAsString(final int row) {
        return row == getProgramCounterRow() ? ARROW : "";
    }

    @Override
    public String getAddressAsString(final int row) {
        return (String) getValueAt(row, 1);
    }

    @Override
    public String getValueAsString(final int row) {
        return (String) getValueAt(row, 2);
    }

    public int getProgramCounterRow() {
        return programCounterRow;
    }

    public void setProgramCounterRow(final int programCounter) {
        final int oldValue = programCounterRow;
        programCounterRow = programCounter;
        fireTableRowsUpdated(oldValue, oldValue);
        fireTableRowsUpdated(programCounterRow, programCounterRow);
    }
}
