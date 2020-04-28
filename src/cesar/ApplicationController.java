package cesar;

import static cesar.ApplicationProperties.getProperty;
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
import java.util.Arrays;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import cesar.gui.dialogs.SaveTextDialog;
import cesar.gui.displays.RegisterDisplay;
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
import cesar.gui.windows.DataWindow;
import cesar.gui.windows.MainWindow;
import cesar.gui.windows.MenuBar;
import cesar.gui.windows.ProgramWindow;
import cesar.gui.windows.SideWindow;
import cesar.gui.windows.TextWindow;
import cesar.hardware.Cpu;
import cesar.hardware.Cpu.ExecutionResult;
import cesar.utils.Base;
import cesar.utils.Bytes;
import cesar.utils.Components;
import cesar.utils.Shorts;


public class ApplicationController {
    private static final int MINIMUM_ADDRESS = 0;
    private static final int MAXIMUM_ADDRESS = Cpu.MEMORY_SIZE - 1;

    private static final String GOTO_DIALOG_TITLE = getProperty("Goto.title");
    private static final String GOTO_DIALOG_MESSAGE = getProperty("Goto.message");

    private static final String ZERO_MEMORY_TITLE = getProperty("ZeroMemory.title");
    private static final String ZERO_MEMORY_START_MESSAGE = getProperty("ZeroMemory.startMessage");
    private static final String ZERO_MEMORY_END_MESSAGE = getProperty("ZeroMemory.endMessage");

    private static final String NEW_REGISTER_VALUE_ERROR_FORMAT = getProperty("NewRegisterValue.errorFormat");

    private static final String MEMORY_ERROR_FORMAT = "ERRO: Posição da memória inválida (%s)";

    private final MainWindow window;
    private final Cpu cpu;
    private final FileLoader fileLoader;
    private final SaveTextDialog saveTextDialog;

    private final RegisterPanel registerPanel;
    private final ConditionPanel conditionPanel;
    private final ExecutionPanel executionPanel;
    private final InstructionPanel instructionPanel;

    private final ButtonPanel buttonPanel;
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

    private boolean running;

    public ApplicationController() {
        window = new MainWindow();
        cpu = window.getCpu();
        menuBar = (MenuBar) window.getJMenuBar();
        statusBar = window.getStatusBar();

        fileLoader = new FileLoader(window);
        saveTextDialog = new SaveTextDialog(window);

        registerPanel = window.getRegisterPanel();
        conditionPanel = window.getConditionPanel();
        buttonPanel = window.getButtonPanel();
        executionPanel = window.getExecutionPanel();
        instructionPanel = window.getInstructionPanel();

        programWindow = window.getProgramWindow();
        programTable = programWindow.getTable();
        programTableModel = (ProgramTableModel) programTable.getModel();

        dataWindow = window.getDataWindow();
        dataTable = dataWindow.getTable();
        dataTableModel = (DataTableModel) dataTable.getModel();

        textWindow = window.getTextWindow();

        running = false;

        setBase(Base.DECIMAL);
        setEventListeners();
        dataTable.scrollToRow(Cpu.DATA_START_ADDRESS, true);
        updateInterface();

    }

    public void run() {
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
                    textWindow.getDisplay().repaint();
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

    private void showNewRegisterValueDialog(final RegisterDisplay registerDisplay, final int registerNumber) {
        final int radix = currentBase.toInt();
        final String currentValue = Integer.toString(cpu.getRegister(registerNumber), radix);
        final String input = registerDisplay.showNewValueDialog(currentValue);

        if (input != null) {
            try {
                final int newValue = Integer.parseInt(input, radix);
                if (Shorts.isValidShort(newValue)) {
                    cpu.setRegister(registerNumber, Shorts.fromInt(newValue));
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
        setButtonEvents();
        setKeyListenerEvents();
        setSideWindowEvents();
        setRegisterDisplayEvents();
        setMenuEvents();
    }

    private void setButtonEvents() {
        buttonPanel.btnNext.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent event) {
                executeNextInstruction();
            }
        });

        buttonPanel.btnRun.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent event) {
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
                programTableModel.fireTableRowsUpdated(Cpu.KEYBOARD_STATE_ADDRESS, Cpu.LAST_CHAR_ADDRESS);
                dataTableModel.fireTableRowsUpdated(Cpu.KEYBOARD_STATE_ADDRESS, Cpu.LAST_CHAR_ADDRESS);
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
            final JLabel addressLabel = sideWindow.getAddressLabel();
            final JTextField valueField = sideWindow.getValueField();

            table.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(final MouseEvent event) {
                    if (table.getSelectedRow() >= 0) {
                        final int row = table.getSelectedRow();
                        final String address = tableModel.getAddressAsString(row);
                        final String value = tableModel.getValueAsString(row);
                        addressLabel.setText(String.format(SideWindow.LABEL_FORMAT, address));
                        valueField.setText(value);
                        valueField.requestFocus();
                        valueField.selectAll();

                        final int radix = Base.toInt(tableModel.getBase());
                        final int currentAddress = Integer.parseInt(address, radix);
                        final int currentValue = Integer.parseInt(value, radix);

                        sideWindow.setCurrentAddress(currentAddress);
                        sideWindow.setCurrentValue(currentValue);
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
                    cpu.setRegister(7, Shorts.fromInt(selectedRow));
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
                    showNewRegisterValueDialog(display, display.getNumber());
                }
            }
        };

        for (final RegisterDisplay display : registerPanel.getDisplays()) {
            display.addMouseListener(registerPanelMouseListener);
        }
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

        menuBar.fileSaveText.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent event) {
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

    private void saveAsText() {
        saveTextDialog.saveText(cpu, currentBase);
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
                statusBar.setTempMessage(String.format(MEMORY_ERROR_FORMAT, input));
            }
        }
        catch (final NumberFormatException event) {
            statusBar.setTempMessage(String.format(MEMORY_ERROR_FORMAT, input));
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
                statusBar.setTempMessage(String.format(MEMORY_ERROR_FORMAT, userInput));
                return;
            }

            userInput = getUserInput(window, ZERO_MEMORY_END_MESSAGE, ZERO_MEMORY_TITLE,
                    Integer.toString(MAXIMUM_ADDRESS, radix));

            final int endAddress = Integer.parseInt(userInput, radix);
            if (!isValidMemoryAddress(endAddress) || endAddress < startAddress) {
                statusBar.setTempMessage(String.format(MEMORY_ERROR_FORMAT, userInput));
                return;
            }

            final byte zero = 0;
            Arrays.fill(cpu.getMemory(), startAddress, endAddress + 1, zero);
            cpu.updateMnemonics();
            updateInterface();
        }
        catch (final NumberFormatException event) {
            statusBar.setTempMessage(String.format(MEMORY_ERROR_FORMAT, userInput));
        }
    }
}
