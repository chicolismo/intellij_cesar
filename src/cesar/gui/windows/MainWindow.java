package cesar.gui.windows;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ToolTipManager;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;

import cesar.gui.dialogs.SaveTextDialog;
import cesar.gui.displays.RegisterDisplay;
import cesar.gui.displays.TextDisplay;
import cesar.gui.panels.ButtonPanel;
import cesar.gui.panels.ConditionPanel;
import cesar.gui.panels.ExecutionPanel;
import cesar.gui.panels.InstructionPanel;
import cesar.gui.panels.RegisterPanel;
import cesar.gui.panels.StatusBar;
import cesar.gui.tables.DataTable;
import cesar.gui.tables.DataTableModel;
import cesar.gui.tables.ProgramTable;
import cesar.gui.tables.ProgramTableModel;
import cesar.gui.tables.Table;
import cesar.gui.tables.TableModel;
import cesar.gui.utils.FileLoader;
import cesar.gui.utils.FileLoader.FileLoaderException;
import cesar.hardware.Cpu;
import cesar.utils.Base;

public class MainWindow extends JFrame {
    public static final long serialVersionUID = -4182598865843186332L;

    private static final String MEMORY_ERROR_FORMAT = "ERRO: Posição da memória inválida (%s)";

    private static final int MIN_MEMORY_ADDRESS = 0;
    private static final int MAX_MEMORY_ADDRESS = Cpu.MEMORY_SIZE - 1;

    private final int windowWidth;
    private final int windowHeight;

    private final Cpu cpu;

    private final ProgramWindow programWindow;

    private final ProgramTable programTable;
    private final ProgramTableModel programTableModel;
    private final SaveTextDialog saveTextDialog;

    private final DataWindow dataWindow;
    private final DataTable dataTable;

    private final DataTableModel dataTableModel;
    private final TextWindow textWindow;
    private final TextDisplay textDisplay;
    private final RegisterPanel registerPanel;
    private final ExecutionPanel executionPanel;

    private final ConditionPanel conditionPanel;
    private final InstructionPanel instructionPanel;
    private final ButtonPanel buttonPanel;

    private final MenuBar menuBar;

    private final StatusBar statusBar;

    private final FileLoader fileLoader;

    private boolean running;

    private Base currentBase;

    public MainWindow() {
        super("Cesar");
        setIconImage(
                Toolkit.getDefaultToolkit().getImage(MainWindow.class.getResource("/cesar/gui/assets/computer.png")));
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        // setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
        setFocusable(true);
        setAutoRequestFocus(true);
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        cpu = new Cpu();

        fileLoader = new FileLoader(this);

        running = false;
        currentBase = Base.DECIMAL;

        programWindow = new ProgramWindow(this, cpu);
        programTable = programWindow.getTable();
        programTableModel = (ProgramTableModel) programTable.getModel();
        saveTextDialog = new SaveTextDialog(this);

        dataWindow = new DataWindow(this, cpu);
        dataTable = dataWindow.getTable();
        dataTableModel = (DataTableModel) dataTable.getModel();

        textWindow = new TextWindow(this, cpu);
        textDisplay = textWindow.getDisplay();

        menuBar = new MenuBar();
        setJMenuBar(menuBar);

        registerPanel = new RegisterPanel();
        executionPanel = new ExecutionPanel();
        conditionPanel = new ConditionPanel();
        instructionPanel = new InstructionPanel();
        buttonPanel = new ButtonPanel();

        registerPanel.setAlignmentX(CENTER_ALIGNMENT);
        registerPanel.setAlignmentY(TOP_ALIGNMENT);

        buttonPanel.setAlignmentX(CENTER_ALIGNMENT);
        buttonPanel.setAlignmentY(BOTTOM_ALIGNMENT);

        statusBar = new StatusBar();
        statusBar.setText("Bem-vindos");
        statusBar.setMinimumSize(statusBar.getPreferredSize());

        add(createMainPanel());
        add(statusBar);
        pack();
        windowWidth = getWidth();
        windowHeight = getHeight();
        setResizable(false);

        menuBar.viewProgram.setState(true);
        menuBar.viewData.setState(true);
        menuBar.viewDisplay.setState(true);

        initEvents();

        buttonPanel.btnDec.doClick();

        programWindow.setSize(programWindow.getPreferredSize());
        dataWindow.setSize(dataWindow.getPreferredSize());
        updateSubWindowsPositions();
        programWindow.setVisible(true);
        dataWindow.setVisible(true);
        textWindow.setVisible(true);
        textDisplay.repaint();

        dataTable.scrollToRow(Cpu.DATA_START_ADDRESS, true);
        updateDisplays();

        final ToolTipManager ttm = ToolTipManager.sharedInstance();
        ttm.setInitialDelay(300);
    }

    private JPanel createMainPanel() {
        final JPanel middleRightPanel = new JPanel();
        middleRightPanel.setLayout(new BoxLayout(middleRightPanel, BoxLayout.Y_AXIS));
        middleRightPanel.add(conditionPanel);
        middleRightPanel.add(Box.createVerticalGlue());
        middleRightPanel.add(buttonPanel);

        final JPanel middlePanel = new JPanel();
        middlePanel.setLayout(new BoxLayout(middlePanel, BoxLayout.X_AXIS));
        middlePanel.add(executionPanel);
        middlePanel.add(middleRightPanel);

        final JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(registerPanel);
        mainPanel.add(middlePanel);
        mainPanel.add(instructionPanel);
        mainPanel.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1),
                BorderFactory.createBevelBorder(BevelBorder.LOWERED)));
        return mainPanel;
    }

    public void executeNextInstruction() {
        final Cpu.ExecutionResult result = cpu.executeNextInstruction();
        statusBar.setText(result.toString());

        switch (result) {
            case HALT:
                stopRunning();
                buttonPanel.btnRun.setSelected(false);
            case NOOP:
            case OK:
                updateDisplays();
                executionPanel.incrementInstructions();
                if (cpu.hasMemoryChanged()) {
                    final int start = cpu.getLastChangedAddress();
                    final int end = cpu.getLastChangedMnemonic();
                    programTableModel.fireTableRowsUpdated(start, end);
                    dataTableModel.fireTableRowsUpdated(start, start + 1);
                    textDisplay.repaint();
                }
                updateProgramCounterRow();
                break;

            case BREAK_POINT:

            case INVALID_INSTRUCTION:
                stopRunning();
                break;
        }
    }

    private void goTo() {
        int radix = currentBase.toInt();
        final String input = (String) JOptionPane.showInputDialog(this, "Digite a posição de memória a ir:",
                "Ir Para...", JOptionPane.PLAIN_MESSAGE, null, null, Integer.toString(MIN_MEMORY_ADDRESS, radix));
        try {
            final int address = Integer.parseInt(input, radix);
            if (isValidMemoryAddress(address)) {
                programWindow.clickOnRow(address);
            }
            else {
                setTempMessage(String.format(MEMORY_ERROR_FORMAT, input));
            }
        }
        catch (final NumberFormatException e) {
            setTempMessage(String.format(MEMORY_ERROR_FORMAT, input));
        }
    }


    private void initButtonEvents() {
        buttonPanel.btnNext.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                executeNextInstruction();
            }
        });

        buttonPanel.btnRun.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                if (buttonPanel.btnRun.isSelected() && !isRunning()) {
                    startRunning();
                }
                else {
                    stopRunning();
                }
            }
        });

        buttonPanel.btnDec.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                setBase(Base.DECIMAL);
                menuBar.editDecimal.setSelected(true);
            }
        });

        buttonPanel.btnHex.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                setBase(Base.HEXADECIMAL);
                menuBar.editHexadecimal.setSelected(true);
            }
        });
    }

    private void initEvents() {
        initKeyListenerEvents();
        initRegisterDisplayEvents();
        initMenuEvents();
        initButtonEvents();
        initSideWindowEvents();
    }

    private void initKeyListenerEvents() {
        final MainWindow mainWindow = this;

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentMoved(final ComponentEvent e) {
                updateSubWindowsPositions();
            }
        });

        final KeyListener keyListener = new KeyListener() {
            @Override
            public void keyPressed(final KeyEvent event) {
                menuBar.dispatchEvent(event);
                mainWindow.requestFocus();
            }

            @Override
            public void keyReleased(final KeyEvent event) {
                menuBar.dispatchEvent(event);
                mainWindow.requestFocus();
            }

            @Override
            public void keyTyped(final KeyEvent event) {
                menuBar.dispatchEvent(event);
                cpu.setTypedKey((byte) event.getKeyChar());
                programTableModel.fireTableRowsUpdated(Cpu.KEYBOARD_STATE_ADDRESS, Cpu.LAST_CHAR_ADDRESS);
                dataTableModel.fireTableRowsUpdated(Cpu.KEYBOARD_STATE_ADDRESS, Cpu.LAST_CHAR_ADDRESS);
                mainWindow.requestFocus();
            }
        };

        mainWindow.addKeyListener(keyListener);
        programWindow.addKeyListener(keyListener);
        dataWindow.addKeyListener(keyListener);
        textWindow.addKeyListener(keyListener);
    }

    private void initMenuEvents() {
        final Component[][] pairs = new Component[][] { { programWindow, menuBar.viewProgram },
            { dataWindow, menuBar.viewData }, { textWindow, menuBar.viewDisplay } };

        for (final Component[] pair : pairs) {
            final JDialog sideWindow = (JDialog) pair[0];
            final JCheckBoxMenuItem checkBox = (JCheckBoxMenuItem) pair[1];

            sideWindow.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentHidden(final ComponentEvent e) {
                    super.componentHidden(e);
                    checkBox.setState(false);
                }
            });

            checkBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent actionEvent) {
                    sideWindow.setVisible(checkBox.getState());
                }
            });
        }

        menuBar.fileLoad.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                loadFile();
            }
        });

        menuBar.fileLoadPartially.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                loadFilePartially();
            }
        });


        menuBar.fileSaveText.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                saveAsText();
            }
        });

        menuBar.fileExit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent actionEvent) {
                // TODO: Testar se o arquivo aberto foi alterado.
                System.exit(0);
            }
        });

        menuBar.editGoto.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent event) {
                goTo();
            }
        });

        menuBar.editZeroMemory.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                zeroMemory();
            }
        });

        menuBar.editCopyMemory.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
            }
        });

        menuBar.editDecimal.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                buttonPanel.btnDec.doClick();
            }
        });

        menuBar.editHexadecimal.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                buttonPanel.btnHex.doClick();
            }
        });
    }

    private void initRegisterDisplayEvents() {
        final MouseListener registerPanelMouseListener = new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent event) {
                if (event.getClickCount() == 2) {
                    final RegisterDisplay display = (RegisterDisplay) event.getSource();
                    showNewRegisterValueDialog(display, display.getNumber());
                }
            }
        };

        for (final RegisterDisplay display : registerPanel.getDisplays()) {
            display.addMouseListener(registerPanelMouseListener);
        }
    }

    private void initSideWindowEvents() {
        for (final SideWindow<?, ?> window : new SideWindow<?, ?>[] { programWindow, dataWindow }) {
            final Table table = window.getTable();
            final TableModel model = (TableModel) table.getModel();
            final JLabel addressLabel = window.getAddressLabel();
            final JTextField valueField = window.getValueField();

            table.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(final MouseEvent e) {
                    System.out.println(e);
                    if (table.getSelectedRow() >= 0) {
                        final int row = table.getSelectedRow();
                        final String address = model.getAddressAsString(row);
                        final String value = model.getValueAsString(row);
                        addressLabel.setText(String.format(SideWindow.LABEL_FORMAT, address));
                        valueField.setText(value);
                        valueField.requestFocus();
                        valueField.selectAll();

                        final int radix = Base.toInt(model.getBase());
                        final int currentAddress = Integer.parseInt(address, radix);
                        final int currentValue = Integer.parseInt(value, radix);

                        window.setCurrentAddress(currentAddress);
                        window.setCurrentValue(currentValue);
                    }
                }
            });

            valueField.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent actionEvent) {
                    final int radix = Base.toInt(model.getBase());
                    final String value = valueField.getText();
                    final int newValue;
                    try {
                        newValue = Integer.parseInt(value, radix);
                    }
                    catch (final NumberFormatException exception) {
                        // Se o valor digitado for inválido, ignorar o ENTER
                        return;
                    }
                    if (newValue <= 0xFF && newValue >= Byte.MIN_VALUE) {
                        window.setCurrentValue(newValue);
                        int currentAddress = window.getCurrentAddress();
                        cpu.setByte(currentAddress, (byte) (0xFF & newValue));
                        programTableModel.fireTableDataChanged();
                        dataTableModel.fireTableDataChanged();
                        if (Cpu.isIOAddress(currentAddress)) {
                            textDisplay.repaint();
                        }
                        // Seleciona a próxima linha
                        currentAddress = 0xFFFF & currentAddress + 1;
                        table.setRowSelectionInterval(currentAddress, currentAddress);
                        valueField.setText(model.getValueAsString(currentAddress));
                        table.scrollToRow(currentAddress);
                        valueField.requestFocus();
                        valueField.selectAll();
                        window.setCurrentAddress(currentAddress);
                    }
                }
            });
        }

        programTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent event) {
                final int selectedRow;
                if (event.getClickCount() == 2 && (selectedRow = programTable.getSelectedRow()) != -1) {
                    cpu.setRegister(7, (short) (0xFFFF & selectedRow));
                    updateInterface();
                }
            }
        });
    }

    synchronized private boolean isRunning() {
        return running;
    }

    private boolean isValidMemoryAddress(int address) {
        return address >= MIN_MEMORY_ADDRESS && address <= MAX_MEMORY_ADDRESS;
    }

    private void loadFile() {
        try {
            if (fileLoader.loadFile(cpu)) {
                updateInterface();
                dataTable.scrollToRow(Cpu.DATA_START_ADDRESS, true);
            }
        }
        catch (final FileLoaderException e) {
            setTempMessage(e.getMessage());
        }
    }

    private void loadFilePartially() {
        try {
            if (fileLoader.loadFilePartially(cpu, currentBase)) {
                updateInterface();
            }
        }
        catch (final FileLoaderException e) {
            setTempMessage(e.getMessage());
        }
    }

    private void saveAsText() {
        saveTextDialog.saveText(cpu, currentBase);
    }

    private void setBase(final Base base) {
        currentBase = base;
        programWindow.setBase(base);
        dataWindow.setBase(base);
        registerPanel.setBase(base);
    }

    /**
     * Escreve uma mensagem temporária na barra de status.
     *
     * @param message A mensagem a ser escrita na barra de status.
     */
    private void setTempMessage(final String message) {
        final long milliseconds = 3000;
        final String currentText = statusBar.getText();

        final Thread tempThread = new Thread(new Runnable() {
            @Override
            public void run() {
                statusBar.setText(message);
                try {
                    Thread.sleep(milliseconds);
                }
                catch (final InterruptedException e) {
                    e.printStackTrace();
                }
                statusBar.setText(currentText);
            }
        });
        tempThread.start();
    }

    private void showNewRegisterValueDialog(final RegisterDisplay display, final int registerNumber) {
        statusBar.clear();
        final int radix = currentBase.toInt();
        final String input = JOptionPane.showInputDialog(display,
                String.format("Digite um valor %s para o registrador %d", currentBase.toString(), registerNumber),
                Integer.toString(cpu.getRegister(registerNumber), radix));
        if (input != null) {
            try {
                final int newValue = Integer.parseInt(input, radix);
                if (newValue <= 0xFFFF && newValue >= Short.MIN_VALUE) {
                    cpu.setRegister(registerNumber, (short) (0xFFFF & newValue));
                    updateInterface();
                }
                else {
                    throw new NumberFormatException("O valor digitado excede o tamanho da palavra: " + newValue);
                }
            }
            catch (final NumberFormatException e) {
                statusBar.setText(e.getMessage());
            }
        }
    }

    synchronized private void startRunning() {
        running = true;
        final Thread runningThread = new Thread(new Runnable() {
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

    synchronized private void stopRunning() {
        running = false;
    }

    private void updateDisplays() {
        for (int i = 0; i < Cpu.REGISTER_COUNT; ++i) {
            registerPanel.getDisplay(i).setValue(cpu.getRegister(i));
        }
        conditionPanel.setNegative(cpu.isNegative());
        conditionPanel.setZero(cpu.isZero());
        conditionPanel.setOverflow(cpu.isOverflow());
        conditionPanel.setCarry(cpu.isCarry());
        executionPanel.setMemoryAccessCount(cpu.getMemoryAccessCount());
        instructionPanel.setRiText(cpu.getReadInstruction());
        instructionPanel.setMnemonicText(cpu.getReadMnemonic());
    }

    private void updateInterface() {
        updateDisplays();
        updateProgramCounterRow();
        repaint();
        textWindow.repaint();
        programTable.scrollToRow(programTableModel.getProgramCounterRow());
        programWindow.repaint();
        dataWindow.repaint();
    }

    private void updateProgramCounterRow() {
        final int programCounter = cpu.getProgramCounter();
        programTableModel.setProgramCounterRow(programCounter);
        programTable.setRowSelectionInterval(programCounter, programCounter);
        if (!isRunning()) {
            programTable.scrollToRow(programCounter);
        }
    }

    private void updateSubWindowsPositions() {
        final int gap = 6;
        final Point pos = getLocation();
        final Dimension programWindowSize = programWindow.getSize();
        final Dimension programWindowPreferredSize = programWindow.getPreferredSize();
        final Dimension dataWindowPreferredSize = dataWindow.getPreferredSize();
        programWindow.setLocation(pos.x - programWindowSize.width - gap, pos.y);
        dataWindow.setLocation(pos.x + windowWidth + gap, pos.y);
        programWindow.setSize(programWindowPreferredSize.width, windowHeight);
        dataWindow.setSize(dataWindowPreferredSize.width, windowHeight);
        textWindow.setLocation(pos.x - programWindowSize.width - gap, pos.y + windowHeight + gap);
        requestFocus();
    }

    private void zeroMemory() {
        int radix = currentBase.toInt();
        String input = null;
        int startAddress;
        int endAddress;
        try {
            input = (String) JOptionPane.showInputDialog(this, "Digite o endereço inicial:", "Zerar Memória",
                    JOptionPane.PLAIN_MESSAGE, null, null, Integer.toString(MIN_MEMORY_ADDRESS, radix));
            if (input == null) {
                return;
            }

            startAddress = Integer.parseInt(input, radix);
            if (!isValidMemoryAddress(startAddress)) {
                setTempMessage(String.format(MEMORY_ERROR_FORMAT, input));
                return;
            }

            input = (String) JOptionPane.showInputDialog(this, "Digite o endereço final:", "Zerar Memória",
                    JOptionPane.PLAIN_MESSAGE, null, null, Integer.toString(MAX_MEMORY_ADDRESS, radix));

            endAddress = Integer.parseInt(input, radix);
            if (!isValidMemoryAddress(endAddress) || endAddress < startAddress) {
                setTempMessage(String.format(MEMORY_ERROR_FORMAT, input));
                return;
            }

            final byte zero = 0;
            Arrays.fill(cpu.getMemory(), startAddress, endAddress + 1, zero);
            cpu.updateMnemonics();
            updateInterface();
        }
        catch (NumberFormatException e) {
            setTempMessage(String.format(MEMORY_ERROR_FORMAT, input));
        }
    }
}
