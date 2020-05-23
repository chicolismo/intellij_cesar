package cesar.controllers;

import static cesar.utils.Integers.clamp;

import java.awt.Component;
import java.awt.event.*;

import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;

import cesar.models.Base;
import cesar.models.Cpu;
import cesar.models.Cpu.ExecutionResult;
import cesar.utils.Bytes;
import cesar.utils.Defaults;
import cesar.utils.Shorts;
import cesar.views.dialogs.CopyMemoryDialog;
import cesar.views.dialogs.GotoDialog;
import cesar.views.dialogs.SaveTextDialog;
import cesar.views.dialogs.ZeroMemoryDialog;
import cesar.views.displays.RegisterDisplay;
import cesar.views.panels.ButtonPanel;
import cesar.views.panels.ConditionPanel;
import cesar.views.panels.ExecutionPanel;
import cesar.views.panels.InstructionPanel;
import cesar.views.panels.MenuBar;
import cesar.views.panels.RegisterPanel;
import cesar.views.panels.StatusBar;
import cesar.views.tables.DataTable;
import cesar.views.tables.DataTableModel;
import cesar.views.tables.ProgramTable;
import cesar.views.tables.ProgramTableModel;
import cesar.views.tables.Table;
import cesar.views.tables.TableModel;
import cesar.views.utils.Components;
import cesar.views.utils.FileLoader;
import cesar.views.utils.FileSaver;
import cesar.views.windows.DataWindow;
import cesar.views.windows.MainWindow;
import cesar.views.windows.ProgramWindow;
import cesar.views.windows.SideWindow;
import cesar.views.windows.TextWindow;

public final class ApplicationController {
    private final MainWindow window;
    private final Cpu cpu;

    private final FileLoader fileLoader;
    private final FileSaver fileSaver;

    private final SaveTextDialog saveTextDialog;
    private final GotoDialog gotoDialog;
    private final ZeroMemoryDialog zeroMemoryDialog;
    private final CopyMemoryDialog copyMemoryDialog;
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

    private Base base;
    private int instructionCount = 0;
    private boolean running;

    public ApplicationController() {
        window = new MainWindow();
        cpu = window.getCpu();
        menuBar = (MenuBar) window.getJMenuBar();
        statusBar = window.getStatusBar();
        base = Defaults.DEFAULT_BASE;

        fileLoader = new FileLoader(window);
        fileSaver = new FileSaver(window);
        saveTextDialog = new SaveTextDialog(window);
        gotoDialog = new GotoDialog(window);
        zeroMemoryDialog = new ZeroMemoryDialog(window);
        copyMemoryDialog = new CopyMemoryDialog(window);

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

        setBase(Defaults.DEFAULT_BASE);
    }

    private void copyMemory() {
        if (copyMemoryDialog.showDialog(base)) {
            cpu.copyMemory(copyMemoryDialog.getStartAddress(), copyMemoryDialog.getEndAddress(),
                    copyMemoryDialog.getDstAddress());
            updateInterface();
        }
    }

    private synchronized void executeNextInstruction() {
        final ExecutionResult result = cpu.executeNextInstruction();
        statusBar.setText(result.toString());

        switch (result) {
            case NOOP:
            case OK:
                ++instructionCount;
                updateAfterInstruction();
                break;

            case HALT:
            case BREAK_POINT:
            case END_OF_MEMORY:
                if (isRunning() && runButton.isSelected()) {
                    runButton.doClick();
                }
                break;
        }
    }

    private boolean askToSave() {
        boolean result = true;
        final int choice =
                JOptionPane.showConfirmDialog(window, "O conteúdo da memória mudou, deseja salvar o arquivo?");
        if (choice == JOptionPane.OK_OPTION) {
            result = fileSaver.saveFile(cpu.getMemory());
        }
        else if (choice == JOptionPane.CANCEL_OPTION) {
            result = false;
        }
        return result;
    }

    private void exitProgram() {
        boolean finished = true;
        if (cpu.hasOriginalMemoryChanged()) {
            finished = askToSave();
        }
        if (finished) {
            window.dispose();
            System.exit(0);
        }
    }

    private void goTo() {
        if (gotoDialog.showDialog()) {
            programWindow.clickOnRow(gotoDialog.getAddress());
        }
    }

    private synchronized boolean isRunning() {
        return running;
    }

    private void loadFile() {
        boolean readyToLoad = true;
        if (cpu.hasOriginalMemoryChanged()) {
            readyToLoad = askToSave();
        }
        if (readyToLoad && fileLoader.loadFile()) {
            updateInterface();
            dataTable.scrollToRow(Cpu.DATA_START_ADDRESS, true);
        }
    }

    private void loadFilePartially() {
        boolean readyToLoad = true;
        if (cpu.hasOriginalMemoryChanged()) {
            readyToLoad = askToSave();
        }
        if (readyToLoad && fileLoader.loadFilePartially()) {
            updateInterface();
        }
    }

    private void saveAsText() {
        saveTextDialog.showDialog();
    }

    private void saveFile() {
        fileSaver.saveFile(cpu.getMemory());
    }

    private void setBase(final Base newBase) {
        if (base != newBase) {
            base = newBase;
            saveTextDialog.setBase(newBase);
            zeroMemoryDialog.setBase(newBase);
            gotoDialog.setBase(newBase);
            programWindow.setBase(newBase);
            dataWindow.setBase(newBase);
            fileLoader.setBase(newBase);
            registerPanel.setBase(newBase);
        }
    }

    private void setEventListeners() {
        //====================================================================
        // MainWindow Events
        //====================================================================
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent e) {
                exitProgram();
            }
        });

        //====================================================================
        // Button Events
        //====================================================================
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
                    setRunning(false);
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

        //====================================================================
        // Key Events
        //====================================================================
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
                    programTableModel.fireTableRowsUpdated(Cpu.KEYBOARD_STATE_ADDRESS, Cpu.KEYBOARD_INPUT_ADDRESS);
                    dataTableModel.fireTableRowsUpdated(Cpu.KEYBOARD_STATE_ADDRESS, Cpu.KEYBOARD_INPUT_ADDRESS);
                }
                window.requestFocus();
            }
        };
        window.addKeyListener(keyListener);
        programWindow.addKeyListener(keyListener);
        dataWindow.addKeyListener(keyListener);
        textWindow.addKeyListener(keyListener);

        //====================================================================
        // Side Window Events
        //====================================================================
        programTable.addMouseListener(new TableMouseAdapter(programTable, programWindow));
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
        programWindow.getValueField().addActionListener(new ValueFieldActionListener(programWindow));
        programWindow.getValueField().addKeyListener(new ValueFieldKeyAdapter(programWindow));
        final ProgramWindow.BreakPointField breakPointField = programWindow.getBreakPointField();
        breakPointField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent event) {
                cpu.setBreakPoint(breakPointField.getBreakPoint());
            }
        });
        breakPointField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(final FocusEvent event) {
                cpu.setBreakPoint(breakPointField.getBreakPoint());
            }
        });

        dataTable.addMouseListener(new TableMouseAdapter(dataTable, dataWindow));
        dataWindow.getValueField().addActionListener(new ValueFieldActionListener(dataWindow));
        dataWindow.getValueField().addKeyListener(new ValueFieldKeyAdapter(dataWindow));

        //====================================================================
        // Register Display Events
        //====================================================================
        final MouseAdapter registerDisplayMouseAdapter = new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent event) {
                if (event.getClickCount() == 2) {
                    showRegisterDisplayDialog((RegisterDisplay) event.getSource());
                }
            }
        };

        for (final RegisterDisplay display : registerPanel.getDisplays()) {
            display.addMouseListener(registerDisplayMouseAdapter);
        }

        //====================================================================
        // Menu Events
        //====================================================================
        final Component[][] pairs = new Component[][] {
                { programWindow, menuBar.viewProgram }, { dataWindow, menuBar.viewData },
                { textWindow, menuBar.viewDisplay }
        };

        for (final Component[] pair : pairs) {
            final JDialog sideWindow = (JDialog) pair[0];
            final JCheckBoxMenuItem checkBox = (JCheckBoxMenuItem) pair[1];
            sideWindow.addComponentListener(new SideWindowComponentAdapter(checkBox));
            checkBox.addActionListener(new CheckBoxMenuItemComponentAdapter(sideWindow, checkBox));
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
                copyMemory();
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
                showRegisterDisplayDialog(registerPanel.getDisplay(Cpu.PC));
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

    private synchronized void setRunning(final boolean value) {
        running = value;
    }

    private void showRegisterDisplayDialog(final RegisterDisplay registerDisplay) {
        final short currentValue = registerDisplay.getValueAsShort();
        if (registerDisplay.showDialog()) {
            short newValue = registerDisplay.getValueAsShort();
            if (newValue != currentValue) {
                cpu.setRegisterValue(registerDisplay.getNumber(), newValue);
            }
        }
        else if (registerDisplay.hasError()) {
            statusBar.setTempMessage(registerDisplay.getErrorMessage());
        }
    }

    private synchronized void startRunning() {
        setRunning(true);
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

    private void updateAfterInstruction() {
        if (menuBar.execUpdateRegisters.getState()) {
            updateDisplays();
        }
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
        if (!isRunning()) {
            programTable.setRowSelectionInterval(programCounter, programCounter);
            programTable.scrollToRow(programCounter);
        }
    }

    private void zeroMemory() {
        if (zeroMemoryDialog.showZeroMemoryDialog()) {
            cpu.zeroMemory(zeroMemoryDialog.getStartAddress(), zeroMemoryDialog.getEndAddress());
            updateInterface();
        }
    }

    public void run() {
        menuBar.viewProgram.setState(true);
        menuBar.viewData.setState(true);
        menuBar.viewDisplay.setState(true);
        menuBar.execUpdateRegisters.setState(true);
        setEventListeners();

        decimalButton.doClick();
        updateInterface();

        window.setLocationRelativeTo(null);
        Components.centerComponent(window);
        window.setVisible(true);
        programWindow.setVisible(true);
        dataWindow.setVisible(true);
        dataTable.scrollToRow(Cpu.DATA_START_ADDRESS, true);
        textWindow.setVisible(true);
        window.requestFocus();
    }


    private static class CheckBoxMenuItemComponentAdapter implements ActionListener {
        private final JDialog sideWindow;
        private final JCheckBoxMenuItem checkBox;

        public CheckBoxMenuItemComponentAdapter(final JDialog sideWindow, final JCheckBoxMenuItem checkBox) {
            this.sideWindow = sideWindow;
            this.checkBox = checkBox;
        }

        @Override
        public void actionPerformed(final ActionEvent actionEvent) {
            sideWindow.setVisible(checkBox.getState());
        }
    }


    private static class SideWindowComponentAdapter extends ComponentAdapter {
        private final JCheckBoxMenuItem checkBox;

        public SideWindowComponentAdapter(final JCheckBoxMenuItem checkBox) {
            this.checkBox = checkBox;
        }

        @Override
        public void componentHidden(final ComponentEvent event) {
            super.componentHidden(event);
            checkBox.setState(false);
        }
    }


    private static class TableMouseAdapter extends MouseAdapter {
        private final Table table;
        private final SideWindow<?, ?> sideWindow;

        public TableMouseAdapter(final Table table, final SideWindow<?, ?> sideWindow) {
            this.table = table;
            this.sideWindow = sideWindow;
        }

        @Override
        public void mouseReleased(final MouseEvent event) {
            if (table.getSelectedRow() >= 0) {
                sideWindow.clickOnRow(table.getSelectedRow());
            }
        }
    }


    private class ValueFieldActionListener implements ActionListener {
        private final SideWindow<?, ?> sideWindow;
        private final Table table;
        private final TableModel tableModel;

        public ValueFieldActionListener(final SideWindow<?, ?> sideWindow) {
            this.sideWindow = sideWindow;
            table = sideWindow.getTable();
            tableModel = (TableModel) table.getModel();
        }

        @Override
        public void actionPerformed(final ActionEvent actionEvent) {
            final JTextField valueField = (JTextField) actionEvent.getSource();
            final int radix = base.toInt();
            final String value = valueField.getText();
            try {
                final int newValue = Integer.parseInt(value, radix);
                if (Bytes.isValidByte(newValue)) {
                    sideWindow.setCurrentValue(newValue);
                    int address = sideWindow.getCurrentAddress();
                    cpu.setByte(address, Bytes.fromInt(newValue));
                    cpu.updateMnemonics();
                    dataTableModel.fireTableDataChanged();
                    programTableModel.fireTableDataChanged();
                    if (Cpu.isIOAddress(address)) {
                        textWindow.getDisplay().repaint();
                    }
                    // Seleciona a próxima linha
                    address = clamp(address + 1);
                    table.setRowSelectionInterval(address, address);
                    valueField.setText(tableModel.getValueAsString(address));
                    table.scrollToRow(address);
                    valueField.requestFocus();
                    valueField.selectAll();
                    sideWindow.setCurrentAddress(address);
                }
            }
            catch (final NumberFormatException ignored) {
            }
        }
    }


    private static class ValueFieldKeyAdapter extends KeyAdapter {
        private final SideWindow<?, ?> sideWindow;

        public ValueFieldKeyAdapter(final SideWindow<?, ?> sideWindow) {
            this.sideWindow = sideWindow;
        }

        @Override
        public void keyPressed(final KeyEvent event) {
            // TODO: Verificar se o valor atual é inserido na tabela quando uma seta é apertada.

            final int keyCode = event.getKeyCode();
            if (keyCode == KeyEvent.VK_UP) {
                sideWindow.clickOnRow(Math.max(Cpu.FIRST_ADDRESS, sideWindow.getCurrentAddress() - 1));
            }
            else if (keyCode == KeyEvent.VK_DOWN) {
                sideWindow.clickOnRow(Math.min(Cpu.LAST_ADDRESS, sideWindow.getCurrentAddress() + 1));
            }
            else {
                super.keyReleased(event);
            }
        }
    }
}
