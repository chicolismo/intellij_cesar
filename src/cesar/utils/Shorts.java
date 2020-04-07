package cesar.utils;

public class Shorts {
    private Shorts() {
    }

    public static short fromBytes(byte msb, byte lsb) {
        return (short) (0xFFFF & (((0xFF & msb) << 8) | (0xFF & lsb)));
    }

    public static int toUnsignedInt(short s) {
        return s & 0xffff;
    }

    public static long toUnsignedLong(short s) {
        return s & 0xffff;
    }
}
