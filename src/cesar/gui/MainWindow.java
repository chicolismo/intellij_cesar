package cesar.gui;

import java.awt.Dimension;
import java.awt.Point;
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
import cesar.utils.Defaults;

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

    public MainWindow() {
        super("Cesar");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setFocusable(true);
        setAutoRequestFocus(true);
        BoxLayout mainLayout = new BoxLayout(getContentPane(), BoxLayout.Y_AXIS);
        getContentPane().setLayout(mainLayout);

        JPanel mainPanel = new JPanel();
        Border border = BorderFactory.createCompoundBorder(Defaults.createEmptyBorder(1),
                BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        mainPanel.setBorder(border);
        var vbox = new BoxLayout(mainPanel, BoxLayout.Y_AXIS);
        mainPanel.setLayout(vbox);

        cpu = new Cpu();
        programPanel = new ProgramPanel(this, cpu.getMemory());
        programTableModel = (ProgramTableModel) programPanel.getTable().getModel();

        dataPanel = new DataPanel(this, cpu.getMemory());
        dataTableModel = (DataTableModel) dataPanel.getTable().getModel();

        textDisplay = new TextDisplay(cpu.getMemory());

        menuBar = new MenuBar();
        setJMenuBar(menuBar);

        registerDisplays = new RegisterDisplay[8];

        programPanel.setSize(programPanel.getPreferredSize());
        dataPanel.setSize(dataPanel.getPreferredSize());

        var registerPanel = new RegisterPanel();
        for (int i = 0; i < 8; ++i) {
            registerDisplays[i] = registerPanel.getDisplay(i);
        }
        registerPanel.setAlignmentX(CENTER_ALIGNMENT);
        mainPanel.add(registerPanel);

        var executionPanel = new ExecutionPanel();
        var conditionPanel = new ConditionPanel();
        var buttonPanel = new ButtonPanel();


        decimalButton = buttonPanel.getDecButton();
        hexadecimalButton = buttonPanel.getHexButton();
        decimalButton.doClick();

        var middleRightPanel = new JPanel();
        var middleRightBox = new BoxLayout(middleRightPanel, BoxLayout.Y_AXIS);
        middleRightPanel.setLayout(middleRightBox);
        middleRightPanel.add(conditionPanel);
        middleRightPanel.add(buttonPanel);

        var middlePanel = new JPanel();
        var hbox = new BoxLayout(middlePanel, BoxLayout.X_AXIS);
        middlePanel.setLayout(hbox);
        middlePanel.add(executionPanel);
        middlePanel.add(middleRightPanel);

        mainPanel.add(middlePanel);

        var instructionPanel = new InstructionPanel();
        mainPanel.add(instructionPanel);

        var statusBar = new StatusBar();
        statusBar.setText("Bem-vindos");

        add(mainPanel);
        add(statusBar);

        initEvents();
        pack();
        setResizable(false);

        textPanel = new JDialog();
        textPanel.add(textDisplay);
        textPanel.pack();
        textPanel.setResizable(false);

        updatePositions();
        programPanel.setVisible(true);
        dataPanel.setVisible(true);
        textPanel.setVisible(true);

        textDisplay.repaint();
    }

    private void initEvents() {
        final var parent = this;
        final var fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileFilter(new FileNameExtensionFilter("Arquivos de memória", "mem"));

        menuBar.fileOpen.addActionListener(actionEvent -> {
            if (fileChooser.showDialog(parent, null) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                System.out.println(file.getName());
            }
        });

        menuBar.fileExit.addActionListener(actionEvent -> {
            // TODO: Testar se o arquivo aberto foi alterado.
            System.exit(0);
        });

        menuBar.viewProgram.addActionListener(actionEvent -> {
            programPanel.setVisible(true);
        });

        menuBar.viewData.addActionListener(actionEvent -> {
            dataPanel.setVisible(true);
        });

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentMoved(ComponentEvent e) {
                updatePositions();
            }
        });

        decimalButton.addActionListener((e) -> {
            programTableModel.setBase(Base.DECIMAL);
            dataTableModel.setBase(Base.DECIMAL);
            for (var register : registerDisplays) {
                register.setBase(Base.DECIMAL);
            }
        });

        hexadecimalButton.addActionListener((e) -> {
            programTableModel.setBase(Base.HEXADECIMAL);
            dataTableModel.setBase(Base.HEXADECIMAL);
            for (var register : registerDisplays) {
                register.setBase(Base.HEXADECIMAL);
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
}
