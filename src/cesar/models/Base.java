package cesar.models;

public enum Base {
    DECIMAL, HEXADECIMAL;

    private static final String DECIMAL_STRING = "decimal";
    private static final String HEXADECIMAL_STRING = "hexadecimal";
    private static final int DECIMAL_INT = 10;
    private static final int HEXADECIMAL_INT = 16;

    public int toInt() {
        return Base.toInt(this);
    }

    public static int toInt(final Base base) {
        return base == DECIMAL ? DECIMAL_INT : HEXADECIMAL_INT;
    }

    @Override
    public String toString() {
        return this == DECIMAL ? DECIMAL_STRING : HEXADECIMAL_STRING;
    }
}
