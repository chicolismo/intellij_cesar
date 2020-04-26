package cesar.gui.windows;

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

    private final int windowWidth;
    private final int windowHeight;

    private final Cpu cpu;
    private final MenuBar menuBar;
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
        super("Cesar");
        setIconImage(
                Toolkit.getDefaultToolkit().getImage(MainWindow.class.getResource("/cesar/gui/assets/computer.png")));
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setFocusable(true);
        setAutoRequestFocus(true);
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        cpu = new Cpu();
        programWindow = new ProgramWindow(this, cpu);
        dataWindow = new DataWindow(this, cpu);
        textWindow = new TextWindow(this, cpu);
        menuBar = new MenuBar();
        setJMenuBar(menuBar);

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
        statusBar.setText("Pronto");
        statusBar.setMinimumSize(statusBar.getPreferredSize());

        add(new MainPanel(conditionPanel, buttonPanel, executionPanel, registerPanel, instructionPanel));
        add(statusBar);
        pack();

        windowWidth = getWidth();
        windowHeight = getHeight();
        setResizable(false);

        menuBar.viewProgram.setState(true);
        menuBar.viewData.setState(true);
        menuBar.viewDisplay.setState(true);

        programWindow.setSize(programWindow.getPreferredSize());
        dataWindow.setSize(dataWindow.getPreferredSize());
        updateSubWindowsPositions();

        addComponentListener(new MainWindowComponentAdapter());
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
        final int gap = 6;
        final Point pos = getLocation();
        final Dimension programWindowSize = programWindow.getSize();
        final Dimension programWindowPreferredSize = programWindow.getPreferredSize();
        final Dimension dataWindowPreferredSize = dataWindow.getPreferredSize();
        programWindow.setLocation(pos.x - programWindowSize.width - gap, pos.y);
        dataWindow.setLocation(pos.x + windowWidth + gap, pos.y);
        programWindow.setSize(programWindowPreferredSize.width, windowHeight);
        dataWindow.setSize(dataWindowPreferredSize.width, windowHeight);
        textWindow.setLocation(pos.x - programWindowSize.width - gap, pos.y + windowHeight + gap);
        requestFocus();
    }
}
