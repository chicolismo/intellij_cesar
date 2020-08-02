package cesar.views.panels;

import cesar.models.Base;
import cesar.models.Cpu;
import cesar.views.displays.RegisterDisplay;

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

public class RegisterPanel extends JPanel {
    private static final long serialVersionUID = 2962079321929645473L;

    private static final String IS_LABEL = "IS";
    private static final String REGISTER_NEW_VALUE_TITLE = "Alterar registrador";
    private static final String REGISTER_LABEL_FORMAT = "R%d";
    private static final String REGISTER_NEW_VALUE_FORMAT = "Digite o novo valor do R%d";
    private static final ImageIcon[] COMPUTER_ICONS = new ImageIcon[2];
    private static final Color PC_LABEL_COLOR = new Color(0, 96, 0);
    private static final String COMPUTER_ICON_PATH = "/cesar/resources/images/computer.png";
    private static final String ALTERNATIVE_ICON_PATH = "/cesar/resources/images/weber.png";

    static {
        BufferedImage computerIcon = null;
        BufferedImage alternativeIcon = null;
        try {
            computerIcon = ImageIO.read(RegisterPanel.class.getResourceAsStream(COMPUTER_ICON_PATH));
            alternativeIcon = ImageIO.read(RegisterPanel.class.getResourceAsStream(ALTERNATIVE_ICON_PATH));
        }
        catch (final IOException e) {
            System.err.println("Erro ao ler o ícone do computador");
            System.exit(1);
        }
        COMPUTER_ICONS[0] = new ImageIcon(computerIcon);
        COMPUTER_ICONS[1] = new ImageIcon(alternativeIcon);
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
            registerDisplays[i] =
                    new RegisterDisplay(i, String.format(REGISTER_LABEL_FORMAT, i), REGISTER_NEW_VALUE_TITLE,
                            String.format(REGISTER_NEW_VALUE_FORMAT, i));
        }

        // Deixa a cor do rótulo do R7 "verde"
        ((TitledBorder) registerDisplays[Cpu.PC].getBorder()).setTitleColor(PC_LABEL_COLOR);

        initLayout();
        doLayout();
    }

    private void initLayout() {
        final JLabel computerLabel = new JLabel(COMPUTER_ICONS[0]);
        computerLabel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 3, 3, 3),
                BorderFactory.createBevelBorder(BevelBorder.RAISED)));

        final JPanel centerPanel = new JPanel();
        final GroupLayout groupLayout = new GroupLayout(centerPanel);
        groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.TRAILING).addGroup(
                groupLayout.createSequentialGroup()
                        .addComponent(computerLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE,
                                Short.MAX_VALUE)
                        .addComponent(interruptionPanel, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE)));

        groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                .addComponent(interruptionPanel, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE,
                        GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
                .addComponent(computerLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));
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
                    computerLabel.setIcon(isComputerShowing ? COMPUTER_ICONS[1] : COMPUTER_ICONS[0]);
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
