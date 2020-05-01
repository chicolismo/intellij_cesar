package cesar.gui.dialogs;

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

import cesar.Properties;
import cesar.hardware.Cpu;
import cesar.utils.Base;
import cesar.utils.Integers;
import cesar.utils.textual.TextConverter;

public class SaveTextDialog extends JDialog {
    private static final long serialVersionUID = 476591876321607487L;

    private static final String TITLE = Properties.getProperty("SaveText.title");
    private static final String FILTER_DESCRIPTION = Properties.getProperty("SaveText.fileFilterDescription");
    private static final String FILTER_EXTENSIONS = Properties.getProperty("SaveText.fileFilterExtensions");
    private static final String OK_TEXT = Properties.getProperty("SaveText.okButtonText");
    private static final String CANCEL_TEXT = Properties.getProperty("SaveText.cancelButtonText");
    private static final String PROGRAM_REGION_TITLE = Properties.getProperty("SaveText.programRegionTitle");
    private static final String PROGRAM_REGION_START_ADDRESS_TEXT = Properties
            .getProperty("SaveText.programRegionStartAddressText");
    private static final String PROGRAM_REGION_START_ADDRESS_ERROR = Properties
            .getProperty("SaveText.programRegionStartAddressErrorMessage");
    private static final String PROGRAM_REGION_END_ADDRESS_TEXT = Properties
            .getProperty("SaveText.programRegionEndAddressText");
    private static final String PROGRAM_REGION_END_ADDRESS_ERROR = Properties
            .getProperty("SaveText.programRegionEndAddressErrorMessage");
    private static final String DATA_REGION_TITLE = Properties.getProperty("SaveText.dataRegionTitle");
    private static final String DATA_REGION_START_ADDRESS_TEXT = Properties
            .getProperty("SaveText.dataRegionStartAddressText");
    private static final String DATA_REGION_START_ADDRESS_ERROR = Properties
            .getProperty("SaveText.dataRegionStartAddressErrorMessage");
    private static final String DATA_REGION_END_ADDRESS_TEXT = Properties
            .getProperty("SaveText.dataRegionEndAddressText");
    private static final String DATA_REGION_END_ADDRESS_ERROR = Properties
            .getProperty("SaveText.dataRegionEndAddressErrorMessage");

    private static final int START_PROGRAM_ADDRESS = Cpu.FIRST_ADDRESS;
    private static final int END_PROGRAM_ADDRESS = Cpu.DATA_START_ADDRESS - 1;
    private static final int START_DATA_ADDRESS = 65_000;
    private static final int END_DATA_ADDRESS = Cpu.LAST_ADDRESS;

    private final JTextField startProgramAddressField;
    private final JTextField endProgramAddressField;
    private final JTextField startDataAddressField;
    private final JTextField endDataAddressField;

    private final JLabel statusBar;

    private final int[] addresses;
    private final JFileChooser fileChooser;
    private final JButton okButton;
    private final JButton cancelButton;
    private final Cpu cpu;
    private Base currentBase;
    private File outputFile;

    public SaveTextDialog(final JFrame parent, final Cpu cpu) {
        super(parent, TITLE, true);
        this.cpu = cpu;
        fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter(FILTER_DESCRIPTION, FILTER_EXTENSIONS));
        currentBase = Base.DECIMAL;
        okButton = new JButton(OK_TEXT);
        cancelButton = new JButton(CANCEL_TEXT);
        statusBar = new JLabel("");
        statusBar.setMinimumSize(statusBar.getPreferredSize());

        addresses = new int[4];
        setDefaultValues();

        startProgramAddressField = new JTextField(4);
        endProgramAddressField = new JTextField(4);
        startDataAddressField = new JTextField(4);
        endDataAddressField = new JTextField(4);


        setLayout(new BorderLayout(5, 8));
        add(createContentPane(), BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(parent);

        initEvents();
    }

    public void setBase(final Base newBase) {
        currentBase = newBase;
    }

    public void showDialog() {
        if (fileChooser.showSaveDialog(getParent()) == JFileChooser.APPROVE_OPTION) {
            outputFile = fileChooser.getSelectedFile();
            setDefaultValues();
            updateFields();
            setVisible(true);
        }
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

    private int[] getAddresses() {
        return addresses;
    }

    private void initEvents() {
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
                tryWriteToFile();
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

    private void setDefaultValues() {
        addresses[0] = START_PROGRAM_ADDRESS;
        addresses[1] = END_PROGRAM_ADDRESS;
        addresses[2] = START_DATA_ADDRESS;
        addresses[3] = END_DATA_ADDRESS;
    }

    private void setText(final String message) {
        statusBar.setText(message);
    }

    private void tryWriteToFile() {
        if (outputFile != null && updateValues()) {
            TextConverter.writeToFile(cpu, currentBase, outputFile, getAddresses());
            outputFile = null;
            setVisible(false);
        }
    }

    private void updateFields() {
        final int radix = currentBase.toInt();
        startProgramAddressField.setText(Integer.toString(addresses[0], radix));
        endProgramAddressField.setText(Integer.toString(addresses[1], radix));
        startDataAddressField.setText(Integer.toString(addresses[2], radix));
        endDataAddressField.setText(Integer.toString(addresses[3], radix));
    }

    private boolean updateValues() {
        final int radix = currentBase.toInt();
        try {
            addresses[0] = Integer.parseInt(startProgramAddressField.getText(), radix);
            if (!Integers.isInInterval(addresses[0], Cpu.FIRST_ADDRESS, Cpu.LAST_ADDRESS)) {
                setText(PROGRAM_REGION_START_ADDRESS_ERROR);
                return false;
            }
        }
        catch (final NumberFormatException e) {
            setText(PROGRAM_REGION_START_ADDRESS_ERROR);
            return false;
        }

        try {
            addresses[1] = Integer.parseInt(endProgramAddressField.getText(), radix);
            if (!Integers.isInInterval(addresses[1], Cpu.FIRST_ADDRESS, Cpu.LAST_ADDRESS)) {
                setText(PROGRAM_REGION_END_ADDRESS_ERROR);
                return false;
            }
        }
        catch (final NumberFormatException e) {
            setText(PROGRAM_REGION_END_ADDRESS_ERROR);
            return false;
        }

        try {
            addresses[2] = Integer.parseInt(startDataAddressField.getText(), radix);
            if (!Integers.isInInterval(addresses[2], Cpu.FIRST_ADDRESS, Cpu.LAST_ADDRESS)) {
                setText(DATA_REGION_START_ADDRESS_ERROR);
                return false;
            }
        }
        catch (final NumberFormatException e) {
            setText(DATA_REGION_START_ADDRESS_ERROR);
            return false;
        }

        try {
            addresses[3] = Integer.parseInt(endDataAddressField.getText(), radix);
            if (!Integers.isInInterval(addresses[3], Cpu.FIRST_ADDRESS, Cpu.LAST_ADDRESS)) {
                setText(DATA_REGION_END_ADDRESS_ERROR);
                return false;
            }
        }
        catch (final NumberFormatException e) {
            setText(DATA_REGION_END_ADDRESS_ERROR);
            return false;
        }

        return true;
    }
}
