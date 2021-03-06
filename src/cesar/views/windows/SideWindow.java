package cesar.views.windows;

import cesar.models.Base;
import cesar.models.Cpu;
import cesar.utils.Defaults;
import cesar.views.tables.Table;
import cesar.views.tables.TableModel;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;

public abstract class SideWindow<TableType extends Table, TableModelType extends TableModel> extends JDialog {
    public static final long serialVersionUID = 3602114587032491724L;
    public static final String LABEL_FORMAT = "[%s]";

    protected final JLabel addressLabel;

    protected final JTextField valueField;
    protected int currentAddress;
    protected int currentValue;
    protected TableType table;
    protected TableModelType model;
    private Base currentBase;

    public SideWindow(final MainWindow parent, final String title, final Cpu cpu) {
        super(parent, title, false);
        setFocusable(true);
        setModalityType(ModalityType.MODELESS);
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        initTable(cpu);
        setCurrentAddress(0);
        setCurrentValue(0);
        addressLabel = new JLabel(String.format(LABEL_FORMAT, "0"));
        valueField = new JTextField(6);
        valueField.setMinimumSize(valueField.getPreferredSize());

        ((AbstractDocument) valueField.getDocument()).setDocumentFilter(new UpperCaseFilter());
        currentBase = Defaults.DEFAULT_BASE;
    }

    abstract protected void initTable(final Cpu cpu);

    public void setCurrentValue(final int value) {
        currentValue = value;
    }

    protected void initLayout() {
        final JComponent contentPane = (JComponent) getContentPane();
        contentPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

        final JScrollPane scrollPane = new JScrollPane(table);
        final Dimension tableSize = table.getPreferredSize();
        final int scrollBarWidth = scrollPane.getVerticalScrollBar().getPreferredSize().width;
        final Dimension scrollPaneSize = new Dimension(tableSize.width + scrollBarWidth, tableSize.height);
        scrollPane.setPreferredSize(scrollPaneSize);
        contentPane.add(scrollPane);
    }

    public void clickOnRow(final int row) {
        table.setRowSelectionInterval(row, row);
        table.scrollToRow(row);
        final String address = model.getAddressAsString(row);
        final String value = model.getValueAsString(row);
        addressLabel.setText(String.format(SideWindow.LABEL_FORMAT, address));
        valueField.setText(value);
        valueField.requestFocus();
        valueField.selectAll();
        final int radix = currentBase.toInt();
        final int currentAddress = Integer.parseInt(address, radix);
        final int currentValue = Integer.parseInt(value, radix);
        setCurrentAddress(currentAddress);
        setCurrentValue(currentValue);
    }

    public int getCurrentAddress() {
        return currentAddress;
    }

    public void setCurrentAddress(final int address) {
        currentAddress = address;
    }

    public TableType getTable() {
        return table;
    }

    public JTextField getValueField() {
        return valueField;
    }

    public void setBase(final Base base) {
        currentBase = base;
        model.setBase(base);
        final int radix = base.toInt();
        addressLabel.setText(String.format(LABEL_FORMAT, Integer.toString(currentAddress, radix)));
        valueField.setText(Integer.toString(currentValue, radix));
    }

    public Base getCurrentBase() {
        return currentBase;
    }

    protected static GridBagLayout getGridLayout(final double[] rowWeights, final double[] colWeights) {
        final GridBagLayout grid = new GridBagLayout();
        grid.rowWeights = rowWeights;
        grid.columnWeights = colWeights;
        return grid;
    }

    protected static class UpperCaseFilter extends DocumentFilter {
        @Override
        public void insertString(DocumentFilter.FilterBypass fb, int offset, String text, AttributeSet attr)
                throws BadLocationException {
            fb.insertString(offset, text.toUpperCase(), attr);
        }

        @Override
        public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                throws BadLocationException {
            fb.replace(offset, length, text.toUpperCase(), attrs);
        }
    }
}
