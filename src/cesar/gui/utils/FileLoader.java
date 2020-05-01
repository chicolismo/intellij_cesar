package cesar.gui.utils;

import static cesar.Properties.getProperty;

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

import org.apache.commons.io.FilenameUtils;

import cesar.hardware.Cpu;
import cesar.utils.Base;
import cesar.utils.FileUtils;

public class FileLoader {
    public static class FileLoaderException extends Exception {
        private static final long serialVersionUID = 6949583264037362079L;

        public FileLoaderException(final String message) {
            super(message);
        }
    }

    private static class AddressDialog {
        private final String message;
        private final String initialValue;

        public AddressDialog(final String message, final String initialValue) {
            this.message = message;
            this.initialValue = initialValue;
        }

        public String showDialog(final JFrame parent) {
            return (String) JOptionPane.showInputDialog(parent, message, PARTIAL_LOAD_DIALOG_TITLE,
                    JOptionPane.PLAIN_MESSAGE, null, null, initialValue);
        }
    }

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

    static {
        VALID_EXTENSIONS.addAll(Arrays.asList(FileUtils.splitExtensions(FILE_LOAD_EXTENSIONS)));
    }

    private final JFileChooser fileChooser;

    private final JFrame parent;

    public FileLoader(final JFrame parent) {
        this.parent = parent;
        fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileFilter(new FileFilter() {
            /**
             * Apenas arquivos com a extensão ".mem" e o tamanho de 64kb são exibidos no
             * diálogo.
             */
            @Override
            public boolean accept(final File file) {
                if (file != null) {
                    if (file.isDirectory()) {
                        return true;
                    }
                    else {
                        final String extension = FilenameUtils.getExtension(file.getName());
                        return VALID_EXTENSIONS.contains(extension) && CESAR_FILE_SIZE == (int) file.length();
                    }
                }
                return false;
            }

            @Override
            public String getDescription() {
                return FILE_LOAD_DESCRIPTION;
            }
        });

    }

    private static boolean hasCorrectFileSize(final File file) {
        return CESAR_FILE_SIZE == (int) file.length();
    }

    private static boolean isValidAddress(final int address) {
        return Cpu.isValidAddress(address);
    }

    private static byte[] readBytes(final File file) throws FileLoaderException {
        try {
            if (!hasCorrectFileSize(file)) {
                throw new FileLoaderException(WRONG_SIZE_MESSAGE);
            }
            final BufferedInputStream stream = new BufferedInputStream(new FileInputStream(file));
            final byte[] fileBytes = new byte[BUFFER_SIZE];
            stream.skip(4); // Os quatro primeiros bytes são o cabeçalho do arquivo.
            final int bytesRead = stream.read(fileBytes, 0, BUFFER_SIZE);
            stream.close();
            if (bytesRead != BUFFER_SIZE) {
                throw new FileLoaderException("Não foi possível ler todos os bytes do arquivo");
            }
            return fileBytes;
        }
        catch (final IOException e) {
            throw new FileLoaderException("Ocorreu um erro ao ler o arquivo.");
        }
    }

    public boolean loadFile(final Cpu cpu) throws FileLoaderException {
        final File file = showDialog();
        if (file != null) {
            cpu.setMemory(readBytes(file));
            return true;
        }
        return false;
    }


    public boolean loadFilePartially(final Cpu cpu, final Base base) throws FileLoaderException {
        final File file = showDialog();
        if (file != null) {
            final int[] addresses = getAddresses(base);
            if (addresses != null) {
                cpu.setMemory(readBytes(file), addresses[0], addresses[1], addresses[2]);
                return true;
            }
        }
        return false;
    }

    private int[] getAddresses(final Base base) throws FileLoaderException {
        final int[] addresses = new int[3];

        final AddressDialog[] dialogs = new AddressDialog[] {
            new AddressDialog(START_ADDRESS_MESSAGE, Integer.toString(MIN_ADDRESS, base.toInt())),
            new AddressDialog(END_ADDRESS_MESSAGE, Integer.toString(MAX_ADDRESS, base.toInt())),
            new AddressDialog(TARGET_ADDRESS_MESSAGE, Integer.toString(MIN_ADDRESS, base.toInt())) };

        String input = "";
        try {
            input = dialogs[0].showDialog(parent);
            if (input == null) {
                return null;
            }

            final int startAddress = Integer.parseInt(input, 10);
            if (isValidAddress(startAddress)) {
                addresses[0] = startAddress;
            }
            else {
                throw new FileLoaderException(String.format(ERROR_MESSAGE_FORMAT, input));
            }

            input = dialogs[1].showDialog(parent);
            final int endAddress = Integer.parseInt(input, 10);
            if (isValidAddress(endAddress) && endAddress > startAddress) {
                addresses[1] = endAddress;
            }
            else {
                throw new FileLoaderException(String.format(ERROR_MESSAGE_FORMAT, input));
            }

            input = dialogs[2].showDialog(parent);
            final int targetAddress = Integer.parseInt(input, 10);
            if (isValidAddress(targetAddress)) {
                addresses[2] = targetAddress;
            }
            else {
                throw new FileLoaderException(String.format(ERROR_MESSAGE_FORMAT, input));
            }
        }
        catch (final NumberFormatException e) {
            throw new FileLoaderException(String.format(ERROR_MESSAGE_FORMAT, input));
        }

        return addresses;
    }

    private File showDialog() {
        if (fileChooser.showDialog(parent, null) == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        }
        return null;
    }
}
