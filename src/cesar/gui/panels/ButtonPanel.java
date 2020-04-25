package cesar.gui.panels;

import cesar.utils.Base;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class ButtonPanel extends JPanel {
    private static final long serialVersionUID = -1509965084306287422L;

    private static final BufferedImage runIcon;
    private static final BufferedImage nextIcon;

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
        runIcon = icon;
        nextIcon = next;
    }

    public final JToggleButton btnDec;
    public final JToggleButton btnHex;
    public final JToggleButton btnRun;
    public final JButton btnNext;

    public ButtonPanel() {
        btnDec = new JToggleButton("0..9");
        btnDec.putClientProperty("Base.value", Base.DECIMAL);

        btnHex = new JToggleButton("0..F");
        btnHex.putClientProperty("Base.value", Base.HEXADECIMAL);
        btnRun = new JToggleButton(new ImageIcon(runIcon));
        btnNext = new JButton(new ImageIcon(nextIcon));

        if (System.getProperty("os.name").equals("Mac OS X")) {
            btnDec.putClientProperty("JButton.buttonType", "segmented");
            btnDec.putClientProperty("JComponent.sizeVariant", "small");
            btnDec.putClientProperty("JButton.segmentPosition", "first");
            btnHex.putClientProperty("JButton.buttonType", "segmented");
            btnHex.putClientProperty("JComponent.sizeVariant", "small");
            btnHex.putClientProperty("JButton.segmentPosition", "last");
        }

        initLayout();
    }

    private void initLayout() {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setAlignmentY(BOTTOM_ALIGNMENT);

        final Insets margins = new Insets(1, 1, 1, 1);
        final AbstractButton[] buttons = new AbstractButton[] { btnDec, btnHex, btnRun, btnNext };
        for (final AbstractButton button : buttons) {
            button.setMargin(margins);
            button.setAlignmentY(CENTER_ALIGNMENT);
            button.setFocusable(false);
        }

        final ButtonGroup changeBaseGroup = new ButtonGroup();
        changeBaseGroup.add(btnDec);
        changeBaseGroup.add(btnHex);

        add(btnDec);
        add(btnHex);
        add(Box.createHorizontalGlue());
        add(btnRun);
        add(btnNext);
    }
}
