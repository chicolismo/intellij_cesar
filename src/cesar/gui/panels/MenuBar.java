package cesar.gui.panels;

import cesar.Properties;
import cesar.utils.Defaults;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MenuBar extends JMenuBar {
    private static final long serialVersionUID = -7618206299229330211L;

    private final static int CTRL_KEY;

    static {
        int ctrlKey;
        try {
            //noinspection JavaReflectionMemberAccess
            final Method m = Toolkit.class.getMethod("getMenuShortcutKeyMaskEx");

            ctrlKey = (int) m.invoke(Toolkit.getDefaultToolkit());
        }
        catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            ctrlKey = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        }
        CTRL_KEY = ctrlKey;
    }

    public final JMenu fileMenu;
    public final JMenu editMenu;
    public final JMenu viewMenu;
    public final JMenu execMenu;
    public final JMenu helpMenu;

    public final JMenuItem fileLoad;
    public final JMenuItem fileLoadPartially;
    public final JMenuItem fileSave;
    public final JMenuItem fileSaveText;
    public final JMenuItem fileExit;

    public final JMenuItem editGoto;
    public final JMenuItem editZeroMemory;
    public final JMenuItem editCopyMemory;
    public final JRadioButtonMenuItem editDecimal;
    public final JRadioButtonMenuItem editHexadecimal;

    public final JCheckBoxMenuItem viewProgram;
    public final JCheckBoxMenuItem viewData;
    public final JCheckBoxMenuItem viewDisplay;

    public final JMenuItem execRun;
    public final JMenuItem execNext;
    public final JMenuItem execChangeProgramCounter;
    public final JMenuItem execZeroProgramCounter;

    public final JCheckBoxMenuItem execUpdateRegisters;
    public final JCheckBoxMenuItem execEnableInterruptionCounter;
    public final JMenuItem execZeroInterruptionCounter;
    public final JCheckBoxMenuItem execEnableCompatibilityMode;

    public final JMenuItem helpAbout;

    public MenuBar() {
        final boolean isApple = Defaults.IS_APPLE;

        // ==============================================================================================================
        // Arquivo
        // ==============================================================================================================
        fileMenu = new JMenu(getString("FileMenu.label"));
        fileMenu.setToolTipText(getString("FileMenu.tooltip"));

        fileLoad = new JMenuItem(getString("FileMenu.loadLabel"), KeyEvent.VK_C);
        fileLoad.setToolTipText(getString("FileMenu.loadTooltip"));
        fileLoad.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, CTRL_KEY));

        fileLoadPartially = new JMenuItem(getString("FileMenu.loadPartiallyLabel"), KeyEvent.VK_P);
        fileLoadPartially.setToolTipText(getString("FileMenu.loadPartiallyTooltip"));
        fileLoadPartially.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, CTRL_KEY));

        fileSave = new JMenuItem(getString("FileMenu.saveLabel"), KeyEvent.VK_S);
        fileSave.setToolTipText(getString("FileMenu.saveTooltip"));
        fileSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, CTRL_KEY));

        fileSaveText = new JMenuItem(getString("FileMenu.saveTextLabel"), KeyEvent.VK_T);
        fileSaveText.setToolTipText(getString("FileMenu.saveTextTooltip"));
        fileSaveText.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, CTRL_KEY));

        fileExit = new JMenuItem(getString("FileMenu.exitLabel"), KeyEvent.VK_R);

        fileMenu.add(fileLoad);
        fileMenu.add(fileLoadPartially);
        fileMenu.add(fileSave);
        fileMenu.add(fileSaveText);
        fileMenu.addSeparator();
        fileMenu.add(fileExit);

        // ==============================================================================================================
        // Editar
        // ==============================================================================================================
        editMenu = new JMenu(getString("EditMenu.label"));
        editMenu.setToolTipText(getString("EditMenu.tooltip"));

        editGoto = new JMenuItem(getString("EditMenu.gotoLabel"));
        editGoto.setToolTipText(getString("EditMenu.gotoTooltip"));
        editGoto.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, CTRL_KEY));

        editZeroMemory = new JMenuItem(getString("EditMenu.zeroMemoryLabel"));
        editZeroMemory.setToolTipText(getString("EditMenu.zeroMemoryTooltip"));
        editZeroMemory.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, CTRL_KEY));

        editCopyMemory = new JMenuItem(getString("EditMenu.copyMemoryLabel"));
        editCopyMemory.setToolTipText(getString("EditMenu.copyMemoryTooltip"));

        editDecimal = new JRadioButtonMenuItem(getString("EditMenu.decimalLabel"));
        editDecimal.setToolTipText(getString("EditMenu.decimalTooltip"));
        editDecimal.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, CTRL_KEY));

        editHexadecimal = new JRadioButtonMenuItem(getString("EditMenu.hexadecimalLabel"));
        editHexadecimal.setToolTipText(getString("EditMenu.hexadecimalTooltip"));
        editHexadecimal.setAccelerator(KeyStroke.getKeyStroke(isApple ? KeyEvent.VK_X : KeyEvent.VK_H, CTRL_KEY));

        final ButtonGroup group = new ButtonGroup();
        group.add(editDecimal);
        group.add(editHexadecimal);

        editMenu.add(editGoto);
        editMenu.addSeparator();
        editMenu.add(editZeroMemory);
        editMenu.add(editCopyMemory);
        editMenu.addSeparator();
        editMenu.add(editDecimal);
        editMenu.add(editHexadecimal);

        // ==============================================================================================================
        // Visualizar
        // ==============================================================================================================
        viewMenu = new JMenu(getString("ViewMenu.label"));
        viewMenu.setToolTipText(getString("ViewMenu.tooltip"));

        viewProgram = new JCheckBoxMenuItem(getString("ViewMenu.programLabel"));
        viewProgram.setAccelerator(KeyStroke.getKeyStroke(isApple ? KeyEvent.VK_F9 : KeyEvent.VK_F11, 0));
        viewProgram.setToolTipText(getString("ViewMenu.programTooltip"));

        viewData = new JCheckBoxMenuItem(getString("ViewMenu.dataLabel"));
        viewData.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0));
        viewData.setToolTipText(getString("ViewMenu.dataTooltip"));

        viewDisplay = new JCheckBoxMenuItem(getString("ViewMenu.displayLabel"));

        viewMenu.add(viewProgram);
        viewMenu.add(viewData);
        viewMenu.add(viewDisplay);

        // ==============================================================================================================
        // Executar
        // ==============================================================================================================
        execMenu = new JMenu(getString("ExecMenu.label"));
        execMenu.setToolTipText(getString("ExecMenu.tooltip"));

        execRun = new JMenuItem(getString("ExecMenu.runLabel"));
        execRun.setToolTipText(getString("ExecMenu.runTooltip"));
        execRun.setAccelerator(KeyStroke.getKeyStroke(isApple ? KeyEvent.VK_F8 : KeyEvent.VK_F9, 0));

        execNext = new JMenuItem(getString("ExecMenu.nextLabel"));
        execNext.setToolTipText(getString("ExecMenu.nextTooltip"));
        execNext.setAccelerator(KeyStroke.getKeyStroke(isApple ? KeyEvent.VK_F7 : KeyEvent.VK_F8, 0));

        execChangeProgramCounter = new JMenuItem(getString("ExecMenu.changeProgramCounterLabel"));
        execChangeProgramCounter.setToolTipText(getString("ExecMenu.changeProgramCounterTooltip"));
        execChangeProgramCounter.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, CTRL_KEY));

        execZeroProgramCounter = new JMenuItem(getString("ExecMenu.zeroProgramCounterLabel"));
        execZeroProgramCounter.setToolTipText(getString("ExecMenu.zeroProgramCounterTooltip"));
        execZeroProgramCounter.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0));

        execUpdateRegisters = new JCheckBoxMenuItem(getString("ExecMenu.updateRegistersLabel"));
        execUpdateRegisters.setToolTipText(getString("ExecMenu.updateRegistersTooltip"));

        execEnableInterruptionCounter = new JCheckBoxMenuItem(getString("ExecMenu.enableInterruptionCounterLabel"));
        execEnableInterruptionCounter.setToolTipText(getString("ExecMenu.enableInterruptionCounterTooltip"));

        execZeroInterruptionCounter = new JMenuItem(getString("ExecMenu.zeroInterruptionCounterLabel"));

        execEnableCompatibilityMode = new JCheckBoxMenuItem(getString("ExecMenu.enableCompatibilityModeLabel"));
        execEnableCompatibilityMode.setToolTipText(getString("ExecMenu.enableCompatibilityModeTooltip"));

        execMenu.add(execRun);
        execMenu.add(execNext);
        execMenu.addSeparator();
        execMenu.add(execChangeProgramCounter);
        execMenu.add(execZeroProgramCounter);
        execMenu.addSeparator();
        execMenu.add(execUpdateRegisters);
        execMenu.add(execEnableInterruptionCounter);
        execMenu.add(execZeroInterruptionCounter);
        execMenu.addSeparator();
        execMenu.add(execEnableCompatibilityMode);

        // ==============================================================================================================
        // Ajuda
        // ==============================================================================================================
        helpMenu = new JMenu(getString("HelpMenu.label"));

        helpAbout = new JMenuItem(getString("HelpMenu.aboutLabel"), KeyEvent.VK_S);

        helpMenu.add(helpAbout);

        // ==============================================================================================================

        fileMenu.setMnemonic(KeyEvent.VK_A);
        editMenu.setMnemonic(KeyEvent.VK_E);
        viewMenu.setMnemonic(KeyEvent.VK_V);
        execMenu.setMnemonic(KeyEvent.VK_X);
        helpMenu.setMnemonic('?');

        add(fileMenu);
        add(editMenu);
        add(viewMenu);
        add(execMenu);
        add(helpMenu);
    }

    private static String getString(final String key) {
        return Properties.getProperty(key);
    }
}
