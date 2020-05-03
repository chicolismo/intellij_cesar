package cesar.utils;

public class Bytes {
    public static final int MIN_VALUE = Byte.MIN_VALUE;
    public static final int MAX_VALUE = 0xFF;

    private Bytes() {
    }

    public static byte fromInt(final int value) {
        return (byte) (MAX_VALUE & value);
    }

    public static boolean isValidByte(final int value) {
        return Integers.isInInterval(value, MIN_VALUE, MAX_VALUE);
    }

    public static int toUnsignedInt(final byte b) {
        return MAX_VALUE & b;
    }
}
