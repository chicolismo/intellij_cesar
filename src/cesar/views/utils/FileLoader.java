package cesar.views.utils;

import static cesar.utils.Properties.getProperty;

import java.awt.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import cesar.views.panels.StatusBar;
import cesar.views.windows.MainWindow;
import com.sun.istack.internal.Nullable;

import cesar.models.Base;
import cesar.models.Cpu;
import cesar.utils.Defaults;
import cesar.utils.FileUtils;

public class FileLoader {
    private static final int CESAR_FILE_SIZE = 0x10004; // 65_540

    private static final int BUFFER_SIZE = Cpu.MEMORY_SIZE; // 0x10000 ou 65_536

    private static final int MIN_ADDRESS = Cpu.FIRST_ADDRESS;

    private static final int MAX_ADDRESS = Cpu.LAST_ADDRESS;
    private static final String WRONG_SIZE_MESSAGE = "Tamanho de arquivo incorreto";
    private static final String PARTIAL_LOAD_DIALOG_TITLE = getProperty("FileLoader.partialLoadDialogTitle");
    private static final String ERROR_MESSAGE_FORMAT = getProperty("FileLoader.errorFormat");
    private static final String START_ADDRESS_MESSAGE = getProperty("FileLoader.startAddressMessage");
    private static final String END_ADDRESS_MESSAGE = getProperty("FileLoader.endAddressMessage");
    private static final String TARGET_ADDRESS_MESSAGE = getProperty("FileLoader.targetAddressMessage");
    private static final String FILE_LOAD_DESCRIPTION = getProperty("FileLoader.fileFilterDescription");
    private static final String FILE_LOAD_EXTENSIONS = getProperty("FileLoader.fileFilterExtensions");
    private static final Set<String> VALID_EXTENSIONS = new HashSet<>();
    private final StatusBar statusBar;
    private int startAddress, endAddress, targetAddress;
    private final Cpu cpu;

    static {
        VALID_EXTENSIONS.addAll(Arrays.asList(FileUtils.splitExtensions(FILE_LOAD_EXTENSIONS)));
    }

    private final JFileChooser fileChooser;
    private final MainWindow parent;
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

    private static boolean hasCorrectFileSize(final File file) {
        return CESAR_FILE_SIZE == (int) file.length();
    }

    private static boolean isInvalidAddress(final int address) {
        return !Cpu.isValidAddress(address);
    }

    @Nullable
    private byte[] readBytes(final File file) {
        byte[] result = null;
        if (hasCorrectFileSize(file)) {
            try (BufferedInputStream stream = new BufferedInputStream(new FileInputStream(file))) {
                final byte[] fileBytes = new byte[BUFFER_SIZE];
                final int headerSize = 4; // Os quatro primeiros bytes são o cabeçalho do arquivo.
                if (stream.skip(headerSize) != headerSize) {
                    statusBar.setTempMessage(
                            String.format("Não foi possível avançar o cabeçalho do arquivo \"%s\"", file.getName()));
                }
                final int bytesRead = stream.read(fileBytes, 0, BUFFER_SIZE);
                if (bytesRead != BUFFER_SIZE) {
                    statusBar.setTempMessage("Não foi possível ler todos os bytes do arquivo");
                }
                result = fileBytes;
            }
            catch (final IOException e) {
                statusBar.setTempMessage("Ocorreu um erro ao ler o arquivo.");
            }
        }
        else {
            statusBar.setTempMessage(WRONG_SIZE_MESSAGE);
        }
        return result;
    }


    private boolean getAddresses() {
        final AddressDialog startAddressDialog, endAddressDialog, targetAddressDialog;

        final int radix = base.toInt();
        startAddressDialog = new AddressDialog(START_ADDRESS_MESSAGE, Integer.toString(MIN_ADDRESS, radix));
        endAddressDialog = new AddressDialog(END_ADDRESS_MESSAGE, Integer.toString(MAX_ADDRESS, radix));
        targetAddressDialog = new AddressDialog(TARGET_ADDRESS_MESSAGE, Integer.toString(MIN_ADDRESS, radix));

        boolean result = true;
        try {
            String input = startAddressDialog.showDialog(parent);
            if (input != null) {
                startAddress = Integer.parseInt(input, radix);
                if (isInvalidAddress(startAddress)) {
                    statusBar.setTempMessage(String.format(ERROR_MESSAGE_FORMAT, input));
                    result = false;
                }
                else {
                    input = endAddressDialog.showDialog(parent);
                    endAddress = Integer.parseInt(input, radix);
                    if (isInvalidAddress(endAddress) || endAddress <= startAddress) {
                        statusBar.setTempMessage(String.format(ERROR_MESSAGE_FORMAT, input));
                        result = false;
                    }
                    else {
                        input = targetAddressDialog.showDialog(parent);
                        targetAddress = Integer.parseInt(input, radix);
                        if (isInvalidAddress(targetAddress)) {
                            statusBar.setTempMessage(String.format(ERROR_MESSAGE_FORMAT, input));
                            result = false;
                        }
                    }
                }
            }
        }
        catch (final NumberFormatException ignore) {
            result = false;
        }
        return result;
    }

    private boolean showDialog() {
        boolean result = false;
        final int choice = fileChooser.showDialog(parent, null);
        if (choice == JFileChooser.APPROVE_OPTION) {
            currentFile = fileChooser.getSelectedFile();
            result = true;
        }
        return result;
    }

    public boolean loadFile() {
        boolean result = false;
        if (showDialog()) {
            cpu.setMemory(readBytes(currentFile));
            result = true;
        }
        return result;
    }

    public boolean loadFilePartially() {
        boolean result = false;
        if (showDialog() && getAddresses()) {
            cpu.setMemory(readBytes(currentFile), startAddress, endAddress, targetAddress);
            result = true;
        }
        return result;
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
            return (String) JOptionPane.showInputDialog(parent, message, PARTIAL_LOAD_DIALOG_TITLE,
                    JOptionPane.PLAIN_MESSAGE, null, null, initialValue);
        }
    }


    private static class CesarFileFilter extends FileFilter {
        @Override
        public boolean accept(final File file) {
            boolean result = false;
            if (file != null) {
                if (file.isDirectory()) {
                    result = true;
                }
                else {
                    final String extension = FileUtils.getExtension(file.getName());
                    // Apenas arquivos com a extensão ".mem" e o tamanho de 64kb são exibidos no
                    // diálogo.
                    result = VALID_EXTENSIONS.contains(extension) && CESAR_FILE_SIZE == (int) file.length();
                }
            }
            return result;
        }

        @Override
        public String getDescription() {
            return FILE_LOAD_DESCRIPTION;
        }
    }
}
