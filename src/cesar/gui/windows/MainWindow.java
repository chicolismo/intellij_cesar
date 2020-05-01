package cesar.gui.windows;

import cesar.gui.panels.MenuBar;
import cesar.gui.panels.*;
import cesar.hardware.Cpu;

import javax.swing.*;
import java.awt.Dialog.ModalExclusionType;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import static cesar.Properties.getProperty;

public class MainWindow extends JFrame {
    public static final long serialVersionUID = -4182598865843186332L;
    private static final int WINDOW_GAP;

    static {
        int gap;
        try {
            gap = Integer.parseInt(getProperty("MainWindow.windowGap"), 10);
        }
        catch (final NumberFormatException e) {
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


    private final class MainWindowComponentAdapter extends ComponentAdapter {
        @Override
        public void componentMoved(final ComponentEvent event) {
            updateSubWindowsPositions();
        }
    }
}
