package cesar.gui.dialogs;

import static cesar.Properties.getProperty;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import cesar.hardware.Cpu;
import cesar.hardware.TextConverter;
import cesar.utils.Base;

public class SaveTextDialog extends JDialog {
    private static final long serialVersionUID = 476591876321607487L;
    private static final int MIN_VALUE = 0;
    private static final int MAX_VALUE = Cpu.MEMORY_SIZE - 1;

    private static final String TITLE = getProperty("SaveText.title");
    private static final String FILTER_DESCRIPTION = getProperty("SaveText.FileFilter.description");
    private static final String FILTER_EXTENSIONS = getProperty("SaveText.FileFilter.extensions");
    private static final String OK_TEXT = getProperty("SaveText.OkButton.text");
    private static final String CANCEL_TEXT = getProperty("SaveText.CancelButton.text");
    private static final String PROGRAM_REGION_TITLE = getProperty("SaveText.ProgramRegion.title");
    private static final String PROGRAM_REGION_START_ADDRESS_TEXT = getProperty(
            "SaveText.ProgramRegion.StartAddress.text");
    private static final String PROGRAM_REGION_START_ADDRESS_ERROR = getProperty(
            "SaveText.ProgramRegion.StartAddress.errorMessage");
    private static final String PROGRAM_REGION_END_ADDRESS_TEXT = getProperty("SaveText.ProgramRegion.EndAddress.text");
    private static final String PROGRAM_REGION_END_ADDRESS_ERROR = getProperty(
            "SaveText.ProgramRegion.EndAddress.errorMessage");
    private static final String DATA_REGION_TITLE = getProperty("SaveText.DataRegion.title");
    private static final String DATA_REGION_START_ADDRESS_TEXT = getProperty("SaveText.DataRegion.StartAddress.text");
    private static final String DATA_REGION_START_ADDRESS_ERROR = getProperty(
            "SaveText.DataRegion.StartAddress.errorMessage");
    private static final String DATA_REGION_END_ADDRESS_TEXT = getProperty("SaveText.DataRegion.EndAddress.text");
    private static final String DATA_REGION_END_ADDRESS_ERROR = getProperty(
            "SaveText.DataRegion.EndAddress.errorMessage");

    private final JTextField startProgramAddressField;
    private final JTextField endProgramAddressField;
    private final JTextField startDataAddressField;
    private final JTextField endDataAddressField;

    private final JLabel statusBar;

    private int startProgramAddress;
    private int endProgramAddress;
    private int startDataAddress;
    private int endDataAddress;
    private final JFileChooser fileChooser;
    private final JButton okButton;
    private final JButton cancelButton;
    private boolean valuesOk;
    private Cpu cpu;
    private Base base;
    private File outputFile;

    public SaveTextDialog(final JFrame parent) {
        super(parent, TITLE, true);

        fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter(FILTER_DESCRIPTION, FILTER_EXTENSIONS));

        okButton = new JButton(OK_TEXT);
        cancelButton = new JButton(CANCEL_TEXT);
        statusBar = new JLabel("");
        statusBar.setMinimumSize(statusBar.getPreferredSize());

        startProgramAddressField = new JTextField(4);
        endProgramAddressField = new JTextField(4);
        startDataAddressField = new JTextField(4);
        endDataAddressField = new JTextField(4);
        valuesOk = true;

        setDefaultValues();

        setLayout(new BorderLayout(5, 8));
        add(createContentPane(), BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(parent);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(final ComponentEvent e) {
                setDefaultValues();
                super.componentShown(e);
            }
        });

        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                convertValues();
                if (valuesOk && outputFile != null) {
                    final TextConverter converter = new TextConverter(cpu, base);
                    converter.writeToFile(outputFile, startProgramAddress, endProgramAddress, startDataAddress,
                            endDataAddress);
                    outputFile = null;
                    setVisible(false);
                }
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                outputFile = null;
                setVisible(false);
            }
        });
    }

    private void convertValues() {
        valuesOk = false;

        try {
            startProgramAddress = Integer.parseInt(startProgramAddressField.getText(), 10);
            if (startProgramAddress < MIN_VALUE || startProgramAddress > MAX_VALUE) {
                setText(PROGRAM_REGION_START_ADDRESS_ERROR);
                return;
            }
        }
        catch (final NumberFormatException e) {
            setText(PROGRAM_REGION_START_ADDRESS_ERROR);
            return;
        }

        try {
            endProgramAddress = Integer.parseInt(endProgramAddressField.getText(), 10);
            if (endProgramAddress < 0 || endProgramAddress > MAX_VALUE) {
                setText(PROGRAM_REGION_END_ADDRESS_ERROR);
                return;
            }
        }
        catch (final NumberFormatException e) {
            setText(PROGRAM_REGION_END_ADDRESS_ERROR);
            return;
        }

        try {
            startDataAddress = Integer.parseInt(startDataAddressField.getText(), 10);
            if (startDataAddress < MIN_VALUE || startDataAddress > MAX_VALUE) {
                setText(DATA_REGION_START_ADDRESS_ERROR);
                return;
            }
        }
        catch (final NumberFormatException e) {
            setText(DATA_REGION_START_ADDRESS_ERROR);
            return;
        }

        try {
            endDataAddress = Integer.parseInt(endDataAddressField.getText(), 10);
            if (endDataAddress < MIN_VALUE || endDataAddress > MAX_VALUE) {
                setText(DATA_REGION_END_ADDRESS_ERROR);
                return;
            }
        }
        catch (final NumberFormatException e) {
            setText(DATA_REGION_END_ADDRESS_ERROR);
            return;
        }

        valuesOk = true;
    }

    private JPanel createContentPane() {
        final JPanel panel = new JPanel();
        final JPanel programPanel = new JPanel();
        final JPanel dataPanel = new JPanel();
        final JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));

        final GridLayout programGrid = new GridLayout(2, 2, 5, 5);
        programPanel.setLayout(programGrid);
        programPanel.setBorder(BorderFactory.createTitledBorder(PROGRAM_REGION_TITLE));
        programPanel.add(new JLabel(PROGRAM_REGION_START_ADDRESS_TEXT));
        programPanel.add(startProgramAddressField);
        programPanel.add(new JLabel(PROGRAM_REGION_END_ADDRESS_TEXT));
        programPanel.add(endProgramAddressField);

        final GridLayout dataGrid = new GridLayout(2, 2, 5, 5);
        dataPanel.setLayout(dataGrid);
        dataPanel.setBorder(BorderFactory.createTitledBorder(DATA_REGION_TITLE));
        dataPanel.add(new JLabel(DATA_REGION_START_ADDRESS_TEXT));
        dataPanel.add(startDataAddressField);
        dataPanel.add(new JLabel(DATA_REGION_END_ADDRESS_TEXT));
        dataPanel.add(endDataAddressField);

        buttonPanel.setLayout(new GridLayout(2, 1, 5, 5));
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        panel.add(programPanel);
        panel.add(dataPanel);
        panel.add(buttonPanel);

        return panel;
    }

    public void saveText(final Cpu cpu, final Base base) {
        if (fileChooser.showSaveDialog(getParent()) == JFileChooser.APPROVE_OPTION) {
            this.cpu = cpu;
            this.base = base;
            outputFile = fileChooser.getSelectedFile();
            setVisible(true);
        }
    }

    private void setDefaultValues() {
        startProgramAddress = MIN_VALUE;
        endProgramAddress = 1_023;
        startDataAddress = 65_000;
        endDataAddress = MAX_VALUE;
        startProgramAddressField.setText(Integer.toString(startProgramAddress, 10));
        endProgramAddressField.setText(Integer.toString(endProgramAddress, 10));
        startDataAddressField.setText(Integer.toString(startDataAddress, 10));
        endDataAddressField.setText(Integer.toString(endDataAddress, 10));
    }

    private void setText(final String message) {
        statusBar.setText(message);
    }

}
