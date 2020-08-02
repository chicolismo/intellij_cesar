package cesar.views.utils;

import cesar.utils.FileUtils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;

public class FileSaver {

    // 03 52 4D 53 RMS
    private static final byte[] FILE_HEADER = new byte[]{0x03, 0x52, 0x4D, 0x53};
    private static final String FILE_FILTER_DESCRIPTION = "Arquivos do Cesar (*.mem)";
    private static final String FILE_FILTER_EXTENSIONS = "mem";
    private static final String OVERWRITE_DIALOG_TITLE = "Deseja sobreescrever o arquivo?";
    private static final String OVERWRITE_DIALOG_MESSAGE = "Arquivo já existe";
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

    public boolean saveFile(final byte[] bytes) {
        boolean result = false;
        final File file = getFile();
        if (file != null && !file.isDirectory()) {
            final String filePath = getFilePath(file);
            if (filePath == null) {
                JOptionPane.showMessageDialog(parent, "Extensão de arquivo inválida");
            }
            else if (!file.exists() || JOptionPane
                    .showConfirmDialog(parent, OVERWRITE_DIALOG_MESSAGE, OVERWRITE_DIALOG_TITLE,
                            JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {
                try (BufferedOutputStream outStream = new BufferedOutputStream(
                        Files.newOutputStream(Paths.get(filePath)))) {
                    outStream.write(FILE_HEADER, 0, FILE_HEADER.length);
                    outStream.write(bytes, 0, bytes.length);
                    result = true;
                }
                catch (final IOException e) {
                    final String message =
                            String.format("Um erro ocorreu ao tentar salvar o arquivo \"%s\"\n%s", file.getName(),
                                    e.getMessage());
                    JOptionPane
                            .showMessageDialog(parent, message, "Erro ao salvar o arquivo", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    private File getFile() {
        if (fileChooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        }
        else {
            return null;
        }
    }

    private static String getFilePath(final File file) {
        final String extension = FileUtils.getFileExtension(file.getName());

        String filePath = null;
        if (VALID_EXTENSIONS_SET.contains(extension)) {
            filePath = file.getAbsolutePath();
        }
        else if (extension.isEmpty()) {
            filePath = String.format("%s.%s", file.getAbsolutePath(), VALID_EXTENSIONS[0]);
        }
        return filePath;
    }
}
