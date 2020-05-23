package cesar.views.windows;

import java.awt.Color;
import java.awt.GridBagConstraints;


import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

import cesar.models.Base;
import cesar.models.Cpu;
import cesar.utils.Properties;
import cesar.utils.Shorts;
import cesar.views.tables.ProgramTable;
import cesar.views.tables.ProgramTableModel;

public class ProgramWindow extends SideWindow<ProgramTable, ProgramTableModel> {
    public static final long serialVersionUID = 8452878222228144644L;

    private final BreakPointField bpField;

    public ProgramWindow(final MainWindow parent, final Cpu cpu) {
        super(parent, Properties.getProperty("ProgramWindow.title"), cpu);
        bpField = new BreakPointField(cpu, getCurrentBase());
        bpField.setMinimumSize(bpField.getPreferredSize());
        initLayout();
    }

    @Override
    protected void initLayout() {
        super.initLayout();

        final JLabel bpLabel = new JLabel(Properties.getProperty("ProgramWindow.BreakPoint.text"));
        try {
            final int rgb = Integer.parseInt(Properties.getProperty("ProgramWindow.breakPointColor"), 16);
            bpLabel.setForeground(new Color(rgb));
        }
        catch (final NumberFormatException e) {
            bpLabel.setForeground(Color.RED);
        }

        final JPanel lowerPanel = new JPanel(
                getGridLayout(new double[] { 1.0 }, new double[] { 0.0, 0.0, 1.0, 0.0, 0.0 }));

        final GridBagConstraints c_0 = new GridBagConstraints();
        c_0.ipadx = 4;
        c_0.gridx = 0;
        c_0.gridy = 0;
        c_0.anchor = GridBagConstraints.WEST;
        lowerPanel.add(bpLabel, c_0);

        final GridBagConstraints c_1 = new GridBagConstraints();
        c_1.ipadx = 4;
        c_1.gridx = 1;
        c_1.gridy = 0;
        c_1.anchor = GridBagConstraints.WEST;
        lowerPanel.add(bpField, c_1);

        final GridBagConstraints c_2 = new GridBagConstraints();
        c_2.ipadx = 4;
        c_2.gridx = 1;
        c_2.gridy = 0;
        c_2.anchor = GridBagConstraints.WEST;
        lowerPanel.add(Box.createHorizontalGlue(), c_2);

        final GridBagConstraints c_3 = new GridBagConstraints();
        c_3.ipadx = 4;
        c_3.gridx = 3;
        c_3.gridy = 0;
        c_3.anchor = GridBagConstraints.EAST;
        lowerPanel.add(addressLabel, c_3);

        final GridBagConstraints c_4 = new GridBagConstraints();
        c_4.ipadx = 4;
        c_4.gridx = 4;
        c_4.gridy = 0;
        c_4.anchor = GridBagConstraints.EAST;
        lowerPanel.add(valueField, c_4);

        add(Box.createVerticalStrut(4));
        add(lowerPanel);
        pack();
    }

    @Override
    protected void initTable(final Cpu cpu) {
        model = new ProgramTableModel(cpu);
        table = new ProgramTable(model);
    }

    @Override
    public void setBase(final Base base) {
        super.setBase(base);
        bpField.setBase(base);
    }

    public BreakPointField getBreakPointField() {
        return bpField;
    }

    public static class BreakPointField extends JTextField {
        private final Cpu cpu;
        private Base currentBase;

        public BreakPointField(final Cpu cpu, final Base base) {
            super(4);
            this.cpu = cpu;
            setBase(base);
            setCurrentBreakPoint();
            ((AbstractDocument) getDocument()).setDocumentFilter(new UpperCaseFilter());
        }

        public void setBase(final Base base) {
            currentBase = base;
            setCurrentBreakPoint();
        }

        public short getBreakPoint() {
            final int radix = currentBase.toInt();
            try {
                final String string = getText();
                final int value = Integer.parseInt(string, radix);
                return Shorts.fromInt(value);
            }
            catch (NumberFormatException ignore) {
                setCurrentBreakPoint();
                return cpu.getBreakPoint();
            }
        }

        private void setCurrentBreakPoint() {
            final short breakPoint = cpu.getBreakPoint();
            setText(Integer.toString(Shorts.toUnsignedInt(breakPoint), currentBase.toInt()));
        }
    }
}
