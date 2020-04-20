package cesar.gui.windows;

import cesar.gui.displays.RegisterDisplay;
import cesar.gui.displays.TextDisplay;
import cesar.gui.panels.*;
import cesar.gui.tables.*;
import cesar.hardware.Cpu;
import cesar.utils.Base;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class MainWindow extends JFrame {
    public static final long serialVersionUID = -4182598865843186332L;

    private final int windowWidth;
    private final int windowHeight;

    private final Cpu cpu;

    private final ProgramWindow programWindow;
    private final ProgramTable programTable;
    private final ProgramTableModel programTableModel;

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
    private final JFileChooser fileChooser;

    private boolean running;

    private Base currentBase;

    public MainWindow() {
        super("Cesar");
        setIconImage(
                Toolkit.getDefaultToolkit().getImage(MainWindow.class.getResource("/cesar/gui/assets/computer.png")));
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
        setFocusable(true);
        setAutoRequestFocus(true);
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        cpu = new Cpu();

        fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileFilter(new FileNameExtensionFilter("Arquivos do Cesar (*.mem)", "mem"));

        running = false;
        currentBase = Base.DECIMAL;

        programWindow = new ProgramWindow(this, cpu);
        programTable = programWindow.getTable();
        programTableModel = (ProgramTableModel) programTable.getModel();

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
        buttonPanel.btnDec.doClick();

        registerPanel.setAlignmentX(CENTER_ALIGNMENT);
        buttonPanel.setAlignmentY(CENTER_ALIGNMENT);

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

        programWindow.setSize(programWindow.getPreferredSize());
        dataWindow.setSize(dataWindow.getPreferredSize());
        updateSubWindowsPositions();
        programWindow.setVisible(true);
        dataWindow.setVisible(true);
        textWindow.setVisible(true);
        textDisplay.repaint();

        dataTable.scrollToRow(Cpu.DATA_START_ADDRESS, true);
        updateDisplays();
    }

    private JPanel createMainPanel() {
        final JPanel middleRightPanel = new JPanel();
        middleRightPanel.setLayout(new BoxLayout(middleRightPanel, BoxLayout.Y_AXIS));
        middleRightPanel.add(conditionPanel);
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

    synchronized private boolean isRunning() {
        return running;
    }

    synchronized private void stopRunning() {
        running = false;
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

    private void initEvents() {
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentMoved(final ComponentEvent e) {
                updateSubWindowsPositions();
            }
        });

        final KeyListener keyListener = new KeyAdapter() {
            @Override
            public void keyPressed(final KeyEvent event) {
                if (event.getModifiersEx() == 0) {
                    // TODO: Testar se está no intervalo válido do alfabeto
                    byte keyValue = (byte) (0xFF & event.getKeyCode());
                    cpu.setTypedKey(keyValue);
                    programTableModel.fireTableRowsUpdated(Cpu.KEYBOARD_STATE_ADDRESS, Cpu.LAST_CHAR_ADDRESS);
                    dataTableModel.fireTableRowsUpdated(Cpu.KEYBOARD_STATE_ADDRESS, Cpu.LAST_CHAR_ADDRESS);
                }
            }
        };

        addKeyListener(keyListener);
        textWindow.addKeyListener(keyListener);

        for (final RegisterDisplay display : registerPanel.getDisplays()) {
            final int registerNumber = display.getNumber();
            display.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(final MouseEvent event) {
                    if (event.getClickCount() == 2) {
                        showNewRegisterValueDialog(display, registerNumber);
                    }
                }
            });
        }

        final Component[][] pairs = new Component[][] {
                { programWindow, menuBar.viewProgram },
                { dataWindow, menuBar.viewData }, { textWindow, menuBar.viewDisplay }
        };

        for (final Component[] pair : pairs) {
            final JDialog subWindow = (JDialog) pair[0];
            final JCheckBoxMenuItem checkBox = (JCheckBoxMenuItem) pair[1];

            subWindow.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentHidden(final ComponentEvent e) {
                    super.componentHidden(e);
                    checkBox.setState(false);
                }
            });

            checkBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent actionEvent) {
                    subWindow.setVisible(checkBox.getState());
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

        menuBar.fileOpen.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openFile();
            }
        });

        menuBar.fileExit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent actionEvent) {
                // TODO: Testar se o arquivo aberto foi alterado.
                System.exit(0);
            }
        });

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

        final Object[][] buttons = new Object[][] {
                { buttonPanel.btnDec, Base.DECIMAL },
                { buttonPanel.btnHex, Base.HEXADECIMAL }
        };

        for (final Object[] pair : buttons) {
            final JToggleButton button = (JToggleButton) pair[0];
            final Base base = (Base) pair[1];
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent actionEvent) {
                    setBase(base);
                }
            });
        }

        for (final SideWindow<?, ?> window : new SideWindow<?, ?>[] { programWindow, dataWindow }) {
            final Table table = window.getTable();
            final TableModel model = (TableModel) table.getModel();
            final JLabel addressLabel = window.getAddressLabel();
            final JTextField valueField = window.getValueField();

            table.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(final MouseEvent e) {
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
                        model.setValue(currentAddress, (byte) (0xFF & window.getCurrentValue()));
                        model.fireTableDataChanged();
                        if (cpu.isIOAddress(currentAddress)) {
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
    }

    private void setBase(final Base base) {
        currentBase = base;
        programWindow.setBase(base);
        dataWindow.setBase(base);
        registerPanel.setBase(base);
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

    private void openFile() {
        if (fileChooser.showDialog(this, null) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        final File file = fileChooser.getSelectedFile();
        try {
            final FileInputStream inputStream = new FileInputStream(file);
            final int size = (int) file.length();
            final byte[] buffer = new byte[size];
            final int bytesRead = inputStream.read(buffer, 0, size);
            inputStream.close();
            if (bytesRead != size) {
                throw new IOException("Não foi possível ler todos os bytes do arquivo");
            }
            cpu.setMemory(buffer);
            updateInterface();
            dataTable.scrollToRow(Cpu.DATA_START_ADDRESS, true);
        }
        catch (final IOException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Um erro ocorreu ao abrir o arquivo",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
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

    private void updateProgramCounterRow() {
        final int programCounter = cpu.getProgramCounter();
        programTableModel.setProgramCounterRow(programCounter);
        programTable.setRowSelectionInterval(programCounter, programCounter);
        if (!isRunning()) {
            programTable.scrollToRow(programCounter);
        }
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
}
