package cesar.gui.panels;

import cesar.Properties;
import cesar.gui.displays.DigitalDisplay;
import cesar.utils.Defaults;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import java.awt.*;

public class ExecutionPanel extends JPanel {
    private static final long serialVersionUID = 8981667379501321204L;
    private static final String TITLE = Properties.getProperty("ExecutionPanel.title");
    private static final String ACCESS_LABEL = Properties.getProperty("ExecutionPanel.accessLabel");
    private static final String INSTRUCTION_LABEL = Properties.getProperty("ExecutionPanel.instructionLabel");

    private final DigitalDisplay memoryAccessCountDisplay;
    private final DigitalDisplay instructionCountDisplay;

    public ExecutionPanel() {
        memoryAccessCountDisplay = new DigitalDisplay();
        instructionCountDisplay = new DigitalDisplay();

        final JLabel accessLabel = Defaults.createLabel(ACCESS_LABEL);
        final JLabel instructionLabel = Defaults.createLabel(INSTRUCTION_LABEL);

        final GridBagLayout grid = new GridBagLayout();
        grid.columnWidths = new int[] { 0, 0 };
        grid.rowHeights = new int[] { 0, 0 };
        grid.columnWeights = new double[] { 0.0, 0.0 };
        grid.rowWeights = new double[] { 0.0, 0.0 };
        setLayout(grid);

        final GridBagConstraints c_0 = new GridBagConstraints();
        c_0.ipadx = 5;
        c_0.gridx = 0;
        c_0.gridy = 0;
        c_0.anchor = GridBagConstraints.WEST;
        add(accessLabel, c_0);

        final GridBagConstraints c_1 = new GridBagConstraints();
        c_1.ipadx = 5;
        c_1.gridx = 0;
        c_1.gridy = 1;
        c_1.anchor = GridBagConstraints.WEST;
        add(instructionLabel, c_1);

        final GridBagConstraints c_2 = new GridBagConstraints();
        c_2.gridx = 1;
        c_2.gridy = 0;
        c_2.anchor = GridBagConstraints.EAST;
        add(memoryAccessCountDisplay, c_2);

        final GridBagConstraints c_3 = new GridBagConstraints();
        c_3.gridx = 1;
        c_3.gridy = 1;
        c_3.anchor = GridBagConstraints.EAST;
        add(instructionCountDisplay, c_3);

        final Border outer = Defaults.createTitledBorder(TITLE);
        final Border inner = Defaults.getEmptyBorder();
        setBorder(new CompoundBorder(outer, inner));
    }

    public void setInstructionCount(int count) {
        instructionCountDisplay.setValue(count);
    }

    public void setMemoryAccessCount(final int accesses) {
        memoryAccessCountDisplay.setValue(accesses);
    }
}
