package cesar.utils.textual;

import java.util.ArrayList;

public final class Line {
    private final ArrayList<Byte> bytes;
    private int address;
    private String mnemonic;

    public Line() {
        bytes = new ArrayList<>();
    }

    public void addByte(final byte b) {
        bytes.add(b);
    }

    public String asString(final int maxByteCount, final String addressFormat, final String byteFormat,
            final String emptyString, final String byteSeparator, final String endLine) {

        final StringBuilder builder = new StringBuilder();
        builder.append(String.format(addressFormat, address).toUpperCase()).append("   ");

        int counter = maxByteCount;
        for (final byte b : bytes) {
            builder.append(String.format(byteFormat, 0xFF & b).toUpperCase()).append(byteSeparator);
            --counter;
        }
        while (counter > 0) {
            builder.append(emptyString).append(byteSeparator);
            --counter;
        }

        builder.append(" ").append(mnemonic).append(endLine);
        return builder.toString();
    }

    public int getAddress() {
        return address;
    }

    public int getBytesSize() {
        return bytes.size();
    }

    public void setAddress(final int address) {
        this.address = address;
    }

    public void setString(final String mnemonic) {
        this.mnemonic = mnemonic;
    }
}
