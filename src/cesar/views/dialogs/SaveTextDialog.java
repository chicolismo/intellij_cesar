package cesar.views.dialogs;

import cesar.models.Base;
import cesar.models.Cpu;
import cesar.utils.Defaults;
import cesar.utils.Integers;
import cesar.utils.textual.TextConverter;
import cesar.views.windows.MainWindow;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;

public class SaveTextDialog extends JDialog {
    private static final long serialVersionUID = 476591876321607487L;

    private static final String TITLE = "Salvar texto";
    private static final String FILTER_DESCRIPTION = "Arquivos texto (*.txt)";
    private static final String FILTER_EXTENSIONS = "txt";
    private static final String OK_TEXT = "Ok";
    private static final String CANCEL_TEXT = "Cancelar";

    private static final String PROGRAM_REGION_TITLE = "Região do programa";

    private static final String PROGRAM_REGION_START_ADDRESS_TEXT = "Endereço inicial";
    private static final String PROGRAM_REGION_START_ADDRESS_ERROR = "ERRO: Endereço inicial do programa incorreto";

    private static final String PROGRAM_REGION_END_ADDRESS_TEXT = "Endereço final";
    private static final String PROGRAM_REGION_END_ADDRESS_ERROR = "ERRO: Endereço final do programa incorreto";

    private static final String DATA_REGION_TITLE = "Região de dados";

    private static final String DATA_REGION_START_ADDRESS_TEXT = "Endereço inicial";
    private static final String DATA_REGION_START_ADDRESS_ERROR = "ERRO: Endereço inicial dos dados incorreto";

    private static final String DATA_REGION_END_ADDRESS_TEXT = "Endereço final";
    private static final String DATA_REGION_END_ADDRESS_ERROR = "ERRO: Endereço final dos dados incorreto";

    private static final int START_PROGRAM_ADDRESS = Cpu.FIRST_ADDRESS;
    private static final int END_PROGRAM_ADDRESS = Cpu.DATA_START_ADDRESS - 1;
    private static final int START_DATA_ADDRESS = 65_000;
    private static final int END_DATA_ADDRESS = Cpu.LAST_ADDRESS;

    private final MainWindow parent;
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

    public SaveTextDialog(final MainWindow parent) {
        super(parent, TITLE, true);
        this.parent = parent;
        cpu = parent.getCpu();
        fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter(FILTER_DESCRIPTION, FILTER_EXTENSIONS));
        currentBase = Defaults.DEFAULT_BASE;
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

    private void setDefaultValues() {
        addresses[0] = START_PROGRAM_ADDRESS;
        addresses[1] = END_PROGRAM_ADDRESS;
        addresses[2] = START_DATA_ADDRESS;
        addresses[3] = END_DATA_ADDRESS;
    }

    private JPanel createContentPane() {
        final JPanel panel = new JPanel();

        final JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setLayout(new GridLayout(2, 1, 5, 5));
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        panel.add(createPanel(PROGRAM_REGION_TITLE, PROGRAM_REGION_START_ADDRESS_TEXT, startProgramAddressField,
                PROGRAM_REGION_END_ADDRESS_TEXT, endProgramAddressField));
        panel.add(createPanel(DATA_REGION_TITLE, DATA_REGION_START_ADDRESS_TEXT, startDataAddressField,
                DATA_REGION_END_ADDRESS_TEXT, endDataAddressField));
        panel.add(buttonPanel);

        return panel;
    }

    private void initEvents() {
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(final ComponentEvent event) {
                setDefaultValues();
                super.componentShown(event);
            }
        });

        okButton.addActionListener(actionEvent -> tryWriteToFile());

        cancelButton.addActionListener(actionEvent -> {
            outputFile = null;
            setVisible(false);
        });
    }

    private static JPanel createPanel(final String title, final String startText, final JTextField startField,
                                      final String endText, final JTextField endField) {
        final GridLayout grid = new GridLayout(2, 2, 5, 5);
        final JPanel panel = new JPanel();
        panel.setLayout(grid);
        panel.setBorder(BorderFactory.createTitledBorder(title));
        panel.add(new JLabel(startText));
        panel.add(startField);
        panel.add(new JLabel(endText));
        panel.add(endField);
        return panel;
    }

    private void tryWriteToFile() {
        if (outputFile != null && updateValues()) {
            TextConverter.writeToFile(cpu, currentBase, outputFile, getAddresses());
            outputFile = null;
            setVisible(false);
        }
    }

    private boolean updateValues() {
        final int radix = currentBase.toInt();
        boolean result = true;
        try {
            addresses[0] = Integer.parseInt(startProgramAddressField.getText(), radix);
            if (!Integers.isInInterval(addresses[0], Cpu.FIRST_ADDRESS, Cpu.LAST_ADDRESS)) {
                setText(PROGRAM_REGION_START_ADDRESS_ERROR);
                result = false;
            }
        }
        catch (final NumberFormatException e) {
            setText(PROGRAM_REGION_START_ADDRESS_ERROR);
            result = false;
        }
        if (result) {
            try {
                addresses[1] = Integer.parseInt(endProgramAddressField.getText(), radix);
                if (!Integers.isInInterval(addresses[1], Cpu.FIRST_ADDRESS, Cpu.LAST_ADDRESS)) {
                    setText(PROGRAM_REGION_END_ADDRESS_ERROR);
                    result = false;
                }
            }
            catch (final NumberFormatException e) {
                setText(PROGRAM_REGION_END_ADDRESS_ERROR);
                result = false;
            }
            if (result) {
                try {
                    addresses[2] = Integer.parseInt(startDataAddressField.getText(), radix);
                    if (!Integers.isInInterval(addresses[2], Cpu.FIRST_ADDRESS, Cpu.LAST_ADDRESS)) {
                        setText(DATA_REGION_START_ADDRESS_ERROR);
                        result = false;
                    }
                }
                catch (final NumberFormatException e) {
                    setText(DATA_REGION_START_ADDRESS_ERROR);
                    result = false;
                }
                if (result) {
                    try {
                        addresses[3] = Integer.parseInt(endDataAddressField.getText(), radix);
                        if (!Integers.isInInterval(addresses[3], Cpu.FIRST_ADDRESS, Cpu.LAST_ADDRESS)) {
                            setText(DATA_REGION_END_ADDRESS_ERROR);
                            result = false;
                        }
                    }
                    catch (final NumberFormatException e) {
                        setText(DATA_REGION_END_ADDRESS_ERROR);
                        result = false;
                    }
                }
            }
        }
        return result;
    }

    private int[] getAddresses() {
        return addresses;
    }

    private void setText(final String message) {
        statusBar.setText(message);
    }

    public void setBase(final Base newBase) {
        currentBase = newBase;
    }

    public void showDialog() {
        if (fileChooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
            outputFile = fileChooser.getSelectedFile();
            setDefaultValues();
            updateFields();
            setVisible(true);
        }
    }

    private void updateFields() {
        final int radix = currentBase.toInt();
        startProgramAddressField.setText(Integer.toString(addresses[0], radix));
        endProgramAddressField.setText(Integer.toString(addresses[1], radix));
        startDataAddressField.setText(Integer.toString(addresses[2], radix));
        endDataAddressField.setText(Integer.toString(addresses[3], radix));
    }
}
