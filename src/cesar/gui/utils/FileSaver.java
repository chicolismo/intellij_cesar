package cesar.gui.utils;

import java.awt.Component;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FilenameUtils;

import cesar.Properties;
import cesar.utils.FileUtils;

public class FileSaver {

    // 03 52 4D 53 RMS
    private static final byte[] HEADER = new byte[] { 0x03, 0x52, 0x4D, 0x53 };
    private static final String FILE_FILTER_DESCRIPTION = Properties.getProperty("FileSaver.fileFilterDescription");
    private static final String FILE_FILTER_EXTENSIONS = Properties.getProperty("FileSaver.fileFilterExtensions");
    private static final String OVERWRITE_DIALOG_TITLE = Properties.getProperty("FileSaver.overwriteDialogTitle");
    private static final String OVERWRITE_DIALOG_MESSAGE = Properties.getProperty("FileSaver.overwriteDialogMessage");
    private static final String[] VALID_EXTENSIONS = FileUtils.splitExtensions(FILE_FILTER_EXTENSIONS);
    private static final HashSet<String> VALID_EXTENSIONS_SET = new HashSet<>();
    static {
        VALID_EXTENSIONS_SET.addAll(Arrays.asList(VALID_EXTENSIONS));
    }


    private final Component parent;

    private final JFileChooser fileChooser;

    public FileSaver(final Component parent) {
        this.parent = parent;

        fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileFilter(new FileNameExtensionFilter(FILE_FILTER_DESCRIPTION, FILE_FILTER_EXTENSIONS));
    }

    private static String getFilePath(final File file) {
        final String extension = FilenameUtils.getExtension(file.getName());
        if (VALID_EXTENSIONS_SET.contains(extension)) {
            return file.getAbsolutePath();
        }
        else if (extension.isEmpty()) {
            return String.format("%s.%s", file.getAbsolutePath(), VALID_EXTENSIONS[0]);
        }
        else {
            return null;
        }
    }

    public boolean saveFile(final byte[] bytes) {
        final File file = getFile();
        if (file != null && !file.isDirectory()) {
            final String filePath = getFilePath(file);
            if (filePath == null) {
                JOptionPane.showMessageDialog(parent, "Extensão de arquivo inválida");
                return false;
            }
            if (!file.exists() || JOptionPane.showConfirmDialog(parent, OVERWRITE_DIALOG_MESSAGE,
                    OVERWRITE_DIALOG_TITLE, JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {
                try (BufferedOutputStream outStream = new BufferedOutputStream(
                        Files.newOutputStream(Paths.get(filePath)))) {
                    outStream.write(HEADER, 0, HEADER.length);
                    outStream.write(bytes, 0, bytes.length);
                    return true;
                }
                catch (final IOException e) {
                    final String message = String.format("Um erro ocorreu ao tentar salvar o arquivo \"%s\"\n%s",
                            file.getName(), e.getMessage());
                    JOptionPane.showMessageDialog(parent, message, "Erro ao salvar o arquivo",
                            JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    private File getFile() {
        if (fileChooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        }
        return null;
    }
}
