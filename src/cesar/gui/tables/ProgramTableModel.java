package cesar.gui.tables;

import cesar.hardware.Cpu;

public class ProgramTableModel extends TableModel {
    public static final long serialVersionUID = -5373447997057887767L;
    private static final String ARROW = " \u279C";
    private static final String EMPTY_STRING = "";

    private int pcRow;

    public ProgramTableModel(final Cpu cpu) {
        super(cpu);
        columnNames = new String[] { "PC", "Endereço", "Dado", "Mnemônico" };
        classNames = new Class<?>[] { String.class, Integer.class, Byte.class, String.class };
        pcRow = 0;
    }

    @Override
    public Object getValueAt(final int row, final int column) {
        switch (column) {
            case 0:
                return formatPcRow(row);
            case 1:
                return formatNumber(row);
            case 2:
                return formatNumber(cpu.getByte(row));
            case 3:
            default:
                return " Não implementado";
        }
    }

    @Override
    public String getAddressAsString(final int row) {
        return (String) getValueAt(row, 1);
    }

    @Override
    public String getValueAsString(final int row) {
        return (String) getValueAt(row, 2);
    }

    private String formatPcRow(final int row) {
        return row == pcRow ? ARROW : EMPTY_STRING;
    }

    public int getPcRow() {
        return pcRow;
    }

    public void setPcRow(final int programCounter) {
        final int oldPcRow = pcRow;
        pcRow = programCounter;
        fireTableRowsUpdated(oldPcRow, oldPcRow);
        fireTableRowsUpdated(pcRow, pcRow);
    }
}
