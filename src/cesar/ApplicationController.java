package cesar;

import cesar.gui.dialogs.SaveTextDialog;
import cesar.gui.displays.RegisterDisplay;
import cesar.gui.panels.MenuBar;
import cesar.gui.panels.*;
import cesar.gui.tables.*;
import cesar.gui.utils.FileLoader;
import cesar.gui.utils.FileLoader.FileLoaderException;
import cesar.gui.utils.FileSaver;
import cesar.gui.windows.*;
import cesar.hardware.Cpu;
import cesar.hardware.Cpu.ExecutionResult;
import cesar.utils.Base;
import cesar.utils.Bytes;
import cesar.utils.Components;
import cesar.utils.Shorts;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;

import static cesar.Properties.getProperty;
import static cesar.utils.Integers.clamp;


public class ApplicationController {
    private static final int MINIMUM_ADDRESS = 0;
    private static final int MAXIMUM_ADDRESS = Cpu.MEMORY_SIZE - 1;

    private static final String GOTO_DIALOG_TITLE = getProperty("Goto.title");
    private static final String GOTO_DIALOG_MESSAGE = getProperty("Goto.message");

    private static final String ZERO_MEMORY_TITLE = getProperty("ZeroMemory.title");
    private static final String ZERO_MEMORY_START_MESSAGE = getProperty("ZeroMemory.startMessage");
    private static final String ZERO_MEMORY_END_MESSAGE = getProperty("ZeroMemory.endMessage");

    private static final String NEW_REGISTER_VALUE_ERROR_FORMAT = getProperty("NewRegisterValue.errorFormat");

    private static final String INVALID_MEMORY_POSITION_ERROR_FORMAT = getProperty("Memory.invalidPositionErrorFormat");

    private final MainWindow window;
    private final Cpu cpu;
    private final FileLoader fileLoader;
    private final FileSaver fileSaver;
    private final SaveTextDialog saveTextDialog;

    private final RegisterPanel registerPanel;
    private final ConditionPanel conditionPanel;
    private final ExecutionPanel executionPanel;
    private final InstructionPanel instructionPanel;

    private final ButtonPanel buttonPanel;
    private final JToggleButton runButton;
    private final JButton nextButton;
    private final MenuBar menuBar;
    private final StatusBar statusBar;

    private final ProgramWindow programWindow;
    private final ProgramTable programTable;
    private final ProgramTableModel programTableModel;

    private final DataWindow dataWindow;
    private final DataTable dataTable;
    private final DataTableModel dataTableModel;

    private final TextWindow textWindow;

    private Base currentBase;

    private int instructionCount = 0;
    private boolean running;

    public ApplicationController(final MainWindow window) {
        this.window = window;
        cpu = window.getCpu();
        menuBar = (MenuBar) window.getJMenuBar();
        statusBar = window.getStatusBar();

        fileLoader = new FileLoader(window);
        fileSaver = new FileSaver(window);
        saveTextDialog = new SaveTextDialog(window);

        registerPanel = window.getRegisterPanel();
        conditionPanel = window.getConditionPanel();
        buttonPanel = window.getButtonPanel();
        executionPanel = window.getExecutionPanel();
        instructionPanel = window.getInstructionPanel();

        runButton = buttonPanel.btnRun;
        nextButton = buttonPanel.btnNext;

        programWindow = window.getProgramWindow();
        programTable = programWindow.getTable();
        programTableModel = (ProgramTableModel) programTable.getModel();

        dataWindow = window.getDataWindow();
        dataTable = dataWindow.getTable();
        dataTableModel = (DataTableModel) dataTable.getModel();

        textWindow = window.getTextWindow();

        running = false;
    }

    public void run() {
        setBase(Base.DECIMAL);
        menuBar.viewProgram.setState(true);
        menuBar.viewData.setState(true);
        menuBar.viewDisplay.setState(true);
        menuBar.execUpdateRegisters.setState(true);
        setEventListeners();
        dataTable.scrollToRow(Cpu.DATA_START_ADDRESS, true);
        updateInterface();
        buttonPanel.btnDec.doClick();
        window.setLocationRelativeTo(null);
        Components.centerComponent(window);
        window.setVisible(true);
        programWindow.setVisible(true);
        dataWindow.setVisible(true);
        textWindow.setVisible(true);
        window.requestFocus();
    }

    private static boolean isValidMemoryAddress(final int address) {
        return address >= MINIMUM_ADDRESS && address <= MAXIMUM_ADDRESS;
    }

    private synchronized boolean isRunning() {
        return running;
    }

    private synchronized boolean isUpdateRegistersEnabled() {
        return menuBar.execUpdateRegisters.getState();
    }
    //
    //    private synchronized void setUpdateRegistersEnabled(final boolean value) {
    //        menuBar.execUpdateRegisters.setState(value);
    //    }

    private synchronized void stopRunning() {
        running = false;
    }

    private synchronized void startRunning() {
        running = true;
        final Thread runningThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (isRunning()) {
                    executeNextInstruction();
                }
                updateInterface();
            }
        });
        runningThread.start();
    }

    public void executeNextInstruction() {
        final ExecutionResult result = cpu.executeNextInstruction();
        statusBar.setText(result.toString());

        switch (result) {
            case NOOP:
            case OK:
                ++instructionCount;
                if (isUpdateRegistersEnabled()) {
                    updateAfterInstruction();
                }
                break;

            case HALT:
            case BREAK_POINT:
            case INVALID_INSTRUCTION:
                if (isRunning() && runButton.isSelected()) {
                    runButton.doClick();
                }
                break;
        }
    }

    private void updateAfterInstruction() {
        updateDisplays();
        if (cpu.hasMemoryChanged()) {
            final int start = cpu.getLastChangedAddress();
            final int end = cpu.getLastChangedMnemonic();
            programTableModel.fireTableRowsUpdated(start, end);
            dataTableModel.fireTableRowsUpdated(start, start + 1);
            textWindow.getDisplay().repaint();
        }
        updateProgramCounterRow();
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
            registerPanel.getDisplay(i).setValue(cpu.getRegisterValue(i));
        }
        conditionPanel.setNegative(cpu.isNegative());
        conditionPanel.setZero(cpu.isZero());
        conditionPanel.setOverflow(cpu.isOverflow());
        conditionPanel.setCarry(cpu.isCarry());
        executionPanel.setInstructionCount(instructionCount);
        executionPanel.setMemoryAccessCount(cpu.getMemoryAccessCount());
        instructionPanel.setRiText(cpu.getReadInstruction());
        instructionPanel.setMnemonicText(cpu.getReadMnemonic());
    }

    private void updateInterface() {
        updateDisplays();
        updateProgramCounterRow();
        window.repaint();
        textWindow.repaint();
        programTable.scrollToRow(programTableModel.getProgramCounterRow());
        programWindow.repaint();
        dataWindow.repaint();
    }

    private void setBase(final Base newBase) {
        if (currentBase != newBase) {
            currentBase = newBase;
            programWindow.setBase(newBase);
            dataWindow.setBase(newBase);
            registerPanel.setBase(newBase);
        }
    }

    private void showNewRegisterValueDialog(final RegisterDisplay registerDisplay) {
        final int radix = currentBase.toInt();
        final int registerNumber = registerDisplay.getNumber();
        final String currentValue = Integer.toString(Shorts.toUnsignedInt(cpu.getRegisterValue(registerNumber)), radix);
        final String input = registerDisplay.showNewValueDialog(currentValue);

        if (input != null) {
            try {
                final int newValue = Integer.parseInt(input, radix);
                if (Shorts.isValidShort(newValue)) {
                    cpu.setRegisterValue(registerNumber, Shorts.fromInt(newValue));
                    updateInterface();
                }
                else {
                    statusBar.setTempMessage(String.format(NEW_REGISTER_VALUE_ERROR_FORMAT, input));
                }
            }
            catch (final NumberFormatException event) {
                statusBar.setTempMessage(String.format(NEW_REGISTER_VALUE_ERROR_FORMAT, input));
            }
        }
    }

    private void setEventListeners() {
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent e) {
                exitProgram();
            }
        });

        setButtonEvents();
        setKeyListenerEvents();
        setSideWindowEvents();
        setRegisterDisplayEvents();
        setMenuEvents();
    }

    private void setButtonEvents() {
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent event) {
                executeNextInstruction();
            }
        });

        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent event) {
                if (runButton.isSelected()) {
                    if (!isRunning()) {
                        startRunning();
                    }
                }
                else {
                    stopRunning();
                }
            }
        });

        buttonPanel.btnDec.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent event) {
                setBase(Base.DECIMAL);
                menuBar.editDecimal.setSelected(true);
            }
        });

        buttonPanel.btnHex.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent event) {
                setBase(Base.HEXADECIMAL);
                menuBar.editHexadecimal.setSelected(true);
            }
        });
    }

    private void setKeyListenerEvents() {
        final KeyListener keyListener = new KeyListener() {
            @Override
            public void keyPressed(final KeyEvent event) {
                menuBar.dispatchEvent(event);
                window.requestFocus();
            }

            @Override
            public void keyReleased(final KeyEvent event) {
                menuBar.dispatchEvent(event);
                window.requestFocus();
            }

            @Override
            public void keyTyped(final KeyEvent event) {
                menuBar.dispatchEvent(event);
                cpu.setTypedKey((byte) event.getKeyChar());
                if (cpu.hasMemoryChanged()) {
                    programTableModel.fireTableRowsUpdated(Cpu.KEYBOARD_STATE_ADDRESS, Cpu.LAST_CHAR_ADDRESS);
                    dataTableModel.fireTableRowsUpdated(Cpu.KEYBOARD_STATE_ADDRESS, Cpu.LAST_CHAR_ADDRESS);
                }
                window.requestFocus();
            }
        };

        window.addKeyListener(keyListener);
        programWindow.addKeyListener(keyListener);
        dataWindow.addKeyListener(keyListener);
        textWindow.addKeyListener(keyListener);
    }

    private void setSideWindowEvents() {
        for (final SideWindow<?, ?> sideWindow : new SideWindow<?, ?>[] { programWindow, dataWindow }) {
            final Table table = sideWindow.getTable();
            final TableModel tableModel = (TableModel) table.getModel();
            final JTextField valueField = sideWindow.getValueField();

            table.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(final MouseEvent event) {
                    if (table.getSelectedRow() >= 0) {
                        sideWindow.clickOnRow(table.getSelectedRow());
                    }
                }
            });

            valueField.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent actionEvent) {
                    final int radix = Base.toInt(tableModel.getBase());
                    final String value = valueField.getText();
                    final int newValue;
                    try {
                        newValue = Integer.parseInt(value, radix);
                    }
                    catch (final NumberFormatException exception) {
                        // Se o valor digitado for inválido, ignorar o ENTER
                        return;
                    }
                    if (Bytes.isValidByte(newValue)) {
                        sideWindow.setCurrentValue(newValue);
                        int currentAddress = sideWindow.getCurrentAddress();
                        cpu.setByte(currentAddress, (byte) (0xFF & newValue));
                        programTableModel.fireTableDataChanged();
                        dataTableModel.fireTableDataChanged();
                        if (Cpu.isIOAddress(currentAddress)) {
                            textWindow.getDisplay().repaint();
                        }
                        // Seleciona a próxima linha
                        currentAddress = clamp(currentAddress + 1);
                        table.setRowSelectionInterval(currentAddress, currentAddress);
                        valueField.setText(tableModel.getValueAsString(currentAddress));
                        table.scrollToRow(currentAddress);
                        valueField.requestFocus();
                        valueField.selectAll();
                        sideWindow.setCurrentAddress(currentAddress);
                    }
                }
            });
        }

        programTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent event) {
                final int selectedRow;
                if (event.getClickCount() == 2 && (selectedRow = programTable.getSelectedRow()) != -1) {
                    cpu.setRegisterValue(7, Shorts.fromInt(selectedRow));
                    updateInterface();
                }
            }
        });
    }

    private void setRegisterDisplayEvents() {
        final MouseListener registerPanelMouseListener = new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent event) {
                if (event.getClickCount() == 2) {
                    final RegisterDisplay display = (RegisterDisplay) event.getSource();
                    showNewRegisterValueDialog(display);
                }
            }
        };

        for (final RegisterDisplay display : registerPanel.getDisplays()) {
            display.addMouseListener(registerPanelMouseListener);
        }
    }

    private void setMenuEvents() {
        final Component[][] pairs = new Component[][] {
                { programWindow, menuBar.viewProgram }, { dataWindow, menuBar.viewData },
                { textWindow, menuBar.viewDisplay }
        };

        for (final Component[] pair : pairs) {
            final JDialog sideWindow = (JDialog) pair[0];
            final JCheckBoxMenuItem checkBox = (JCheckBoxMenuItem) pair[1];

            sideWindow.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentHidden(final ComponentEvent event) {
                    super.componentHidden(event);
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
            public void actionPerformed(final ActionEvent event) {
                loadFile();
            }
        });

        menuBar.fileLoadPartially.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent event) {
                loadFilePartially();
            }
        });

        menuBar.fileSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                saveFile();
            }
        });

        menuBar.fileSaveText.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent event) {
                saveAsText();
            }
        });

        menuBar.fileExit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent actionEvent) {
                exitProgram();
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
            public void actionPerformed(final ActionEvent event) {
                zeroMemory();
            }
        });

        menuBar.editCopyMemory.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent event) {
                //                copyMemory();
            }
        });

        menuBar.editDecimal.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent event) {
                buttonPanel.btnDec.doClick();
            }
        });

        menuBar.editHexadecimal.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent event) {
                buttonPanel.btnHex.doClick();
            }
        });

        menuBar.execUpdateRegisters.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                if (menuBar.execUpdateRegisters.getState()) {
                    updateAfterInstruction();
                }
            }
        });

        menuBar.execChangeProgramCounter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                showNewRegisterValueDialog(registerPanel.getDisplay(Cpu.PC));
            }
        });

        menuBar.execZeroProgramCounter.addActionListener(new ActionListener() {
            // TODO: Também tem que zerar o IE.
            @Override
            public void actionPerformed(ActionEvent event) {
                cpu.setRegisterValue(Cpu.PC, Cpu.ZERO_BYTE);
                updateInterface();
            }
        });
    }

    private static String getUserInput(final Component parent, final Object message, final String title,
            final Object initialValue) {
        return (String) JOptionPane.showInputDialog(parent, message, title, JOptionPane.PLAIN_MESSAGE, null, null,
                initialValue);
    }

    private void loadFile() {
        try {
            if (fileLoader.loadFile(cpu)) {
                updateInterface();
                dataTable.scrollToRow(Cpu.DATA_START_ADDRESS, true);
            }
        }
        catch (final FileLoaderException event) {
            statusBar.setTempMessage(event.getMessage());
        }
    }

    private void loadFilePartially() {
        try {
            if (fileLoader.loadFilePartially(cpu, currentBase)) {
                updateInterface();
            }
        }
        catch (final FileLoaderException event) {
            statusBar.setTempMessage(event.getMessage());
        }
    }

    private void saveFile() {
        fileSaver.saveFile(cpu.getMemory());
    }

    private void saveAsText() {
        saveTextDialog.saveText(cpu, currentBase);
    }

    private void exitProgram() {
        if (cpu.hasMemoryChanged()) {
            final int choice = JOptionPane.showConfirmDialog(window,
                    "O conteúdo da memória mudou, deseja salvar o arquivo?");
            if (choice == JOptionPane.OK_OPTION) {
                if (fileSaver.saveFile(cpu.getMemory())) {
                    window.dispose();
                    System.exit(0);
                }
                else {
                    return;
                }
            }
            else if (choice == JOptionPane.CANCEL_OPTION) {
                return;
            }
        }
        window.dispose();
        System.exit(0);
    }

    private void goTo() {
        int radix = currentBase.toInt();

        final String input = (String) JOptionPane.showInputDialog(window, GOTO_DIALOG_MESSAGE, GOTO_DIALOG_TITLE,
                JOptionPane.PLAIN_MESSAGE, null, null, Integer.toString(MINIMUM_ADDRESS, radix));
        try {
            final int address = Integer.parseInt(input, radix);
            if (isValidMemoryAddress(address)) {
                programWindow.clickOnRow(address);
            }
            else {
                statusBar.setTempMessage(String.format(INVALID_MEMORY_POSITION_ERROR_FORMAT, input));
            }
        }
        catch (final NumberFormatException event) {
            statusBar.setTempMessage(String.format(INVALID_MEMORY_POSITION_ERROR_FORMAT, input));
        }
    }

    private void zeroMemory() {
        int radix = currentBase.toInt();
        String userInput = null;
        try {
            userInput = getUserInput(window, ZERO_MEMORY_START_MESSAGE, ZERO_MEMORY_TITLE,
                    Integer.toString(MINIMUM_ADDRESS, radix));

            if (userInput == null) {
                return;
            }

            final int startAddress = Integer.parseInt(userInput, radix);
            if (!isValidMemoryAddress(startAddress)) {
                statusBar.setTempMessage(String.format(INVALID_MEMORY_POSITION_ERROR_FORMAT, userInput));
                return;
            }

            userInput = getUserInput(window, ZERO_MEMORY_END_MESSAGE, ZERO_MEMORY_TITLE,
                    Integer.toString(MAXIMUM_ADDRESS, radix));

            final int endAddress = Integer.parseInt(userInput, radix);
            if (!isValidMemoryAddress(endAddress) || endAddress < startAddress) {
                statusBar.setTempMessage(String.format(INVALID_MEMORY_POSITION_ERROR_FORMAT, userInput));
                return;
            }

            final byte zero = 0;
            Arrays.fill(cpu.getMemory(), startAddress, endAddress + 1, zero);
            cpu.updateMnemonics();
            updateInterface();
        }
        catch (final NumberFormatException event) {
            statusBar.setTempMessage(String.format(INVALID_MEMORY_POSITION_ERROR_FORMAT, userInput));
        }
    }
}
