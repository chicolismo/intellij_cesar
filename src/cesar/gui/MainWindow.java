package cesar.gui;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import cesar.gui.displays.RegisterDisplay;
import cesar.gui.displays.TextDisplay;
import cesar.gui.panels.ButtonPanel;
import cesar.gui.panels.ConditionPanel;
import cesar.gui.panels.ExecutionPanel;
import cesar.gui.panels.InstructionPanel;
import cesar.gui.panels.RegisterPanel;
import cesar.gui.panels.StatusBar;
import cesar.gui.tables.DataTableModel;
import cesar.gui.tables.ProgramTableModel;
import cesar.hardware.Cpu;

public class MainWindow extends JFrame {
    public static final long serialVersionUID = -4182598865843186332L;

    private final MenuBar menuBar;
    private final Cpu cpu;
    private final ProgramPanel programPanel;
    private final DataPanel dataPanel;
    private final ProgramTableModel programTableModel;
    private final DataTableModel dataTableModel;
    private final JDialog textPanel;
    private final TextDisplay textDisplay;
    private final RegisterDisplay[] registerDisplays;
    private final JToggleButton decimalButton;
    private final JToggleButton hexadecimalButton;
    private final StatusBar statusBar;

    public MainWindow() {
        super("Cesar");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
        setFocusable(true);
        setAutoRequestFocus(true);
        BoxLayout mainLayout = new BoxLayout(getContentPane(), BoxLayout.Y_AXIS);
        getContentPane().setLayout(mainLayout);


        cpu = new Cpu();
        programPanel = new ProgramPanel(this, cpu.getMemory());
        dataPanel = new DataPanel(this, cpu.getMemory());

        programTableModel = (ProgramTableModel) programPanel.getTable().getModel();
        dataTableModel = (DataTableModel) dataPanel.getTable().getModel();

        textDisplay = new TextDisplay(cpu.getMemory());

        menuBar = new MenuBar();
        setJMenuBar(menuBar);

        registerDisplays = new RegisterDisplay[8];

        final RegisterPanel registerPanel = new RegisterPanel();
        for (int i = 0; i < 8; ++i) {
            registerDisplays[i] = registerPanel.getDisplay(i);
        }
        registerPanel.setAlignmentX(CENTER_ALIGNMENT);


        final ExecutionPanel executionPanel = new ExecutionPanel();
        final ConditionPanel conditionPanel = new ConditionPanel();
        final ButtonPanel buttonPanel = new ButtonPanel();

        decimalButton = buttonPanel.getDecButton();
        hexadecimalButton = buttonPanel.getHexButton();
        decimalButton.doClick();

        final JPanel middleRightPanel = new JPanel();
        final BoxLayout middleRightBox = new BoxLayout(middleRightPanel, BoxLayout.Y_AXIS);
        middleRightPanel.setLayout(middleRightBox);
        middleRightPanel.add(conditionPanel);
        middleRightPanel.add(buttonPanel);

        final JPanel middlePanel = new JPanel();
        final BoxLayout hbox = new BoxLayout(middlePanel, BoxLayout.X_AXIS);
        middlePanel.setLayout(hbox);
        middlePanel.add(executionPanel);
        middlePanel.add(middleRightPanel);

        final InstructionPanel instructionPanel = new InstructionPanel();

        final JPanel mainPanel = createMainPanel();
        mainPanel.add(registerPanel);
        mainPanel.add(middlePanel);
        mainPanel.add(instructionPanel);

        statusBar = new StatusBar();
        statusBar.setText("Bem-vindos");

        add(mainPanel);
        add(statusBar);

        pack();
        setResizable(false);

        textPanel = createTextPanel(textDisplay);

        programPanel.setSize(programPanel.getPreferredSize());
        dataPanel.setSize(dataPanel.getPreferredSize());

        initEvents();
        updatePositions();
        programPanel.setVisible(true);
        dataPanel.setVisible(true);
        textPanel.setVisible(true);
        textDisplay.repaint();
    }

    private void initEvents() {
        final MainWindow parent = this;
        final JFileChooser fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileFilter(new FileNameExtensionFilter("Arquivos de memória", "mem"));

        menuBar.fileOpen.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (fileChooser.showDialog(parent, null) == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    System.out.println(file.getName());
                }
            }
        });

        menuBar.fileExit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                // TODO: Testar se o arquivo aberto foi alterado.
                System.exit(0);
            }
        });

        menuBar.viewProgram.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                programPanel.setVisible(true);
            }
        });

        menuBar.viewData.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                dataPanel.setVisible(true);
            }
        });

        menuBar.viewDisplay.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                textPanel.setVisible(true);
            }
        });

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentMoved(ComponentEvent e) {
                updatePositions();
            }
        });

        decimalButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                programPanel.setBase(Base.DECIMAL);
                dataPanel.setBase(Base.DECIMAL);
                for (RegisterDisplay register : registerDisplays) {
                    register.setBase(Base.DECIMAL);
                }
            }
        });

        hexadecimalButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                programPanel.setBase(Base.HEXADECIMAL);
                dataPanel.setBase(Base.HEXADECIMAL);
                for (RegisterDisplay register : registerDisplays) {
                    register.setBase(Base.HEXADECIMAL);
                }
            }
        });
    }

    private void updatePositions() {
        final int gap = 6;
        final int width = getWidth();
        final int height = getHeight();
        final Point location = getLocation();
        final Dimension programWindowSize = programPanel.getSize();
        final Dimension programSize = programPanel.getPreferredSize();
        final Dimension dataSize = dataPanel.getPreferredSize();
        programPanel.setLocation(location.x - programWindowSize.width - gap, location.y);
        dataPanel.setLocation(location.x + width + gap, location.y);
        programPanel.setSize(programSize.width, height);
        dataPanel.setSize(dataSize.width, height);
        textPanel.setLocation(location.x - programWindowSize.width - gap, location.y + height + gap);
        this.requestFocus();
    }

    private JPanel createMainPanel() {
        final JPanel mainPanel = new JPanel();
        final BoxLayout mainPanelLayout = new BoxLayout(mainPanel, BoxLayout.Y_AXIS);
        mainPanel.setLayout(mainPanelLayout);
        final Border outer = BorderFactory.createEmptyBorder(1, 1, 1, 1);
        final Border inner = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
        mainPanel.setBorder(new CompoundBorder(outer, inner));
        return mainPanel;
    }

    private JDialog createTextPanel(TextDisplay textDisplay) {
        final JDialog textPanel = new JDialog(this, "Display");
        textPanel.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        textPanel.setFocusable(false);
        textPanel.getContentPane().add(textDisplay);
        textPanel.pack();
        textPanel.setResizable(false);
        return textPanel;
    }
}
