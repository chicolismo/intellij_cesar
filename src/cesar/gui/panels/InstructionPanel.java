package cesar.gui.panels;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;

import cesar.utils.Defaults;

public class InstructionPanel extends JPanel {
    private static final long serialVersionUID = -7005281883928099202L;

    final private JLabel riText;
    final private JLabel mnemText;

    public InstructionPanel() {
        riText = new JLabel("Vai tomar no cu ");
        mnemText = new JLabel("MOV R3 R4");

        Border border = new CompoundBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED),
                BorderFactory.createEmptyBorder(1, 1, 1, 1));

        riText.setBorder(border);
        mnemText.setBorder(border);

        initLayout();
    }

    private void initLayout() {
        JLabel riLabel = Defaults.createLabel("RI: ");
        JLabel mnemLabel = Defaults.createLabel("Mnem: ");

        GridBagLayout grid = new GridBagLayout();
        grid.rowHeights = new int[] { 0, 0 };
        grid.columnWidths = new int[] { 0, 0 };
        grid.rowWeights = new double[] { 0.0, 0.0 };
        grid.columnWeights = new double[] { 0.0, 1.0 };
        setLayout(grid);

        GridBagConstraints c_0 = new GridBagConstraints();
        c_0.gridx = 0;
        c_0.gridy = 0;
        c_0.anchor = GridBagConstraints.WEST;
        add(riLabel, c_0);

        GridBagConstraints c_1 = new GridBagConstraints();
        c_1.gridx = 0;
        c_1.gridy = 1;
        c_1.anchor = GridBagConstraints.WEST;
        add(mnemLabel, c_1);

        GridBagConstraints c_2 = new GridBagConstraints();
        c_2.gridx = 1;
        c_2.gridy = 0;
        c_2.fill = GridBagConstraints.BOTH;
        add(riText, c_2);

        GridBagConstraints c_3 = new GridBagConstraints();
        c_3.gridx = 1;
        c_3.gridy = 1;
        c_3.fill = GridBagConstraints.BOTH;
        add(mnemText, c_3);

        Border outer = Defaults.createTitledBorder("Instrução:");
        Border border = new CompoundBorder(outer, Defaults.createEmptyBorder());
        setBorder(border);
    }
}
