package cesar.gui.windows;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

public class MenuBar extends JMenuBar {
    private static final long serialVersionUID = -7618206299229330211L;

    public final JMenuItem fileOpen;
    public final JMenuItem fileExit;
    public final JCheckBoxMenuItem viewProgram;
    public final JCheckBoxMenuItem viewData;
    public final JCheckBoxMenuItem viewDisplay;

    public MenuBar() {
        // int ctrlKey = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        final int ctrlKey = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

        fileOpen = new JMenuItem("Carregar", KeyEvent.VK_C);
        fileOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ctrlKey));

        fileExit = new JMenuItem("Sair", KeyEvent.VK_S);
        fileExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ctrlKey));

        final JMenu fileMenu = new JMenu("Arquivo");
        fileMenu.add(fileOpen);
        fileMenu.addSeparator();
        fileMenu.add(fileExit);

        final JMenu editMenu = new JMenu("Editar");

        final JMenu viewMenu = new JMenu("Visualizar");
//        viewProgram = new JMenuItem("Exibir programa");
        viewProgram = new JCheckBoxMenuItem("Exibir janela de programa");
        viewData = new JCheckBoxMenuItem("Exibir janela de dados");
        viewDisplay = new JCheckBoxMenuItem("Exibir janela do visor");
        viewMenu.add(viewProgram);
        viewMenu.add(viewData);
        viewMenu.add(viewDisplay);

        final JMenu execMenu = new JMenu("Executar");

        final JMenu helpMenu = new JMenu("?");

        add(fileMenu);
        add(editMenu);
        add(viewMenu);
        add(execMenu);
        add(helpMenu);
    }
}
