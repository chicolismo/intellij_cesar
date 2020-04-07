package cesar.gui.panels;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;

import cesar.gui.displays.DigitalDisplay;
import cesar.utils.Defaults;

public class ExecutionPanel extends JPanel {
    private static final long serialVersionUID = 8981667379501321204L;

    private final DigitalDisplay accessDisplay;
    private final DigitalDisplay instructionDisplay;

    public ExecutionPanel() {
        super(true);
        accessDisplay = new DigitalDisplay();
        instructionDisplay = new DigitalDisplay();

        initLayout();
    }

    private void initLayout() {
        final JLabel accessLabel = Defaults.createLabel("Acessos: ");
        final JLabel instructionLabel = Defaults.createLabel("Instruções: ");

        GridBagLayout grid = new GridBagLayout();
        grid.columnWidths = new int[] { 0, 0 };
        grid.rowHeights = new int[] { 0, 0 };
        grid.columnWeights = new double[] { 0.0, 0.0 };
        grid.rowWeights = new double[] { 0.0, 0.0 };

        setLayout(grid);

        final GridBagConstraints c_0 = new GridBagConstraints();
        c_0.gridx = 0;
        c_0.gridy = 0;
        c_0.anchor = GridBagConstraints.WEST;
        add(accessLabel, c_0);

        final GridBagConstraints c_1 = new GridBagConstraints();
        c_1.gridx = 0;
        c_1.gridy = 1;
        c_1.anchor = GridBagConstraints.WEST;
        add(instructionLabel, c_1);

        final GridBagConstraints c_2 = new GridBagConstraints();
        c_2.gridx = 1;
        c_2.gridy = 0;
        c_2.anchor = GridBagConstraints.EAST;
        add(accessDisplay, c_2);

        final GridBagConstraints c_3 = new GridBagConstraints();
        c_3.gridx = 1;
        c_3.gridy = 1;
        c_3.anchor = GridBagConstraints.EAST;
        add(instructionDisplay, c_3);

        final Border outer = Defaults.createTitledBorder("Execução:");
        final Border inner = Defaults.createEmptyBorder();
        setBorder(new CompoundBorder(outer, inner));
    }
}
