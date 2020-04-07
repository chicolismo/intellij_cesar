package cesar.gui.panels;

import java.awt.Font;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

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
        catch (IOException e) {
            System.err.println("Erro a ler os ícones dos botões");
            e.printStackTrace();
            System.exit(1);
        }
        runIcon = icon;
        nextIcon = next;
    }

    private final JToggleButton btnDec;
    private final JToggleButton btnHex;
    private final JToggleButton btnRun;
    private final JButton btnNext;

    public ButtonPanel() {
        btnDec = new JToggleButton("Dec");
        btnHex = new JToggleButton("Hex");
        btnRun = new JToggleButton(new ImageIcon(runIcon));
        btnNext = new JButton(new ImageIcon(nextIcon));
        initLayout();
    }

    public JToggleButton getDecButton() {
        return btnDec;
    }

    public JToggleButton getHexButton() {
        return btnHex;
    }

    public JToggleButton getRunButton() {
        return btnRun;
    }

    public JButton getNextButton() {
        return btnNext;
    }

    private void initLayout() {
        BoxLayout box = new BoxLayout(this, BoxLayout.X_AXIS);
        setLayout(box);
        btnDec.setAlignmentY(JComponent.BOTTOM_ALIGNMENT);
        btnHex.setAlignmentY(JComponent.BOTTOM_ALIGNMENT);
        btnRun.setAlignmentY(JComponent.BOTTOM_ALIGNMENT);
        btnNext.setAlignmentY(JComponent.BOTTOM_ALIGNMENT);

        btnDec.setFocusable(false);
        btnHex.setFocusable(false);
        btnRun.setFocusable(false);
        btnNext.setFocusable(false);

        Font font = new Font(Font.MONOSPACED, Font.PLAIN, 10);
        Insets margins = new Insets(0, 0, 0, 0);

        btnDec.putClientProperty("JButton.buttonType", "segmentedTextured");
        btnDec.putClientProperty("JButton.segmentPosition", "first");
        btnDec.setFont(font);
        btnHex.putClientProperty("JButton.buttonType", "segmentedTextured");
        btnHex.putClientProperty("JButton.segmentPosition", "last");
        btnHex.setFont(font);

        btnDec.setMargin(margins);
        btnHex.setMargin(margins);
        btnRun.setMargin(margins);
        btnNext.setMargin(margins);

        ButtonGroup g = new ButtonGroup();
        g.add(btnDec);
        g.add(btnHex);

        add(btnDec);
        add(btnHex);
        add(Box.createHorizontalGlue());
        add(btnRun);
        add(btnNext);
    }
}
