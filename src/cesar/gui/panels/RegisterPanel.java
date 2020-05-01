package cesar.gui.panels;

import cesar.gui.displays.RegisterDisplay;
import cesar.hardware.Cpu;
import cesar.utils.Base;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;

import static cesar.Properties.getProperty;

public class RegisterPanel extends JPanel {
    private static final long serialVersionUID = 2962079321929645473L;

    private static final String IS_LABEL = getProperty("IS.label");
    private static final String[][] REGISTER_STRINGS = new String[][] {
            { getProperty("R0.label"), getProperty("R0.newValueTitle"), getProperty("R0.newValueMessage") },
            { getProperty("R1.label"), getProperty("R1.newValueTitle"), getProperty("R1.newValueMessage") },
            { getProperty("R2.label"), getProperty("R2.newValueTitle"), getProperty("R2.newValueMessage") },
            { getProperty("R3.label"), getProperty("R3.newValueTitle"), getProperty("R3.newValueMessage") },
            { getProperty("R4.label"), getProperty("R4.newValueTitle"), getProperty("R4.newValueMessage") },
            { getProperty("R5.label"), getProperty("R5.newValueTitle"), getProperty("R5.newValueMessage") },
            { getProperty("R6.label"), getProperty("R6.newValueTitle"), getProperty("R6.newValueMessage") },
            { getProperty("R7.label"), getProperty("R7.newValueTitle"), getProperty("R7.newValueMessage") }
    };

    private static final ImageIcon COMPUTER_ICON;
    private static final ImageIcon ALTERNATIVE_ICON;
    private static final Color PC_LABEL_COLOR = new Color(0, 96, 0);

    static {
        BufferedImage computerIcon = null;
        BufferedImage alternativeIcon = null;
        try {
            computerIcon = ImageIO.read(RegisterPanel.class.getResourceAsStream("/cesar/gui/assets/computer.png"));
            alternativeIcon = ImageIO.read(RegisterPanel.class.getResourceAsStream("/cesar/gui/assets/weber.png"));
        }
        catch (final IOException e) {
            System.err.println("Erro ao ler o ícone do computador");
            System.exit(1);
        }
        COMPUTER_ICON = new ImageIcon(computerIcon);
        ALTERNATIVE_ICON = new ImageIcon(alternativeIcon);
    }

    private final RegisterDisplay[] registerDisplays;
    private final LedPanel interruptionPanel;
    private boolean isComputerShowing;

    public RegisterPanel() {
        super(true);

        isComputerShowing = true;

        interruptionPanel = new LedPanel(IS_LABEL);

        registerDisplays = new RegisterDisplay[Cpu.REGISTER_COUNT];

        for (int i = 0; i < Cpu.REGISTER_COUNT; ++i) {
            final String[] strings = REGISTER_STRINGS[i];
            //noinspection ObjectAllocationInLoop
            registerDisplays[i] = new RegisterDisplay(i, strings[0], strings[1], strings[2]);
        }

        ((TitledBorder) registerDisplays[Cpu.PC].getBorder()).setTitleColor(PC_LABEL_COLOR);

        initLayout();
        doLayout();
    }

    private void initLayout() {
        final JLabel computerLabel = new JLabel(COMPUTER_ICON);
        computerLabel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 3, 3, 3),
                BorderFactory.createBevelBorder(BevelBorder.RAISED)));

        final JPanel centerPanel = new JPanel();
        final GroupLayout groupLayout = new GroupLayout(centerPanel);
        groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.TRAILING).addGroup(
                groupLayout.createSequentialGroup().addComponent(computerLabel, GroupLayout.DEFAULT_SIZE,
                        GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE).addComponent(interruptionPanel,
                        GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE)));

        groupLayout.setVerticalGroup(
                groupLayout.createParallelGroup(Alignment.LEADING).addComponent(interruptionPanel, Alignment.TRAILING,
                        GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE).addComponent(
                        computerLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));
        centerPanel.setLayout(groupLayout);

        setLayout(new GridLayout(3, 3, 1, 1));

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
                    computerLabel.setIcon(isComputerShowing ? ALTERNATIVE_ICON : COMPUTER_ICON);
                    isComputerShowing = !isComputerShowing;
                }
            }
        });
    }

    public RegisterDisplay getDisplay(final int i) {
        return registerDisplays[i];
    }

    public RegisterDisplay[] getDisplays() {
        return registerDisplays;
    }

    public void setBase(final Base base) {
        for (final RegisterDisplay display : registerDisplays) {
            display.setBase(base);
        }
    }
}
