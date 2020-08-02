package cesar.views.panels;

import cesar.utils.Defaults;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

public class MenuBar extends JMenuBar {
    private static final long serialVersionUID = -7618206299229330211L;

    private final static int CTRL_KEY = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();

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

        // =============================================================================================================
        // Arquivo
        // =============================================================================================================
        fileMenu = new JMenu("Arquivo");
        fileMenu.setToolTipText("Operações com arquivo");

        fileLoad = new JMenuItem("Carregar...");
        fileLoad.setToolTipText("Carrega um arquivo da memória");
        fileLoad.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, CTRL_KEY));

        fileLoadPartially = new JMenuItem("Carga parcial...");
        fileLoadPartially.setToolTipText("Carrega parcialmente um arquivo");
        fileLoadPartially.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, CTRL_KEY));

        fileSave = new JMenuItem("Salvar...");
        fileSave.setToolTipText("Salva a memória em um arquivo .MEM");
        fileSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, CTRL_KEY));

        fileSaveText = new JMenuItem("Salvar texto...");
        fileSaveText.setToolTipText("Salva a memória em um arquivo texto");
        fileSaveText.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, CTRL_KEY));

        fileExit = new JMenuItem("Sair");

        fileMenu.add(fileLoad);
        fileMenu.add(fileLoadPartially);
        fileMenu.add(fileSave);
        fileMenu.add(fileSaveText);
        fileMenu.addSeparator();
        fileMenu.add(fileExit);

        // =============================================================================================================
        // Editar
        // =============================================================================================================
        editMenu = new JMenu("Editar");
        editMenu.setToolTipText("Modo de edição, Ir Para..., Zerar memória, etc");

        editGoto = new JMenuItem("Ir Para...");
        editGoto.setToolTipText("Vai para uma posição na memória");
        editGoto.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, CTRL_KEY));

        editZeroMemory = new JMenuItem("Zerar memória");
        editZeroMemory.setToolTipText("Zera uma região da memória");
        editZeroMemory.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, CTRL_KEY));

        editCopyMemory = new JMenuItem("Copiar memória...");
        editCopyMemory.setToolTipText("Copia uma região da memória");

        editDecimal = new JRadioButtonMenuItem("Decimal");
        editDecimal.setToolTipText("Trocar para o modo de edição Decimal");
        editDecimal.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, CTRL_KEY));

        editHexadecimal = new JRadioButtonMenuItem("Hexadecimal");
        editHexadecimal.setToolTipText("Trocar para o modo de edição Hexadecimal");
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

        // =============================================================================================================
        // Visualizar
        // =============================================================================================================
        viewMenu = new JMenu("Visualizar");
        viewMenu.setToolTipText("Exibe / oculta janelas");

        viewProgram = new JCheckBoxMenuItem("Memória - Programa");
        viewProgram.setToolTipText("Exibe / oculta janela de memória do programa");
        viewProgram.setAccelerator(KeyStroke.getKeyStroke(isApple ? KeyEvent.VK_F9 : KeyEvent.VK_F11, 0));

        viewData = new JCheckBoxMenuItem("Memória - Dados");
        viewData.setToolTipText("Exibe / oculta janela de memória dos dados");
        viewData.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0));

        viewDisplay = new JCheckBoxMenuItem("Saída");

        viewMenu.add(viewProgram);
        viewMenu.add(viewData);
        viewMenu.add(viewDisplay);

        // =============================================================================================================
        // Executar
        // =============================================================================================================
        execMenu = new JMenu("Executar");
        execMenu.setToolTipText("Opções de execução");

        execRun = new JMenuItem("Rodar");
        execRun.setToolTipText("Roda o programa");
        execRun.setAccelerator(KeyStroke.getKeyStroke(isApple ? KeyEvent.VK_F8 : KeyEvent.VK_F9, 0));

        execNext = new JMenuItem("Passo");
        execNext.setToolTipText("Executa o programa passo a passo");
        execNext.setAccelerator(KeyStroke.getKeyStroke(isApple ? KeyEvent.VK_F7 : KeyEvent.VK_F8, 0));

        execChangeProgramCounter = new JMenuItem("Alterar PC...");
        execChangeProgramCounter.setToolTipText("Altera o conteúdo do PC");
        execChangeProgramCounter.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, CTRL_KEY));

        execZeroProgramCounter = new JMenuItem("Reset: Zerar PC e IE");
        execZeroProgramCounter.setToolTipText("Zerar PC");
        execZeroProgramCounter.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0));

        execUpdateRegisters = new JCheckBoxMenuItem("Atualizar Registradores");
        execUpdateRegisters.setToolTipText("Exibe o valor dos registradores enquanto roda o programa");

        execEnableInterruptionCounter = new JCheckBoxMenuItem("Habilitar contadores de interrupção");
        execEnableInterruptionCounter.setToolTipText("Conta pedidos de interrupção enquanto roda o programa");

        execZeroInterruptionCounter = new JMenuItem("Zerar contadores de interrupção");
        execZeroInterruptionCounter.setToolTipText("Zerar contadores de pedidos de interrupção");

        execEnableCompatibilityMode = new JCheckBoxMenuItem("Habilitar modo de compatibilidade (Cesar16)");
        execEnableCompatibilityMode.setToolTipText("Desabilita o sistema de interrupção");

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

        // =============================================================================================================
        // Ajuda
        // =============================================================================================================
        helpMenu = new JMenu("?");

        helpAbout = new JMenuItem("Sobre...");

        helpMenu.add(helpAbout);

        // =============================================================================================================

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
