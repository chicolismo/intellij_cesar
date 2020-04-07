package cesar.gui;

import java.awt.Toolkit;
import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

public class MenuBar extends JMenuBar {
    private static final long serialVersionUID = -7618206299229330211L;

    public final JMenuItem fileOpen;
    public final JMenuItem fileExit;
    public final JMenuItem viewProgram;
    public final JMenuItem viewData;

    public MenuBar() {
        int ctrlKey = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();

        fileOpen = new JMenuItem("Carregar", KeyEvent.VK_C);
        fileOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ctrlKey));

        fileExit = new JMenuItem("Sair", KeyEvent.VK_S);
        fileExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ctrlKey));

        JMenu fileMenu = new JMenu("Arquivo");
        fileMenu.add(fileOpen);
        fileMenu.addSeparator();
        fileMenu.add(fileExit);

        JMenu editMenu = new JMenu("Editar");

        JMenu viewMenu = new JMenu("Visualizar");
        viewProgram = new JMenuItem("Exibir programa");
        viewData = new JMenuItem("Exibir dados");
        viewMenu.add(viewProgram);
        viewMenu.add(viewData);

        JMenu execMenu = new JMenu("Executar");

        JMenu helpMenu = new JMenu("?");

        add(fileMenu);
        add(editMenu);
        add(viewMenu);
        add(execMenu);
        add(helpMenu);
    }
}
