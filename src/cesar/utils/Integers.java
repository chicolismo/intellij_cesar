package cesar.utils;

public class Integers {
    private Integers() {
    }


    public static int clamp(final int value) {
        return 0xFFFF & value;
    }
}
