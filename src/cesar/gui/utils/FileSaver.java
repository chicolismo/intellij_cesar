package cesar.gui.utils;

import cesar.Properties;
import org.apache.commons.io.FilenameUtils;

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

    // 03524D53 RMS
    private static final byte[] HEADER = new byte[] { 0x03, 0x52, 0x4D, 0x53 };

    private static final String FILE_FILTER_DESCRIPTION = Properties.getProperty("FileSaver.fileFilterDescription");
    private static final String FILE_FILTER_EXTENSIONS = Properties.getProperty("FileSaver.fileFilterExtensions");
    private static final String[] VALID_EXTENSIONS = FILE_FILTER_EXTENSIONS.split(",\\s*");
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
        if (fileChooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            if (!file.isDirectory()) {
                String filePath = getFilePath(file);
                if (filePath == null) {
                    // Exibir mensagem de erro
                    return false;
                }
                if (!file.exists() || JOptionPane.showConfirmDialog(parent,
                        "Deseja sobreescrever o arquivo?", "Arquivo já existe", JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {
                    try (BufferedOutputStream outStream = new BufferedOutputStream(
                            Files.newOutputStream(Paths.get(filePath)))) {
                        outStream.write(HEADER, 0, HEADER.length);
                        outStream.write(bytes, 0, bytes.length);
                        return true;
                    }
                    catch (IOException e) {
                        // Exibir mensagem de erro?
                    }
                }
            }
        }
        return false;
    }

    private static String getFilePath(File file) {
        String extension = FilenameUtils.getExtension(file.getName());
        if (VALID_EXTENSIONS_SET.contains(extension)) {
            return file.getAbsolutePath();
        }
        else if (extension.isEmpty()) {
            return file.getAbsolutePath() + "." + VALID_EXTENSIONS[0];
        }
        else {
            return null;
        }
    }
}
