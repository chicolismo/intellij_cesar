package cesar.utils;

public enum Base {
    DECIMAL, HEXADECIMAL;

    public static int toInt(Base base) {
        return base == DECIMAL ? 10 : 16;
    }
}
