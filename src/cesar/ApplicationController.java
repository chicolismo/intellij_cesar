package cesar;

import static cesar.Properties.getProperty;
import static cesar.utils.Integers.clamp;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;

import cesar.gui.dialogs.GotoDialog;
import cesar.gui.dialogs.RegisterValueDialog;
import cesar.gui.dialogs.RegisterValueDialog.RegisterValueDialogException;
import cesar.gui.dialogs.SaveTextDialog;
import cesar.gui.dialogs.ZeroMemoryDialog;
import cesar.gui.displays.RegisterDisplay;
import cesar.gui.panels.ButtonPanel;
import cesar.gui.panels.ConditionPanel;
import cesar.gui.panels.ExecutionPanel;
import cesar.gui.panels.InstructionPanel;
import cesar.gui.panels.MenuBar;
import cesar.gui.panels.RegisterPanel;
import cesar.gui.panels.StatusBar;
import cesar.gui.tables.DataTable;
import cesar.gui.tables.DataTableModel;
import cesar.gui.tables.ProgramTable;
import cesar.gui.tables.ProgramTableModel;
import cesar.gui.tables.Table;
import cesar.gui.tables.TableModel;
import cesar.gui.utils.Components;
import cesar.gui.utils.FileLoader;
import cesar.gui.utils.FileLoader.FileLoaderException;
import cesar.gui.utils.FileSaver;
import cesar.gui.windows.DataWindow;
import cesar.gui.windows.MainWindow;
import cesar.gui.windows.ProgramWindow;
import cesar.gui.windows.SideWindow;
import cesar.gui.windows.TextWindow;
import cesar.hardware.Cpu;
import cesar.hardware.Cpu.ExecutionResult;
import cesar.utils.Base;
import cesar.utils.Bytes;
import cesar.utils.Shorts;


public final class ApplicationController {
    private static final String INVALID_MEMORY_POSITION_ERROR_FORMAT = getProperty("Memory.invalidPositionErrorFormat");

    private final MainWindow window;
    private final Cpu cpu;
    private final FileLoader fileLoader;

    private final FileSaver fileSaver;
    private final SaveTextDialog saveTextDialog;
    private final GotoDialog gotoDialog;
    private final ZeroMemoryDialog zeroMemoryDialog;
    private final RegisterValueDialog registerValueDialog;
    private final RegisterPanel registerPanel;
    private final ConditionPanel conditionPanel;

    private final ExecutionPanel executionPanel;
    private final InstructionPanel instructionPanel;
    private final JToggleButton decimalButton;
    private final JToggleButton hexadecimalButton;
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
        saveTextDialog = new SaveTextDialog(window, cpu);
        gotoDialog = new GotoDialog(window);
        zeroMemoryDialog = new ZeroMemoryDialog(window, Cpu.FIRST_ADDRESS, Cpu.LAST_CHAR_ADDRESS);
        registerValueDialog = new RegisterValueDialog();

        programWindow = window.getProgramWindow();
        programTable = programWindow.getTable();
        programTableModel = (ProgramTableModel) programTable.getModel();

        dataWindow = window.getDataWindow();
        dataTable = dataWindow.getTable();
        dataTableModel = (DataTableModel) dataTable.getModel();

        textWindow = window.getTextWindow();

        registerPanel = window.getRegisterPanel();
        conditionPanel = window.getConditionPanel();
        executionPanel = window.getExecutionPanel();
        instructionPanel = window.getInstructionPanel();

        final ButtonPanel buttonPanel = window.getButtonPanel();
        runButton = buttonPanel.getRunButton();
        nextButton = buttonPanel.getNextButton();
        decimalButton = buttonPanel.getDecimalButton();
        hexadecimalButton = buttonPanel.getHexadecimalButton();

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
        decimalButton.doClick();
        updateInterface();

        window.setLocationRelativeTo(null);
        Components.centerComponent(window);
        window.setVisible(true);
        programWindow.setVisible(true);
        dataWindow.setVisible(true);
        textWindow.setVisible(true);
        window.requestFocus();
    }

    private void executeNextInstruction() {
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
        final int radix = currentBase.toInt();
        final String currentValue = Integer.toString(cpu.getProgramCounter(), radix);

        final String input = gotoDialog.showDialog(currentValue);

        try {
            final int address = Integer.parseInt(input, radix);
            if (Cpu.isValidAddress(address)) {
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

    private synchronized boolean isRunning() {
        return running;
    }

    private synchronized boolean isUpdateRegistersEnabled() {
        return menuBar.execUpdateRegisters.getState();
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

    private void saveAsText() {
        saveTextDialog.showDialog();
    }

    private void saveFile() {
        fileSaver.saveFile(cpu.getMemory());
    }

    private void setBase(final Base newBase) {
        if (currentBase != newBase) {
            currentBase = newBase;
            saveTextDialog.setBase(newBase);
            zeroMemoryDialog.setBase(newBase);
            programWindow.setBase(newBase);
            dataWindow.setBase(newBase);
            registerPanel.setBase(newBase);
        }
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

        decimalButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent event) {
                setBase(Base.DECIMAL);
                menuBar.editDecimal.setSelected(true);
            }
        });

        hexadecimalButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent event) {
                setBase(Base.HEXADECIMAL);
                menuBar.editHexadecimal.setSelected(true);
            }
        });
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

    private void setMenuEvents() {
        final Component[][] pairs = new Component[][] { { programWindow, menuBar.viewProgram },
            { dataWindow, menuBar.viewData }, { textWindow, menuBar.viewDisplay } };

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
                // copyMemory();
            }
        });

        menuBar.editDecimal.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent event) {
                decimalButton.doClick();
            }
        });

        menuBar.editHexadecimal.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent event) {
                hexadecimalButton.doClick();
            }
        });

        menuBar.execRun.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                runButton.doClick();
            }
        });

        menuBar.execNext.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                nextButton.doClick();
            }
        });

        menuBar.execUpdateRegisters.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent event) {
                if (menuBar.execUpdateRegisters.getState()) {
                    updateAfterInstruction();
                }
            }
        });

        menuBar.execChangeProgramCounter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent event) {
                showNewRegisterValueDialog(registerPanel.getDisplay(Cpu.PC));
            }
        });

        menuBar.execZeroProgramCounter.addActionListener(new ActionListener() {
            // TODO: Também tem que zerar o IE.
            @Override
            public void actionPerformed(final ActionEvent event) {
                cpu.setRegisterValue(Cpu.PC, Cpu.ZERO_BYTE);
                updateInterface();
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

    private void showNewRegisterValueDialog(final RegisterDisplay registerDisplay) {
        try {
            final int registerNumber = registerDisplay.getNumber();
            final short currentValue = cpu.getRegisterValue(registerNumber);
            final short newValue = registerValueDialog.showDialog(registerDisplay, currentValue);
            if (newValue != currentValue) {
                cpu.setRegisterValue(registerNumber, newValue);
                updateInterface();
            }
        }
        catch (final RegisterValueDialogException e) {
            statusBar.setTempMessage(e.getMessage());
        }
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

    private synchronized void stopRunning() {
        running = false;
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

    private void updateProgramCounterRow() {
        final int programCounter = cpu.getProgramCounter();
        programTableModel.setProgramCounterRow(programCounter);
        programTable.setRowSelectionInterval(programCounter, programCounter);
        if (!isRunning()) {
            programTable.scrollToRow(programCounter);
        }
    }

    private void zeroMemory() {
        final int radix = currentBase.toInt();
        String input = null;
        try {
            input = zeroMemoryDialog.showStartAddressDialog();
            if (input == null) {
                return;
            }

            final int startAddress = Integer.parseInt(input, radix);
            if (!Cpu.isValidAddress(startAddress)) {
                statusBar.setTempMessage(String.format(INVALID_MEMORY_POSITION_ERROR_FORMAT, input));
                return;
            }

            input = zeroMemoryDialog.showEndAddressDialog();
            final int endAddress = Integer.parseInt(input, radix);
            if (!Cpu.isValidAddress(endAddress) || endAddress < startAddress) {
                statusBar.setTempMessage(String.format(INVALID_MEMORY_POSITION_ERROR_FORMAT, input));
                return;
            }

            cpu.zeroMemory(startAddress, endAddress);
            updateInterface();
        }
        catch (final NumberFormatException event) {
            statusBar.setTempMessage(String.format(INVALID_MEMORY_POSITION_ERROR_FORMAT, input));
        }
    }
}
