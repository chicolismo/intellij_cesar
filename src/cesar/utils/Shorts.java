package cesar.utils;

public class Shorts {
    public static short fromBytes(final byte msb, final byte lsb) {
        return (short) (0xFFFF & ((0xFF & msb) << 8 | 0xFF & lsb));
    }

    public static short fromInt(final int value) {
        return (short) (0xFFFF & value);
    }

    public static byte[] toBytes(final short value) {
        final byte msb = (byte) ((value & 0xFF00) >> 4);
        final byte lsb = (byte) (value & 0xFF);
        return new byte[] { msb, lsb };
    }

    public static int toUnsignedInt(final short value) {
        return value & 0xFFFF;
    }

    public static boolean isValidShort(final int value) {
        return value >= Short.MIN_VALUE && value <= 0xFFFF;
    }

    private Shorts() {
    }
}
