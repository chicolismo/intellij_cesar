package cesar.gui.windows;

import static cesar.Properties.getProperty;

import java.awt.Dialog.ModalExclusionType;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import cesar.gui.panels.ButtonPanel;
import cesar.gui.panels.ConditionPanel;
import cesar.gui.panels.ExecutionPanel;
import cesar.gui.panels.InstructionPanel;
import cesar.gui.panels.MainPanel;
import cesar.gui.panels.MenuBar;
import cesar.gui.panels.RegisterPanel;
import cesar.gui.panels.StatusBar;
import cesar.hardware.Cpu;

public class MainWindow extends JFrame {
    public static final long serialVersionUID = -4182598865843186332L;

    private final class MainWindowComponentAdapter extends ComponentAdapter {
        @Override
        public void componentMoved(final ComponentEvent event) {
            updateSubWindowsPositions();
        }
    }

    private static final int WINDOW_GAP;
    static {
        int gap;
        try {
            gap = Integer.parseInt(getProperty("MainWindow.windowGap"), 10);
        }
        catch (NumberFormatException e) {
            gap = 6;
        }
        WINDOW_GAP = gap;
    }

    private final int windowWidth;
    private final int windowHeight;

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
        super(getProperty("MainWindow.title"));
        setIconImage(
                Toolkit.getDefaultToolkit().getImage(MainWindow.class.getResource(getProperty("MainWindow.iconPath"))));
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

        windowWidth = getWidth();
        windowHeight = getHeight();
        setResizable(false);

        programWindow.setSize(programWindow.getPreferredSize());
        dataWindow.setSize(dataWindow.getPreferredSize());
        addComponentListener(new MainWindowComponentAdapter());
        updateSubWindowsPositions();
    }

    public Cpu getCpu() {
        return cpu;
    }

    public RegisterPanel getRegisterPanel() {
        return registerPanel;
    }

    public ConditionPanel getConditionPanel() {
        return conditionPanel;
    }

    public ExecutionPanel getExecutionPanel() {
        return executionPanel;
    }

    public InstructionPanel getInstructionPanel() {
        return instructionPanel;
    }

    public ButtonPanel getButtonPanel() {
        return buttonPanel;
    }

    public ProgramWindow getProgramWindow() {
        return programWindow;
    }

    public DataWindow getDataWindow() {
        return dataWindow;
    }

    public TextWindow getTextWindow() {
        return textWindow;
    }

    public StatusBar getStatusBar() {
        return statusBar;
    }

    private void updateSubWindowsPositions() {
        final Point pos = getLocation();
        final Dimension programWindowSize = programWindow.getSize();
        final Dimension programWindowPreferredSize = programWindow.getPreferredSize();
        final Dimension dataWindowPreferredSize = dataWindow.getPreferredSize();
        programWindow.setLocation(pos.x - programWindowSize.width - WINDOW_GAP, pos.y);
        dataWindow.setLocation(pos.x + windowWidth + WINDOW_GAP, pos.y);
        programWindow.setSize(programWindowPreferredSize.width, windowHeight);
        dataWindow.setSize(dataWindowPreferredSize.width, windowHeight);
        textWindow.setLocation(pos.x - programWindowSize.width - WINDOW_GAP, pos.y + windowHeight + WINDOW_GAP);
        requestFocus();
    }
}
