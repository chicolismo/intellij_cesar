package cesar.utils.textual;

import java.util.ArrayList;
import java.util.Objects;

import cesar.hardware.Cpu;

public class LineReader {

    private LineReader() {
    }

    public static LineReaderResult readLines(final Cpu cpu) {
        final String[] mnemonics = cpu.getMnemonics();
        final byte[] memory = cpu.getMemory();
        final int memorySize = memory.length;

        final ArrayList<Line> lines = new ArrayList<>();
        int maxByteCount = 0;
        Line line = null;
        for (int i = 0; i < memorySize; ++i) {
            final String mnemonic = mnemonics[i];
            if (!mnemonic.isEmpty()) {
                if (line != null) {
                    lines.add(line);
                    maxByteCount = Math.max(maxByteCount, line.getBytesSize());
                }
                // noinspection ObjectAllocationInLoop
                line = new Line();
                line.setAddress(i);
                line.setString(mnemonic);
                line.addByte(memory[i]);
            }
            else {
                Objects.requireNonNull(line).addByte(memory[i]);
            }
        }
        if (line != null) {
            lines.add(line);
            maxByteCount = Math.max(maxByteCount, line.getBytesSize());
        }

        return new LineReaderResult(maxByteCount, lines);
    }
}