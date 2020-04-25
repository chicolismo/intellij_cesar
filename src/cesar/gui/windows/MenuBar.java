package cesar.gui.windows;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MenuBar extends JMenuBar {
    private static final long serialVersionUID = -7618206299229330211L;

    public final JMenu fileMenu;
    public final JMenu editMenu;
    public final JMenu viewMenu;
    public final JMenu execMenu;
    public final JMenu helpMenu;

    public final JMenuItem fileLoad;
    public final JMenuItem fileLoadPartially;
    public final JMenuItem fileSaveText;
    public final JMenuItem fileExit;
    public final JCheckBoxMenuItem viewProgram;
    public final JCheckBoxMenuItem viewData;
    public final JCheckBoxMenuItem viewDisplay;
    public final JMenuItem helpAbout;
    private final static int CTRL_KEY;

    static {
        int ctrlKey;
        try {
            Method m = Toolkit.class.getMethod("getMenuShortcutKeyMask");
            ctrlKey = (int) m.invoke(Toolkit.getDefaultToolkit());
        }
        catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            ctrlKey = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        }
        CTRL_KEY = ctrlKey;
    }

    public MenuBar() {


        //==============================================================================================================
        // Arquivo
        //==============================================================================================================
        fileMenu = new JMenu("Arquivo");
        fileMenu.setToolTipText("Operações com arquivo");

        fileLoad = new JMenuItem("Carregar...", KeyEvent.VK_C);
        fileLoad.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, CTRL_KEY));
        fileLoad.setToolTipText("Carrega um arquivo da memória");

        fileLoadPartially = new JMenuItem("Carga parcial...", KeyEvent.VK_P);
        fileLoadPartially.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, CTRL_KEY));
        fileLoadPartially.setToolTipText("Carrega parcialmente um arquivo");

        fileSaveText = new JMenuItem("Salvar texto...", KeyEvent.VK_T);
        fileSaveText.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, CTRL_KEY));
        fileSaveText.setToolTipText("Salva a memória em um arquivo texto");

        fileExit = new JMenuItem("Sair", KeyEvent.VK_R);

        fileMenu.add(fileLoad);
        fileMenu.add(fileLoadPartially);
        fileMenu.add(fileSaveText);
        fileMenu.addSeparator();
        fileMenu.add(fileExit);

        //==============================================================================================================
        // Editar
        //==============================================================================================================
        editMenu = new JMenu("Editar");
        editMenu.setToolTipText("Modo de edição, Ir Para..., Zerar memória, etc");

        //==============================================================================================================
        // Visualizar
        //==============================================================================================================
        viewMenu = new JMenu("Visualizar");
        viewMenu.setToolTipText("Exibe / oculta janelas");

        viewProgram = new JCheckBoxMenuItem("Memória - Programa");
        viewProgram.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0));
        viewProgram.setToolTipText("Exibe / oculta janela de memória do programa");

        viewData = new JCheckBoxMenuItem("Memória - Dados");
        viewData.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0));
        viewData.setToolTipText("Exibe / oculta janela de memória dos dados");

        viewDisplay = new JCheckBoxMenuItem("Saída");

        viewMenu.add(viewProgram);
        viewMenu.add(viewData);
        viewMenu.add(viewDisplay);

        //==============================================================================================================
        // Executar
        //==============================================================================================================
        execMenu = new JMenu("Executar");

        //==============================================================================================================
        // Ajuda
        //==============================================================================================================
        helpMenu = new JMenu("?");

        helpAbout = new JMenuItem("Sobre....", KeyEvent.VK_S);

        helpMenu.add(helpAbout);


        //==============================================================================================================
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
