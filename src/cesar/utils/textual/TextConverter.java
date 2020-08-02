package cesar.utils.textual;

import cesar.models.Base;
import cesar.models.Cpu;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * Converte o conteúdo da memória do CPU em um arquivo de texto.
 * <p>
 * Os endereços definidos como sendo do programa são representados com seus
 * devidos mnemônicos, juntamente com os bytes correspondentes da instrução.
 * <p>
 * Os endereços dos dados apenas contém o valor do byte daquele endereço.
 *
 * @author chico
 */
public final class TextConverter {
    private static final String ENDL = System.lineSeparator();

    private static final String BYTE_SEPARATOR = "  ";

    private TextConverter() {
    }

    public static void writeToFile(final Cpu cpu, final Base base, final File file, final int[] addresses) {

        final int startProgramAddress = addresses[0];
        final int endProgramAddress = addresses[1];
        final int startDataAddress = addresses[2];
        final int endDataAddress = addresses[3];

        try {
            final OutputStreamWriter writer =
                    new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.US_ASCII);

            final LineReaderResult result = LineReader.readLines(cpu);

            final String addressFormat = getAddressFormat(base);
            final String byteFormat = getByteFormat(base);
            final String emptyString = getEmptyString(base);
            final int maxByteCount = result.getMaxByteCount();
            final ArrayList<Line> lines = result.getLines();

            for (int i = startProgramAddress; i <= endProgramAddress; ++i) {
                final Line line = lines.get(i);
                if (line.getAddress() <= endProgramAddress) {
                    final String lineString =
                            line.asString(maxByteCount, addressFormat, emptyString, byteFormat, BYTE_SEPARATOR, ENDL);
                    writer.write(lineString);
                }
            }
            writer.write(ENDL);
            for (int i = startDataAddress; i <= endDataAddress; ++i) {
                final String addressString = String.format(addressFormat, i);
                final String byteString = String.format(byteFormat, 0xFF & cpu.getByte(i));
                writer.write(String.format("%s   %s%s", addressString, byteString, ENDL));
            }
            writer.close();
        }
        catch (final IOException e) {
            e.printStackTrace();
        }
    }

    private static String getAddressFormat(final Base base) {
        return base == Base.DECIMAL ? "%5d" : "%4x";
    }

    private static String getByteFormat(final Base base) {
        return base == Base.DECIMAL ? "%3d" : "%2x";
    }

    private static String getEmptyString(final Base base) {
        return base == Base.DECIMAL ? "   " : "  ";
    }
}
