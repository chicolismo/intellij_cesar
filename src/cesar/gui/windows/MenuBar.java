package cesar.gui.windows;

import static cesar.ApplicationProperties.getProperty;

import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;

import cesar.utils.Defaults;

public class MenuBar extends JMenuBar {
    private static final long serialVersionUID = -7618206299229330211L;

    private final static boolean IS_APPLE;
    private final static int CTRL_KEY;

    static {
        int ctrlKey;
        try {
            final Method m = Toolkit.class.getMethod("getMenuShortcutKeyMaskEx");
            ctrlKey = (int) m.invoke(Toolkit.getDefaultToolkit());
        }
        catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            ctrlKey = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        }
        CTRL_KEY = ctrlKey;
        IS_APPLE = Defaults.isApple();
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

    public final JMenuItem helpAbout;

    public MenuBar() {
        // ==============================================================================================================
        // Arquivo
        // ==============================================================================================================
        fileMenu = new JMenu(getProperty("FileMenu.label"));
        fileMenu.setToolTipText(getProperty("FileMenu.tooltip"));

        fileLoad = new JMenuItem(getProperty("FileMenu.Load.label"), KeyEvent.VK_C);
        fileLoad.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, CTRL_KEY));
        fileLoad.setToolTipText(getProperty("FileMenu.Load.tooltip"));

        fileLoadPartially = new JMenuItem(getProperty("FileMenu.LoadPartially.label"), KeyEvent.VK_P);
        fileLoadPartially.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, CTRL_KEY));
        fileLoadPartially.setToolTipText(getProperty("FileMenu.LoadPartially.tooltip"));

        fileSave = new JMenuItem(getProperty("FileMenu.Save.label"), KeyEvent.VK_S);
        fileSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, CTRL_KEY));
        fileSave.setToolTipText(getProperty("FileMenu.Save.tooltip"));

        fileSaveText = new JMenuItem(getProperty("FileMenu.SaveText.label"), KeyEvent.VK_T);
        fileSaveText.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, CTRL_KEY));
        fileSaveText.setToolTipText(getProperty("FileMenu.SaveText.tooltip"));

        fileExit = new JMenuItem(getProperty("FileMenu.Exit.label"), KeyEvent.VK_R);

        fileMenu.add(fileLoad);
        fileMenu.add(fileLoadPartially);
        fileMenu.add(fileSave);
        fileMenu.add(fileSaveText);
        fileMenu.addSeparator();
        fileMenu.add(fileExit);

        // ==============================================================================================================
        // Editar
        // ==============================================================================================================
        editMenu = new JMenu(getProperty("EditMenu.label"));
        editMenu.setToolTipText(getProperty("EditMenu.tooltip"));

        editGoto = new JMenuItem(getProperty("EditMenu.Goto.label"));
        editGoto.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, CTRL_KEY));
        editGoto.setToolTipText(getProperty("EditMenu.Goto.tooltip"));

        editZeroMemory = new JMenuItem(getProperty("EditMenu.ZeroMemory.label"));
        editZeroMemory.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, CTRL_KEY));
        editZeroMemory.setToolTipText(getProperty("EditMenu.ZeroMemory.tooltip"));

        editCopyMemory = new JMenuItem(getProperty("EditMenu.CopyMemory.label"));
        editCopyMemory.setToolTipText(getProperty("EditMenu.CopyMemory.tooltip"));

        editDecimal = new JRadioButtonMenuItem(getProperty("EditMenu.Decimal.label"));
        editDecimal.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, CTRL_KEY));
        editHexadecimal = new JRadioButtonMenuItem(getProperty("EditMenu.Hexadecimal.label"));
        editHexadecimal.setAccelerator(KeyStroke.getKeyStroke(IS_APPLE ? KeyEvent.VK_X : KeyEvent.VK_H, CTRL_KEY));
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
        viewMenu = new JMenu(getProperty("ViewMenu.label"));
        viewMenu.setToolTipText(getProperty("ViewMenu.tooltip"));

        viewProgram = new JCheckBoxMenuItem(getProperty("ViewMenu.Program.label"));
        viewProgram.setAccelerator(KeyStroke.getKeyStroke(IS_APPLE ? KeyEvent.VK_F9 : KeyEvent.VK_F11, 0));
        viewProgram.setToolTipText(getProperty("ViewMenu.Program.tooltip"));

        viewData = new JCheckBoxMenuItem(getProperty("ViewMenu.Data.label"));
        viewData.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0));
        viewData.setToolTipText(getProperty("ViewMenu.Data.tooltip"));

        viewDisplay = new JCheckBoxMenuItem(getProperty("ViewMenu.Display.label"));

        viewMenu.add(viewProgram);
        viewMenu.add(viewData);
        viewMenu.add(viewDisplay);

        // ==============================================================================================================
        // Executar
        // ==============================================================================================================
        execMenu = new JMenu(getProperty("ExecMenu.label"));

        // ==============================================================================================================
        // Ajuda
        // ==============================================================================================================
        helpMenu = new JMenu(getProperty("HelpMenu.label"));

        helpAbout = new JMenuItem(getProperty("HelpMenu.About.label"), KeyEvent.VK_S);

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
}
