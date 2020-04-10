package cesar.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

public class MenuBar extends JMenuBar {
    private static final long serialVersionUID = -7618206299229330211L;

    public final JMenuItem fileOpen;
    public final JMenuItem fileExit;
    public final JMenuItem viewProgram;
    public final JMenuItem viewData;
    public final JMenuItem viewDisplay;

    public MenuBar() {
        // int ctrlKey = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        int ctrlKey = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

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
        viewDisplay = new JMenuItem("Exibir visor");
        viewMenu.add(viewProgram);
        viewMenu.add(viewData);
        viewMenu.add(viewDisplay);

        JMenu execMenu = new JMenu("Executar");

        JMenu helpMenu = new JMenu("?");

        add(fileMenu);
        add(editMenu);
        add(viewMenu);
        add(execMenu);
        add(helpMenu);
    }
}
