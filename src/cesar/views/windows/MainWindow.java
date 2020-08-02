package cesar.views.windows;

import cesar.models.Cpu;
import cesar.views.panels.MenuBar;
import cesar.views.panels.*;

import javax.swing.*;
import java.awt.Dialog.ModalExclusionType;
import java.awt.*;
import java.net.URL;

public class MainWindow extends JFrame {
    public static final long serialVersionUID = -4182598865843186332L;

    private static final String TITLE = "Cesar";
    private static final int WINDOW_GAP = 3;
    private static final URL ICON = MainWindow.class.getResource("/cesar/resources/images/computer.png");


    private final Cpu cpu;
    private final ProgramWindow programWindow;
    private final DataWindow dataWindow;
    private final TextWindow textWindow;
    private final RegisterPanel registerPanel;
    private final ExecutionPanel executionPanel;
    private final ConditionPanel conditionPanel;
    private final InstructionPanel instructionPanel;
    private final ButtonPanel buttonPanel;
    private final StatusBar statusBar;

    public MainWindow() {
        super(TITLE);
        setIconImage(Toolkit.getDefaultToolkit().getImage(ICON));
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setAutoRequestFocus(true);
        setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
        setFocusable(true);

        cpu = new Cpu();
        programWindow = new ProgramWindow(this, cpu);
        dataWindow = new DataWindow(this, cpu);
        textWindow = new TextWindow(this, cpu);

        registerPanel = new RegisterPanel();
        executionPanel = new ExecutionPanel();
        conditionPanel = new ConditionPanel();
        instructionPanel = new InstructionPanel();
        buttonPanel = new ButtonPanel();

        registerPanel.setAlignmentX(CENTER_ALIGNMENT);
        registerPanel.setAlignmentY(TOP_ALIGNMENT);

        buttonPanel.setAlignmentX(CENTER_ALIGNMENT);
        buttonPanel.setAlignmentY(BOTTOM_ALIGNMENT);

        statusBar = new StatusBar();

        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        add(new MainPanel(conditionPanel, buttonPanel, executionPanel, registerPanel, instructionPanel));
        add(statusBar);
        setJMenuBar(new MenuBar());
        pack();
        setResizable(false);
        programWindow.setSize(programWindow.getPreferredSize());
        dataWindow.setSize(dataWindow.getPreferredSize());
    }

    public void updateWindows() {
        final int x = getX();
        final int y = getY();
        final int height = getHeight();
        final int width = getWidth();
        programWindow.setLocation(x - programWindow.getWidth() - WINDOW_GAP, y);
        dataWindow.setLocation(x + width + WINDOW_GAP, y);
        programWindow.setSize(programWindow.getPreferredSize().width, height);
        dataWindow.setSize(dataWindow.getPreferredSize().width, height);
        textWindow.setLocation(x - programWindow.getWidth() - WINDOW_GAP, y + height + WINDOW_GAP);
        requestFocus();
    }

    public ButtonPanel getButtonPanel() {
        return buttonPanel;
    }

    public ConditionPanel getConditionPanel() {
        return conditionPanel;
    }

    public Cpu getCpu() {
        return cpu;
    }

    public DataWindow getDataWindow() {
        return dataWindow;
    }

    public ExecutionPanel getExecutionPanel() {
        return executionPanel;
    }

    public InstructionPanel getInstructionPanel() {
        return instructionPanel;
    }

    public ProgramWindow getProgramWindow() {
        return programWindow;
    }

    public RegisterPanel getRegisterPanel() {
        return registerPanel;
    }

    public StatusBar getStatusBar() {
        return statusBar;
    }

    public TextWindow getTextWindow() {
        return textWindow;
    }
}
