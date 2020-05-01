package cesar.gui.panels;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

public class MainPanel extends JPanel {
    private static final long serialVersionUID = -6154605833067699966L;

    private static final Border BORDER = new CompoundBorder(
            new CompoundBorder(new EmptyBorder(1, 1, 1, 1), new BevelBorder(BevelBorder.LOWERED)),
            new EmptyBorder(3, 3, 3, 3));

    public MainPanel(final ConditionPanel conditionPanel, final ButtonPanel buttonPanel,
            final ExecutionPanel executionPanel, final RegisterPanel registerPanel,
            final InstructionPanel instructionPanel) {

        final JPanel middleRightPanel = new JPanel();
        middleRightPanel.setLayout(new BoxLayout(middleRightPanel, BoxLayout.Y_AXIS));
        middleRightPanel.add(conditionPanel);
        middleRightPanel.add(Box.createVerticalGlue());
        middleRightPanel.add(buttonPanel);

        final JPanel middlePanel = new JPanel();
        middlePanel.setLayout(new BoxLayout(middlePanel, BoxLayout.X_AXIS));
        middlePanel.add(executionPanel);
        middlePanel.add(middleRightPanel);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(registerPanel);
        add(middlePanel);
        add(instructionPanel);
        setBorder(BORDER);
    }
}
