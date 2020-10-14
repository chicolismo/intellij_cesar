package cesar.utils.text;

import java.util.ArrayList;

public class LineReaderResult {
    private final int maxByteCount;
    private final ArrayList<Line> lines;

    public LineReaderResult(final int maxByteCount, final ArrayList<Line> lines) {
        this.maxByteCount = maxByteCount;
        this.lines = lines;
    }

    public ArrayList<Line> getLines() {
        return lines;
    }

    public int getMaxByteCount() {
        return maxByteCount;
    }
}