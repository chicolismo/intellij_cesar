package cesar.gui.panels;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;

import cesar.Properties;
import cesar.gui.displays.RegisterDisplay;
import cesar.hardware.Cpu;
import cesar.utils.Base;

public class RegisterPanel extends JPanel {
    private static final long serialVersionUID = 2962079321929645473L;
    private static final BufferedImage COMPUTER_ICON;
    private static final BufferedImage WEBER_ICON;

    static {
        BufferedImage computerIcon = null;
        BufferedImage weberIcon = null;
        try {
            computerIcon = ImageIO.read(RegisterPanel.class.getResourceAsStream("/cesar/gui/assets/computer.png"));
            weberIcon = ImageIO.read(RegisterPanel.class.getResourceAsStream("/cesar/gui/assets/weber.png"));
        }
        catch (final IOException e) {
            System.err.println("Erro ao ler o ícone do computador");
            System.exit(1);
        }
        COMPUTER_ICON = computerIcon;
        WEBER_ICON = weberIcon;
    }

    private final ImageIcon computerIcon;
    private final ImageIcon weberIcon;
    private final RegisterDisplay[] registerDisplays;
    private final LedPanel interruptionPanel;
    private boolean isComputerShowing;

    public RegisterPanel() {
        super(true);

        registerDisplays = new RegisterDisplay[Cpu.REGISTER_COUNT];
        for (int i = 0; i < Cpu.REGISTER_COUNT; ++i) {
            final String label = Properties.getProperty(String.format("R%d.label", i));
            final String title = Properties.getProperty(String.format("R%d.newValueTitle", i));
            final String message = Properties.getProperty(String.format("R%d.newValueMessage", i));
            registerDisplays[i] = new RegisterDisplay(i, label, title, message);
        }

        final TitledBorder border = (TitledBorder) registerDisplays[7].getBorder();
        border.setTitleColor(new Color(0, 96, 0));

        interruptionPanel = new LedPanel("IS");

        computerIcon = new ImageIcon(COMPUTER_ICON);
        weberIcon = new ImageIcon(WEBER_ICON);

        initLayout();

        isComputerShowing = true;

        doLayout();
    }

    public RegisterDisplay getDisplay(final int i) {
        return registerDisplays[i];
    }

    public RegisterDisplay[] getDisplays() {
        return registerDisplays;
    }

    private void initLayout() {
        final JLabel computerLabel = new JLabel(computerIcon);
        computerLabel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 3, 3, 3),
                BorderFactory.createBevelBorder(BevelBorder.RAISED)));

        final JPanel centerPanel = new JPanel();
        final GroupLayout groupLayout = new GroupLayout(centerPanel);
        groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
                .addGroup(groupLayout.createSequentialGroup()
                        .addComponent(computerLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE,
                                Short.MAX_VALUE)
                        .addComponent(interruptionPanel, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE)));

        groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                .addComponent(interruptionPanel, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE,
                        GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
                .addComponent(computerLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));
        centerPanel.setLayout(groupLayout);

        final GridLayout grid = new GridLayout(3, 3);
        setLayout(grid);
        add(registerDisplays[0]);
        add(registerDisplays[1]);
        add(registerDisplays[2]);
        add(registerDisplays[3]);
        add(registerDisplays[4]);
        add(registerDisplays[5]);
        add(registerDisplays[6]);
        add(centerPanel);
        add(registerDisplays[7]);

        computerLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent e) {
                if (e.getClickCount() == 2) {
                    computerLabel.setIcon(isComputerShowing ? weberIcon : computerIcon);
                    isComputerShowing = !isComputerShowing;
                }
            }
        });
    }

    public void setBase(final Base base) {
        for (final RegisterDisplay display : registerDisplays) {
            display.setBase(base);
        }
    }
}
