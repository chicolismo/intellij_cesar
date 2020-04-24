package cesar.gui.windows;

import com.sun.javafx.scene.control.Keystroke;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MenuBar extends JMenuBar {
    private static final long serialVersionUID = -7618206299229330211L;

    public final JMenuItem fileOpen;
    public final JMenuItem fileOpenPartially;
    public final JMenuItem fileSaveText;
    public final JMenuItem fileExit;
    public final JCheckBoxMenuItem viewProgram;
    public final JCheckBoxMenuItem viewData;
    public final JCheckBoxMenuItem viewDisplay;
    public final JMenuItem helpAbout;
    private final static int CTRL_KEY;
    private final static int ALT_KEY;

    static {
        int ctrlKey;
        try {
            Method m = Toolkit.class.getMethod("getMenuShortcutKeyMask");
            ctrlKey = (int) m.invoke(Toolkit.getDefaultToolkit());
        }
        catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            ctrlKey = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        }
        ALT_KEY = KeyEvent.ALT_MASK;
        CTRL_KEY = ctrlKey;
    };

    public MenuBar() {

        fileOpen = new JMenuItem("Carregar...", KeyEvent.VK_C);
        fileOpen.setIcon(UIManager.getIcon("FileView.directoryIcon"));
        fileOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, CTRL_KEY));

        fileOpenPartially = new JMenuItem("Carga parcial...", KeyEvent.VK_P);
        fileOpenPartially.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, CTRL_KEY));

        fileSaveText = new JMenuItem("Salvar texto...", KeyEvent.VK_T);
        fileSaveText.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, CTRL_KEY));

        fileExit = new JMenuItem("Sair", KeyEvent.VK_R);
        //fileExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ctrlKey));

        final JMenu fileMenu = new JMenu("Arquivo");
        fileMenu.add(fileOpen);
        fileMenu.add(fileOpenPartially);
        fileMenu.add(fileSaveText);
        fileMenu.addSeparator();
        fileMenu.add(fileExit);

        final JMenu editMenu = new JMenu("Editar");

        final JMenu viewMenu = new JMenu("Visualizar");
        viewProgram = new JCheckBoxMenuItem("Memória - Programa");
        viewProgram.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0));

        viewData = new JCheckBoxMenuItem("Memória - Dados");
        viewData.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0));

        viewDisplay = new JCheckBoxMenuItem("Saída");

        viewMenu.add(viewProgram);
        viewMenu.add(viewData);
        viewMenu.add(viewDisplay);

        final JMenu execMenu = new JMenu("Executar");

        final JMenu helpMenu = new JMenu("?");
        helpAbout = new JMenuItem("Sobre....", KeyEvent.VK_S);
        helpMenu.add(helpAbout);


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
