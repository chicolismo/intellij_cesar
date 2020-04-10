package cesar.utils;

public class Shorts {
    private Shorts() {
    }

    public static short fromBytes(byte msb, byte lsb) {
        return (short) (0xFFFF & (((0xFF & msb) << 8) | (0xFF & lsb)));
    }

    public static int toUnsignedInt(final short s) {
        return s & 0xffff;
    }

    public static long toUnsignedLong(final short s) {
        return s & 0xffff;
    }

    public static byte[] toBytes(final short s) {
        final byte msb = (byte) ((s & 0xFF00) >> 4);
        final byte lsb = (byte) (s & 0xFF);
        return new byte[]{msb, lsb};
    }
}
