package cesar.utils.textual;

import java.util.ArrayList;

public final class Line {
    private int address;
    private String mnemonic;
    private final ArrayList<Byte> bytes;

    public Line() {
        bytes = new ArrayList<>();
    }

    public void addByte(final byte b) {
        bytes.add(b);
    }

    public String asString(int counter, final String addressFormat, final String byteFormat, final String emptyString,
            final String byteSeparator, final String endLine) {
        final StringBuilder builder = new StringBuilder();

        builder.append(String.format(addressFormat, address).toUpperCase()).append("   ");

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

    public ArrayList<Byte> getBytes() {
        return bytes;
    }

    public int getBytesSize() {
        return bytes.size();
    }

    public String getMnemonic() {
        return mnemonic;
    }

    public void setAddress(final int address) {
        this.address = address;
    }

    public void setString(final String mnemonic) {
        this.mnemonic = mnemonic;
    }
}
