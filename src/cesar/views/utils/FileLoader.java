package cesar.views.utils;

import cesar.models.Base;
import cesar.models.Cpu;
import cesar.utils.Defaults;
import cesar.utils.FileUtils;
import cesar.views.panels.StatusBar;
import cesar.views.windows.MainWindow;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;

public class FileLoader {
    private static final int CESAR_FILE_SIZE = 0x10004; // 65_540

    private static final int BUFFER_SIZE = Cpu.MEMORY_SIZE; // 0x10000 ou 65_536

    private static final int MIN_ADDRESS = Cpu.FIRST_ADDRESS;

    private static final int MAX_ADDRESS = Cpu.LAST_ADDRESS;
    private static final String WRONG_SIZE_MESSAGE = "Tamanho de arquivo incorreto";
    private static final String PARTIAL_LOAD_DIALOG_TITLE = "Carga Parcial de Memória";
    private static final String ERROR_MESSAGE_FORMAT = "ERRO: Posição de memória inválida (%s)";
    private static final String START_ADDRESS_MESSAGE = "Digite o endereço inicial da memória a copiar";
    private static final String END_ADDRESS_MESSAGE = "Digite o endereço final da memória a copiar";
    private static final String TARGET_ADDRESS_MESSAGE = "Digite o endereço de destino";
    private static final String FILE_DESCRIPTION = "Arquivos do Cesar (*.mem)";
    private static final String FILE_EXTENSION = "mem";

    private final StatusBar statusBar;
    private final Cpu cpu;
    private final JFileChooser fileChooser;
    private final MainWindow parent;
    private int startAddress, endAddress, targetAddress;
    private Base base;
    private File currentFile;

    public FileLoader(final MainWindow parent) {
        this.parent = parent;
        statusBar = parent.getStatusBar();
        base = Defaults.DEFAULT_BASE;
        cpu = parent.getCpu();

        fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileFilter(new CesarFileFilter());
    }

    public boolean loadFile() {
        boolean result = false;
        if (showDialog()) {
            cpu.setMemory(Objects.requireNonNull(readBytes(currentFile)));
            result = true;
        }
        return result;
    }

    private boolean showDialog() {
        boolean result = false;
        final int choice = fileChooser.showDialog(parent, null);
        if (choice == JFileChooser.APPROVE_OPTION) {
            currentFile = fileChooser.getSelectedFile();
            result = currentFile.isFile();
            if (!result) {
                final String message = "O arquivo selecionado não é válido: " + currentFile.getAbsolutePath();
                JOptionPane.showMessageDialog(parent, message, "Erro ao ler o arquivo", JOptionPane.ERROR_MESSAGE);
            }
        }
        return result;
    }

    private byte[] readBytes(final File file) {
        if (hasCorrectFileSize(file)) {
            try (var inputStream = new BufferedInputStream(new FileInputStream(file))) {
                final byte[] fileBytes = new byte[BUFFER_SIZE];
                final int headerSize = 4; // Os quatro primeiros bytes são o cabeçalho do arquivo.
                if (inputStream.skip(headerSize) != headerSize) {
                    statusBar.setTempMessage(
                            String.format("Não foi possível avançar o cabeçalho do arquivo \"%s\"", file.getName()));
                }
                final int bytesRead = inputStream.read(fileBytes, 0, BUFFER_SIZE);
                if (bytesRead != BUFFER_SIZE) {
                    statusBar.setTempMessage("Não foi possível ler todos os bytes do arquivo");
                }
                return fileBytes;
            }
            catch (final IOException e) {
                statusBar.setTempMessage("Ocorreu um erro ao ler o arquivo.");
            }
        }
        else {
            statusBar.setTempMessage(WRONG_SIZE_MESSAGE);
        }
        return null;
    }

    private static boolean hasCorrectFileSize(final File file) {
        return CESAR_FILE_SIZE == (int) file.length();
    }

    public boolean loadFilePartially() {
        if (showDialog() && getAddresses()) {
            cpu.setMemory(readBytes(currentFile), startAddress, endAddress, targetAddress);
            return true;
        }
        return false;
    }

    private boolean getAddresses() {
        final int radix = base.toInt();
        final var startAddressDialog = new AddressDialog(START_ADDRESS_MESSAGE, Integer.toString(MIN_ADDRESS, radix));
        final var endAddressDialog = new AddressDialog(END_ADDRESS_MESSAGE, Integer.toString(MAX_ADDRESS, radix));
        final var targetAddressDialog = new AddressDialog(TARGET_ADDRESS_MESSAGE, Integer.toString(MIN_ADDRESS, radix));

        try {
            String userInput = startAddressDialog.showDialog(parent);
            if (userInput != null) {
                startAddress = Integer.parseInt(userInput, radix);
                if (isInvalidAddress(startAddress)) {
                    statusBar.setTempMessage(String.format(ERROR_MESSAGE_FORMAT, userInput));
                    return false;
                }

                userInput = endAddressDialog.showDialog(parent);
                endAddress = Integer.parseInt(userInput, radix);
                if (isInvalidAddress(endAddress) || endAddress <= startAddress) {
                    statusBar.setTempMessage(String.format(ERROR_MESSAGE_FORMAT, userInput));
                    return false;
                }

                userInput = targetAddressDialog.showDialog(parent);
                targetAddress = Integer.parseInt(userInput, radix);
                if (isInvalidAddress(targetAddress)) {
                    statusBar.setTempMessage(String.format(ERROR_MESSAGE_FORMAT, userInput));
                    return false;
                }
            }
        }
        catch (final NumberFormatException ignore) {
            return false;
        }
        return true;
    }

    private static boolean isInvalidAddress(final int address) {
        return !Cpu.isValidAddress(address);
    }

    public void setBase(final Base newBase) {
        base = newBase;
    }


    private static class AddressDialog {
        private final String message;
        private final String initialValue;

        public AddressDialog(final String message, final String initialValue) {
            this.message = message;
            this.initialValue = initialValue;
        }

        public String showDialog(final Component parent) {
            return (String) JOptionPane
                    .showInputDialog(parent, message, PARTIAL_LOAD_DIALOG_TITLE, JOptionPane.PLAIN_MESSAGE, null, null,
                            initialValue);
        }
    }


    private static class CesarFileFilter extends FileFilter {
        @Override
        public boolean accept(final File file) {
            if (file != null) {
                if (file.isDirectory()) {
                    return true;
                }
                else {
                    return FileUtils.getFileExtension(file.getName()).toLowerCase().equals(FILE_EXTENSION);
                }
            }
            return false;
        }

        @Override
        public String getDescription() {
            return FILE_DESCRIPTION;
        }
    }
}
