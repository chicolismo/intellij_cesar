package cesar.utils;

public enum Base {
    DECIMAL, HEXADECIMAL;

    public int toInt() {
        return Base.toInt(this);
    }

    public static int toInt(final Base base) {
        return base == DECIMAL ? 10 : 16;
    }

    @Override
    public String toString() {
        if (this == DECIMAL) {
            return "decimal";
        }
        else {
            return "hexadecimal";
        }
    }
}
