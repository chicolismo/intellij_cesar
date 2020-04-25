package cesar.gui.dialogs;

import cesar.hardware.Cpu;
import cesar.hardware.TextConverter;
import cesar.utils.Base;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;

public class SaveTextDialog extends JDialog {
    private static final int MIN_VALUE = 0;
    private static final int MAX_VALUE = Cpu.MEMORY_SIZE - 1;

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
        super(parent, "Salvar texto", true);

        fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Arquivos texto (*.txt)", "txt"));

        okButton = new JButton("Ok");
        cancelButton = new JButton("Cancelar");
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
                    TextConverter converter = new TextConverter(cpu, base);
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

    public void saveText(Cpu cpu, Base base) {
        if (fileChooser.showSaveDialog(getParent()) == JFileChooser.APPROVE_OPTION) {
            this.cpu = cpu;
            this.base = base;
            outputFile = fileChooser.getSelectedFile();
            setVisible(true);
        }
    }

    private void setText(String message) {
        statusBar.setText(message);
    }

    private void convertValues() {
        valuesOk = false;

        try {
            startProgramAddress = Integer.parseInt(startProgramAddressField.getText(), 10);
            if (startProgramAddress < MIN_VALUE || startProgramAddress > MAX_VALUE) {
                setText("Erro: Endereço inicial do programa incorreto.");
                return;
            }
        }
        catch (NumberFormatException e) {
            setText("Erro: Endereço inicial do programa incorreto.");
            return;
        }

        try {
            endProgramAddress = Integer.parseInt(endProgramAddressField.getText(), 10);
            if (endProgramAddress < 0 || endProgramAddress > MAX_VALUE) {
                setText("Erro: Endereço final do programa incorreto.");
                return;
            }
        }
        catch (NumberFormatException e) {
            setText("Erro: Endereço final do programa incorreto.");
            return;
        }

        try {
            startDataAddress = Integer.parseInt(startDataAddressField.getText(), 10);
            if (startDataAddress < MIN_VALUE || startDataAddress > MAX_VALUE) {
                setText("Erro: Endereço inicial dos dados incorreto.");
                return;
            }
        }
        catch (NumberFormatException e) {
            setText("Erro: Endereço inicial dos dados incorreto.");
            return;
        }

        try {
            endDataAddress = Integer.parseInt(endDataAddressField.getText(), 10);
            if (endDataAddress < MIN_VALUE || endDataAddress > MAX_VALUE) {
                setText("Erro: Endereço final dos dados incorreto.");
                return;
            }
        }
        catch (NumberFormatException e) {
            setText("Erro: Endereço final dos dados incorreto.");
            return;
        }

        valuesOk = true;
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

    private JPanel createContentPane() {
        final JPanel panel = new JPanel();
        final JPanel programPanel = new JPanel();
        final JPanel dataPanel = new JPanel();
        final JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));

        final GridLayout programGrid = new GridLayout(2, 2, 5, 5);
        final GridLayout dataGrid = new GridLayout(2, 2, 5, 5);
        programPanel.setLayout(programGrid);
        programPanel.setBorder(
                BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Região do programa",
                        TitledBorder.LEFT, TitledBorder.TOP));
        programPanel.add(new JLabel("Endereço inicial"));
        programPanel.add(startProgramAddressField);
        programPanel.add(new JLabel("Endereço final"));
        programPanel.add(endProgramAddressField);
        dataPanel.setLayout(dataGrid);
        dataPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Região de dados",
                TitledBorder.LEFT, TitledBorder.TOP));
        dataPanel.add(new JLabel("Endereço inicial"));
        dataPanel.add(startDataAddressField);
        dataPanel.add(new JLabel("Endereço final"));
        dataPanel.add(endDataAddressField);

        buttonPanel.setLayout(new GridLayout(2, 1, 5, 5));
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        panel.add(programPanel);
        panel.add(dataPanel);
        panel.add(buttonPanel);

        return panel;
    }

}