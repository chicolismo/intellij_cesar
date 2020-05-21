package cesar.controllers;

import static cesar.utils.Integers.clamp;
import static cesar.utils.Properties.getProperty;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

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
import cesar.views.dialogs.RegisterValueDialog;
import cesar.views.dialogs.RegisterValueDialog.RegisterValueDialogException;
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
import cesar.views.utils.FileLoader.FileLoaderException;
import cesar.views.utils.FileSaver;
import cesar.views.windows.DataWindow;
import cesar.views.windows.MainWindow;
import cesar.views.windows.ProgramWindow;
import cesar.views.windows.SideWindow;
import cesar.views.windows.TextWindow;

public final class ApplicationController {
    private static final String INVALID_MEMORY_POSITION_ERROR_FORMAT = getProperty("Memory.invalidPositionErrorFormat");

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

    }

    @SuppressWarnings("EmptyMethod")
    private void copyMemory() {
        if (copyMemoryDialog.showCopyDialog(currentBase)) {
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
                if (isUpdateRegistersEnabled()) {
                    updateAfterInstruction();
                }
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

    private void exitProgram() {
        boolean finished = true;
        if (cpu.hasMemoryChanged()) {
            final int choice = JOptionPane.showConfirmDialog(window,
                    "O conteúdo da memória mudou, deseja salvar o arquivo?");
            if (choice == JOptionPane.OK_OPTION) {
                finished = fileSaver.saveFile(cpu.getMemory());
            }
            else if (choice == JOptionPane.CANCEL_OPTION) {
                finished = false;
            }
        }
        if (finished) {
            window.dispose();
            System.exit(0);
        }
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

    private boolean isUpdateRegistersEnabled() {
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
            if (fileLoader.loadFilePartially(cpu)) {
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
            fileLoader.setBase(newBase);
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

    @SuppressWarnings("ObjectAllocationInLoop")
    private void setMenuEvents() {
        final Component[][] pairs = new Component[][] { { programWindow, menuBar.viewProgram },
            { dataWindow, menuBar.viewData }, { textWindow, menuBar.viewDisplay } };

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
        final MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent event) {
                if (event.getClickCount() == 2) {
                    showNewRegisterValueDialog((RegisterDisplay) event.getSource());
                }
            }
        };

        for (final RegisterDisplay display : registerPanel.getDisplays()) {
            display.addMouseListener(mouseAdapter);
        }
    }

    private synchronized void setRunning(final boolean value) {
        running = value;
    }

    @SuppressWarnings("ObjectAllocationInLoop")
    private void setSideWindowEvents() {
        programTable.addMouseListener(new TableMouseAdapter(programTable, programWindow));
        programWindow.getValueField().addActionListener(new ValueFieldActionListener(programWindow));
        programWindow.getValueField().addKeyListener(new ValueFieldKeyAdapter(programWindow));

        dataTable.addMouseListener(new TableMouseAdapter(dataTable, dataWindow));
        dataWindow.getValueField().addActionListener(new ValueFieldActionListener(dataWindow));
        dataWindow.getValueField().addKeyListener(new ValueFieldKeyAdapter(dataWindow));

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
            final short newValue = RegisterValueDialog.showDialog(registerDisplay, currentValue);
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

    public void run() {
        setBase(Defaults.DEFAULT_BASE);

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
            final int radix = currentBase.toInt();
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
            // TODO: Testar se o valor atual é inserido na tabela quando uma seta é apertada.

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
