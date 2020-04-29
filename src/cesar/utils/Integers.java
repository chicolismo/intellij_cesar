package cesar.utils;

public class Integers {
    private Integers() {
    }


    public static int clamp(final int value) {
        return 0xFFFF & value;
    }

    public static short clampToShort(final int value) {
        return (short) clamp(value);
    }
}
