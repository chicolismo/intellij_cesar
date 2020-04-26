package cesar.gui.windows;

import java.awt.Dimension;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import cesar.gui.tables.Table;
import cesar.gui.tables.TableModel;
import cesar.hardware.Cpu;
import cesar.utils.Base;

public abstract class SideWindow<TableType extends Table, TableModelType extends TableModel> extends JDialog {
    public static final long serialVersionUID = 3602114587032491724L;
    public static final String LABEL_FORMAT = "[%s]";

    protected static GridBagLayout getGridLayout(final double[] rowWeights, final double[] colWeights) {
        final GridBagLayout grid = new GridBagLayout();
        grid.rowWeights = rowWeights;
        grid.columnWeights = colWeights;
        return grid;
    }

    protected final JLabel addressLabel;
    protected final JTextField valueField;
    protected int currentAddress;
    protected int currentValue;
    protected TableType table;

    protected TableModelType model;

    public SideWindow(final MainWindow parent, final String title, final Cpu cpu) {
        super(parent, title, false);
        setFocusable(true);
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        initTable(cpu);
        setCurrentAddress(0);
        setCurrentValue(0);
        addressLabel = new JLabel("[0]");
        valueField = new JTextField(6);
        valueField.setMinimumSize(valueField.getPreferredSize());
    }

    public JLabel getAddressLabel() {
        return addressLabel;
    }

    public int getCurrentAddress() {
        return currentAddress;
    }

    public int getCurrentValue() {
        return currentValue;
    }

    public TableType getTable() {
        return table;
    }

    public JTextField getValueField() {
        return valueField;
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

    abstract protected void initTable(final Cpu cpu);

    public void setBase(final Base base) {
        final int radix = Base.toInt(base);
        model.setBase(base);
        addressLabel.setText(String.format(LABEL_FORMAT, Integer.toString(currentAddress, radix)));
        valueField.setText(Integer.toString(currentValue, radix));
    }

    public void setCurrentAddress(final int address) {
        currentAddress = address;
    }

    public void setCurrentValue(final int value) {
        currentValue = value;
    }
}
