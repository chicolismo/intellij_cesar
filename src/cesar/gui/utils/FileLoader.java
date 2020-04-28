package cesar.gui.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import cesar.ApplicationProperties;
import cesar.hardware.Cpu;
import cesar.utils.Base;

public class FileLoader {
    private static class AddressDialog {
        private final String message;
        private final String initialValue;

        public AddressDialog(final String message, final String initialValue) {
            this.message = message;
            this.initialValue = initialValue;
        }

        public String showDialog(final JFrame parent) {
            return (String) JOptionPane.showInputDialog(parent, message, "Carga Parcial de Memória",
                    JOptionPane.PLAIN_MESSAGE, null, null, initialValue);
        }
    }

    public static class FileLoaderException extends Exception {
        private static final long serialVersionUID = 6949583264037362079L;

        public FileLoaderException(final String message) {
            super(message);
        }
    }

    private static final int CESAR_FILE_SIZE = 65_540;
    private static final int MIN_ADDRESS = 0;
    private static final int MAX_ADDRESS = Cpu.MEMORY_SIZE - 1;

    private static final String ERROR_MESSAGE_FORMAT = ApplicationProperties.getProperty("FileLoader.errorFormat");

    private static boolean isValidAddress(final int address) {
        return address >= MIN_ADDRESS && address <= MAX_ADDRESS;
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
                        final String fileName = file.getName().toLowerCase();
                        return fileName.endsWith(".mem") && CESAR_FILE_SIZE == (int) file.length();
                    }
                }
                return false;
            }

            @Override
            public String getDescription() {
                return "Arquivos do Cesar (*.mem)";
            }
        });

    }

    private int[] getAddresses(final Base base) throws FileLoaderException {
        final int[] addresses = new int[3];

        final AddressDialog[] dialogs = new AddressDialog[] {
            new AddressDialog("Digite o endereço inicial da memória a copiar",
                    Integer.toString(MIN_ADDRESS, base.toInt())),
            new AddressDialog("Digite o endereço final da memória a copiar",
                    Integer.toString(MAX_ADDRESS, base.toInt())),
            new AddressDialog("Digite o endereço de destino", Integer.toString(MIN_ADDRESS, base.toInt())) };

        String input = "";
        try {
            input = dialogs[0].showDialog(parent);

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

    private File getFile() {
        if (fileChooser.showDialog(parent, null) == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        }
        else {
            return null;
        }
    }

    public boolean loadFile(final Cpu cpu) throws FileLoaderException {
        final File file = getFile();
        if (file != null) {
            cpu.setMemory(readBytes(file));
            return true;
        }
        return false;
    }

    public boolean loadFilePartially(final Cpu cpu, final Base base) throws FileLoaderException {
        final File file = getFile();
        if (file != null) {
            final int[] addresses = getAddresses(base);
            cpu.setMemory(readBytes(file), addresses[0], addresses[1], addresses[2]);
            return true;
        }
        return false;
    }


    private byte[] readBytes(final File file) throws FileLoaderException {
        try {
            final int fileSize = (int) file.length();
            if (fileSize != CESAR_FILE_SIZE) {
                throw new FileLoaderException("Tamanho do arquivo está incorreto");
            }
            final BufferedInputStream stream = new BufferedInputStream(new FileInputStream(file));
            final byte[] fileBytes = new byte[fileSize];
            final int bytesRead = stream.read(fileBytes, 0, fileSize);
            stream.close();
            if (bytesRead != fileSize) {
                throw new FileLoaderException("Não foi possível ler todos os bytes do arquivo");
            }
            return fileBytes;
        }
        catch (final IOException e) {
            throw new FileLoaderException("Ocorreu um erro ao ler o arquivo.");
        }
    }
}
