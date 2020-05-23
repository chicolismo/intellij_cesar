package cesar.views.panels;

import java.awt.*;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;

import cesar.utils.Defaults;

public class InstructionPanel extends JPanel {
    private static final long serialVersionUID = -7005281883928099202L;

    private static final int ROW_HEIGHT = 24;

    final private JLabel riText;
    final private JLabel mnemonicText;

    public InstructionPanel() {
        final Font monoFont = new Font(Font.MONOSPACED, Font.PLAIN, 12);

        riText = new JLabel();
        riText.setFont(monoFont);

        mnemonicText = new JLabel();
        mnemonicText.setFont(monoFont);

        riText.setMinimumSize(riText.getPreferredSize());
        mnemonicText.setMinimumSize(mnemonicText.getPreferredSize());

        final Border border = BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED),
                BorderFactory.createEmptyBorder(1, 1, 1, 1));

        riText.setBorder(border);
        mnemonicText.setBorder(border);

        initLayout();
        setSize(getPreferredSize());
    }

    private void initLayout() {
        final JLabel riLabel = Defaults.createLabel("RI: ");
        final JLabel mnemonicLabel = Defaults.createLabel("Mnem: ");

        final GridBagLayout grid = new GridBagLayout();
        grid.rowHeights = new int[] { ROW_HEIGHT, ROW_HEIGHT };
        grid.rowWeights = new double[] { 0.0, 0.0 };
        grid.columnWeights = new double[] { 0.0, 1.0 };
        setLayout(grid);

        final GridBagConstraints c_0 = new GridBagConstraints();
        c_0.gridx = 0;
        c_0.gridy = 0;
        c_0.anchor = GridBagConstraints.WEST;
        add(riLabel, c_0);

        final GridBagConstraints c_1 = new GridBagConstraints();
        c_1.gridx = 0;
        c_1.gridy = 1;
        c_1.anchor = GridBagConstraints.WEST;
        add(mnemonicLabel, c_1);

        final GridBagConstraints c_2 = new GridBagConstraints();
        c_2.gridx = 1;
        c_2.gridy = 0;
        c_2.fill = GridBagConstraints.BOTH;
        add(riText, c_2);

        final GridBagConstraints c_3 = new GridBagConstraints();
        c_3.gridx = 1;
        c_3.gridy = 1;
        c_3.fill = GridBagConstraints.BOTH;
        add(mnemonicText, c_3);

        final Border outer = Defaults.createTitledBorder("Instrução:");
        final Border border = new CompoundBorder(outer, Defaults.createEmptyBorder());
        setBorder(border);
    }

    public void setMnemonicText(final String text) {
        mnemonicText.setText(text);
    }

    public void setRiText(final String text) {
        riText.setText(text);
    }
}
