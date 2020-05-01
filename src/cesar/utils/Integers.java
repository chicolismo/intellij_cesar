package cesar.utils;

public class Integers {
    private static final int CLAMP_VALUE = 0xFFFF;

    private Integers() {
    }

    public static int clamp(final int value) {
        return CLAMP_VALUE & value;
    }

    public static short clampToShort(final int value) {
        return (short) clamp(value);
    }

    public static boolean isInInterval(final int value, final int min, final int maxInclusive) {
        return isInInterval(value, min, maxInclusive, true);
    }

    public static boolean isInInterval(final int value, final int min, final int max, final boolean isMaxInclusive) {
        if (isMaxInclusive) {
            return value >= min && value <= max;
        }
        else {
            return value >= min && value < max;
        }
    }
}
