package cesar.utils;

public class Shorts {
    public static short fromBytes(final byte msb, final byte lsb) {
        return (short) (0xFFFF & ((0xFF & msb) << 8 | 0xFF & lsb));
    }

    public static byte[] toBytes(final short s) {
        final byte msb = (byte) ((s & 0xFF00) >> 4);
        final byte lsb = (byte) (s & 0xFF);
        return new byte[] { msb, lsb };
    }

    public static int toUnsignedInt(final short s) {
        return s & 0xffff;
    }

    private Shorts() {
    }
}
