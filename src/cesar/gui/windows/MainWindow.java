package cesar.gui.windows;

import cesar.gui.displays.RegisterDisplay;
import cesar.gui.displays.TextDisplay;
import cesar.gui.panels.*;
import cesar.gui.tables.DataTable;
import cesar.gui.tables.DataTableModel;
import cesar.gui.tables.ProgramTable;
import cesar.gui.tables.ProgramTableModel;
import cesar.hardware.Cpu;
import cesar.utils.Base;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class MainWindow extends JFrame {
    public static final long serialVersionUID = -4182598865843186332L;

    private final Cpu cpu;

    private final ProgramWindow programWindow;
    private final ProgramTable programTable;
    private final ProgramTableModel programTableModel;

    private final DataWindow dataWindow;
    private final DataTable dataTable;
    private final DataTableModel dataTableModel;

    private final JDialog textPanel;
    private final TextDisplay textDisplay;

    private final RegisterDisplay[] registerDisplays;
    private final ExecutionPanel executionPanel;
    private final ConditionPanel conditionPanel;

    private final JButton nextButton;
    private final JToggleButton runButton;
    private final JToggleButton decimalButton;
    private final JToggleButton hexadecimalButton;

    private final MenuBar menuBar;

    private final StatusBar statusBar;

    private boolean running;

    public MainWindow() {
        super("Cesar");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
        setFocusable(true);
        setAutoRequestFocus(true);
        BoxLayout mainLayout = new BoxLayout(getContentPane(), BoxLayout.Y_AXIS);
        getContentPane().setLayout(mainLayout);

        cpu = new Cpu();

        running = false;

        programWindow = new ProgramWindow(this, cpu);
        programTable = programWindow.getTable();
        programTableModel = (ProgramTableModel) programTable.getModel();

        dataWindow = new DataWindow(this, cpu);
        dataTable = dataWindow.getTable();
        dataTableModel = (DataTableModel) dataTable.getModel();

        textDisplay = new TextDisplay(cpu);

        menuBar = new MenuBar();
        setJMenuBar(menuBar);


        final RegisterPanel registerPanel = new RegisterPanel();
        registerPanel.setAlignmentX(CENTER_ALIGNMENT);

        registerDisplays = registerPanel.getDisplays();

        executionPanel = new ExecutionPanel();
        conditionPanel = new ConditionPanel();
        final ButtonPanel buttonPanel = new ButtonPanel();

        nextButton = buttonPanel.getNextButton();
        runButton = buttonPanel.getRunButton();
        decimalButton = buttonPanel.getDecButton();
        hexadecimalButton = buttonPanel.getHexButton();
        decimalButton.doClick();

        final JPanel middleRightPanel = new JPanel();
        final BoxLayout middleRightBox = new BoxLayout(middleRightPanel, BoxLayout.Y_AXIS);
        middleRightPanel.setLayout(middleRightBox);
        middleRightPanel.add(conditionPanel);
        middleRightPanel.add(buttonPanel);

        final JPanel middlePanel = new JPanel();
        final BoxLayout horizontalBox = new BoxLayout(middlePanel, BoxLayout.X_AXIS);
        middlePanel.setLayout(horizontalBox);
        middlePanel.add(executionPanel);
        middlePanel.add(middleRightPanel);

        final InstructionPanel instructionPanel = new InstructionPanel();

        final JPanel mainPanel = createMainPanel();
        mainPanel.add(registerPanel);
        mainPanel.add(middlePanel);
        mainPanel.add(instructionPanel);

        statusBar = new StatusBar();
        statusBar.setText("Bem-vindos");
        statusBar.setMinimumSize(statusBar.getPreferredSize());

        add(mainPanel);
        add(statusBar);

        pack();
        setResizable(false);

        textPanel = createTextPanel(textDisplay);

        programWindow.setSize(programWindow.getPreferredSize());
        dataWindow.setSize(dataWindow.getPreferredSize());

        initEvents();
        updatePositions();
        programWindow.setVisible(true);
        dataWindow.setVisible(true);
        textPanel.setVisible(true);
        textDisplay.repaint();
        dataTable.scrollToRow(1024, true);
    }

    synchronized private boolean isRunning() {
        return running;
    }

    synchronized private void stopRunning() {
        running = false;
    }

    synchronized private void startRunning() {
        running = true;
        Thread runningThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (isRunning()) {
                    MainWindow.this.executeNextInstruction();
                }
                updateInterface();
            }
        });
        runningThread.start();
    }

    private void initEvents() {
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentMoved(ComponentEvent e) {
                updatePositions();
            }
        });

        final JFileChooser fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileFilter(new FileNameExtensionFilter("Arquivos de memória", "mem"));

        menuBar.fileOpen.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (fileChooser.showDialog(MainWindow.this, null) == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    MainWindow.this.onOpenFile(file);
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
                programWindow.setVisible(true);
            }
        });

        menuBar.viewData.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                dataWindow.setVisible(true);
            }
        });

        menuBar.viewDisplay.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                textPanel.setVisible(true);
            }
        });

        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MainWindow.this.executeNextInstruction();
            }
        });

        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (runButton.isSelected() && !isRunning()) {
                    startRunning();
                } else {
                    stopRunning();
                }
            }
        });

        decimalButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                programWindow.setBase(Base.DECIMAL);
                dataWindow.setBase(Base.DECIMAL);
                for (RegisterDisplay register : registerDisplays) {
                    register.setBase(Base.DECIMAL);
                }
            }
        });

        hexadecimalButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                programWindow.setBase(Base.HEXADECIMAL);
                dataWindow.setBase(Base.HEXADECIMAL);
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
        final Dimension programWindowSize = programWindow.getSize();
        final Dimension programSize = programWindow.getPreferredSize();
        final Dimension dataSize = dataWindow.getPreferredSize();
        programWindow.setLocation(location.x - programWindowSize.width - gap, location.y);
        dataWindow.setLocation(location.x + width + gap, location.y);
        programWindow.setSize(programSize.width, height);
        dataWindow.setSize(dataSize.width, height);
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

    private void onOpenFile(File file) {
        try {
            final FileInputStream inputStream = new FileInputStream(file);
            final int size = (int) file.length();
            final byte[] buffer = new byte[size];
            final int bytesRead = inputStream.read(buffer, 0, size);
            if (bytesRead != size) {
                inputStream.close();
                throw new IOException("Não foi possível ler todos os bytes do arquivo");
            }
            cpu.setMemory(buffer);
            programWindow.repaint();
            dataWindow.repaint();
            textPanel.repaint();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Um erro ocorreu ao abrir o arquivo",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    public void executeNextInstruction() {
        Cpu.ExecutionResult result = cpu.executeNextInstruction();
        statusBar.setText(result.toString());

        switch (result) {
            case HALT:
                stopRunning();
                runButton.setSelected(false);
            case NOOP:
            case OK:
                updateDisplays();
                executionPanel.incrementInstructions();

                if (cpu.hasMemoryChanged()) {
                    final int address = cpu.getLastChangedAddress();
                    programTableModel.fireTableRowsUpdated(address, address + 1);
                    dataTableModel.fireTableRowsUpdated(address, address + 1);
                    textDisplay.repaint();
                }

                final int programCounter = cpu.getProgramCounter();
                programTableModel.setPcRow(programCounter);
                programTable.setRowSelectionInterval(programCounter, programCounter);
                if (!isRunning()) {
                    programTable.scrollToRow(programCounter);
                }

                break;

            case INVALID_INSTRUCTION:
                stopRunning();
                break;
        }
    }

    private void updateDisplays() {
        for (int i = 0; i < registerDisplays.length; ++i) {
            registerDisplays[i].setValue(cpu.getRegister(i));
        }
        conditionPanel.setNegative(cpu.isNegative());
        conditionPanel.setZero(cpu.isZero());
        conditionPanel.setOverflow(cpu.isOverflow());
        conditionPanel.setCarry(cpu.isCarry());
        executionPanel.setMemoryAccessCount(cpu.getMemoryAccessCount());
    }

    private void updateInterface() {
        repaint();
        textPanel.repaint();
        programTable.scrollToRow(programTableModel.getPcRow(), true);
        programWindow.repaint();
        dataWindow.repaint();
    }
}
