package cesar.models;

public enum Base {
    DECIMAL, HEXADECIMAL;

    public static int toInt(final Base base) {
        return base == DECIMAL ? 10 : 16;
    }

    public int toInt() {
        return Base.toInt(this);
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
