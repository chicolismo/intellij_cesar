package cesar.utils;

public class Bytes {
    private Bytes() {
    }

    public static boolean isValidByte(final int value) {
        return value >= Byte.MIN_VALUE && value <= 0xFF;
    }
}
