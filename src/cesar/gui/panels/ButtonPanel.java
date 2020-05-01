package cesar.gui.panels;

import cesar.Properties;
import cesar.utils.Defaults;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class ButtonPanel extends JPanel {
    private static final long serialVersionUID = -1509965084306287422L;

    private static final Insets BUTTON_INSETS = new Insets(1, 1, 1, 1);
    private static final String DECIMAL_LABEL = Properties.getProperty("ButtonPanel.decimalLabel");
    private static final String HEXADECIMAL_LABEL = Properties.getProperty("ButtonPanel.hexadecimalLabel");
    private static final ImageIcon RUN_ICON;
    private static final ImageIcon NEXT_ICON;

    static {
        BufferedImage icon = null;
        BufferedImage next = null;
        try {
            icon = ImageIO.read(ButtonPanel.class.getResourceAsStream("/cesar/gui/assets/config.png"));
            next = ImageIO.read(ButtonPanel.class.getResourceAsStream("/cesar/gui/assets/tools.png"));
        }
        catch (final IOException e) {
            System.err.println("Erro a ler os ícones dos botões");
            e.printStackTrace();
            System.exit(1);
        }
        RUN_ICON = new ImageIcon(icon);
        NEXT_ICON = new ImageIcon(next);
    }

    private final JToggleButton decimalButton;
    private final JToggleButton hexadecimalButton;
    private final JToggleButton runButton;
    private final JButton nextButton;

    public ButtonPanel() {
        decimalButton = new JToggleButton(DECIMAL_LABEL);
        hexadecimalButton = new JToggleButton(HEXADECIMAL_LABEL);
        runButton = new JToggleButton(RUN_ICON);
        nextButton = new JButton(NEXT_ICON);

        if (Defaults.IS_APPLE) {
            decimalButton.putClientProperty("JButton.buttonType", "segmented");
            decimalButton.putClientProperty("JComponent.sizeVariant", "small");
            decimalButton.putClientProperty("JButton.segmentPosition", "first");
            hexadecimalButton.putClientProperty("JButton.buttonType", "segmented");
            hexadecimalButton.putClientProperty("JComponent.sizeVariant", "small");
            hexadecimalButton.putClientProperty("JButton.segmentPosition", "last");
        }

        initLayout();
    }

    private void initLayout() {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setAlignmentY(BOTTOM_ALIGNMENT);

        for (final AbstractButton button : new AbstractButton[] {
                decimalButton, hexadecimalButton, runButton, nextButton
        }) {
            button.setMargin(BUTTON_INSETS);
            button.setAlignmentY(CENTER_ALIGNMENT);
            button.setFocusable(false);
        }

        final ButtonGroup changeBaseGroup = new ButtonGroup();
        changeBaseGroup.add(decimalButton);
        changeBaseGroup.add(hexadecimalButton);

        add(decimalButton);
        add(hexadecimalButton);
        add(Box.createHorizontalGlue());
        add(runButton);
        add(nextButton);
    }

    public JToggleButton getDecimalButton() {
        return decimalButton;
    }

    public JToggleButton getHexadecimalButton() {
        return hexadecimalButton;
    }

    public JButton getNextButton() {
        return nextButton;
    }

    public JToggleButton getRunButton() {
        return runButton;
    }
}
