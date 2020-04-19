package cesar.gui.windows;

import cesar.gui.tables.Table;
import cesar.gui.tables.TableModel;
import cesar.hardware.Cpu;
import cesar.utils.Base;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public abstract class SideWindow<TableType extends Table, TableModelType extends TableModel> extends JDialog {
    public static final long serialVersionUID = 3602114587032491724L;

    protected static final String LABEL_FORMAT = "[%s]";

    protected final JLabel addressLabel;
    protected final JTextField valueField;
    protected int currentAddress;
    protected int currentValue;
    protected TableType table;
    protected TableModelType model;

    public SideWindow(final MainWindow parent, final String title, final Cpu cpu) {
        super(parent, title);
        setIconImage(null);
        setFocusable(false);
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        currentAddress = 0;
        currentValue = 0;
        addressLabel = new JLabel("[0]");
        valueField = new JTextField(6);
        valueField.setMinimumSize(valueField.getPreferredSize());
        initTable(cpu);
    }

    public TableType getTable() {
        return table;
    }

    protected void initEvents() {
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(final MouseEvent e) {
                if (table.getSelectedRow() >= 0) {
                    final int row = table.getSelectedRow();
                    final String address = model.getAddressAsString(row);
                    final String value = model.getValueAsString(row);
                    addressLabel.setText(String.format(LABEL_FORMAT, address));
                    valueField.setText(value);
                    valueField.requestFocus();
                    valueField.selectAll();

                    final int radix = Base.toInt(model.getBase());
                    currentAddress = Integer.parseInt(address, radix);
                    currentValue = Integer.parseInt(value, radix);
                }
            }
        });

        valueField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent actionEvent) {
                final int radix = Base.toInt(model.getBase());
                final String value = valueField.getText();
                final int newValue;
                try {
                    newValue = Integer.parseInt(value, radix);
                }
                catch (final NumberFormatException exception) {
                    // Se o valor digitado for inválido, ignorar o ENTER
                    return;
                }
                if (newValue <= 0xFF && newValue >= Byte.MIN_VALUE) {
                    currentValue = newValue;
                    model.setValue(currentAddress, (byte) (0xFF & currentValue));
                    // Seleciona a próxima linha
                    currentAddress = 0xFFFF & currentAddress + 1;
                    table.setRowSelectionInterval(currentAddress, currentAddress);
                    valueField.setText(model.getValueAsString(currentAddress));
                    table.scrollToRow(currentAddress);
                    valueField.requestFocus();
                    valueField.selectAll();
                }
            }
        });
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

    public void setBase(final Base base) {
        final int radix = Base.toInt(base);
        model.setBase(base);
        addressLabel.setText(String.format(LABEL_FORMAT, Integer.toString(currentAddress, radix)));
        valueField.setText(Integer.toString(currentValue, radix));
    }

    abstract protected void initTable(final Cpu cpu);
}
