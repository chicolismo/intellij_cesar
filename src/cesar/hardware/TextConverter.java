package cesar.hardware;

import cesar.utils.Base;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Objects;

public class TextConverter {
    private static final String ENDL = System.lineSeparator();

    private static final String BYTE_SEPARATOR = "  ";

    private final Component parent;
    private final Cpu cpu;
    private final String addressFormat;
    private final String byteFormat;
    private final String emptyString;
    private int maxByteCount;
    private final JFileChooser fileChooser;

    private TextConverter(final Component parent, final Cpu cpu, final Base base) {
        this.parent = parent;
        this.cpu = cpu;
        addressFormat = base == Base.DECIMAL ? "%5d" : "%4x";
        byteFormat = base == Base.DECIMAL ? "%3d" : "%2x";
        emptyString = base == Base.DECIMAL ? "   " : "  ";
        fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Arquivos texto (*.txt)", "txt"));
    }

    public void showDialog() {
        if (fileChooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
            final File file = fileChooser.getSelectedFile();
            writeToFile(file);
        }
    }

    public static void saveAsText(final Component parent, final Cpu cpu, final Base base) {
        TextConverter converter = new TextConverter(parent, cpu, base);
        converter.showDialog();
    }

    private void writeToFile(final File file) {
        try {
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.US_ASCII);
            for (final Line line : getLines()) {
                writer.write(convertLineToString(line));
            }
            writer.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String convertLineToString(final Line line) {
        final StringBuilder builder = new StringBuilder();

        builder.append(String.format(addressFormat, line.address).toUpperCase()).append("   ");

        int counter = maxByteCount;
        for (final byte b : line.bytes) {
            builder.append(String.format(byteFormat, 0xFF & b).toUpperCase()).append(BYTE_SEPARATOR);
            --counter;
        }
        while (counter > 0) {
            builder.append(emptyString).append(BYTE_SEPARATOR);
            --counter;
        }

        builder.append(" ").append(line.string).append(ENDL);
        return builder.toString();
    }

    private ArrayList<Line> getLines() {
        final String[] strings = cpu.getMnemonics();
        final byte[] memory = cpu.getMemory();

        // TODO: Obter o tamanho da área de código a partir da caixa de diálogo
        final int codeSize = Cpu.MEMORY_SIZE;

        final ArrayList<Line> lines = new ArrayList<>();
        Line line = null;

        for (int i = 0; i < codeSize; ++i) {
            final String string = strings[i];
            if (!string.isEmpty()) {
                if (line != null) {
                    lines.add(line);
                    maxByteCount = Math.max(maxByteCount, line.bytes.size());
                }
                line = new Line();
                line.setAddress(i);
                line.setString(string);
                line.addByte(memory[i]);
            }
            else {
                Objects.requireNonNull(line).addByte(memory[i]);
            }
        }
        lines.add(line);
        maxByteCount = Math.max(maxByteCount, line.bytes.size());
        return lines;
    }

    protected static class Line {
        private int address;
        private String string;
        private final ArrayList<Byte> bytes;

        public Line() {
            bytes = new ArrayList<>();
        }

        public void setAddress(final int value) {
            address = value;
        }

        public void addByte(final byte b) {
            bytes.add(b);
        }

        public void setString(final String mnemonic) {
            string = mnemonic;
        }
    }
}
