package cesar.hardware;

import java.util.EnumMap;

public enum AddressMode {
    REGISTER, REGISTER_POST_INCREMENTED, REGISTER_PRE_DECREMENTED, INDEXED, REGISTER_INDIRECT, POST_INCREMENTED_INDIRECT, PRE_DECREMENTED_INDIRECT, INDEXED_INDIRECT;

    private static final AddressMode[] ARRAY = AddressMode.values();

    private static final EnumMap<AddressMode, String> FORMAT;

    static {
        FORMAT = new EnumMap<>(AddressMode.class);
        FORMAT.put(REGISTER, "R%d");
        FORMAT.put(REGISTER_POST_INCREMENTED, "(R%d)+");
        FORMAT.put(REGISTER_PRE_DECREMENTED, "-(R%d)");
        FORMAT.put(INDEXED, "%d(R%d)");
        FORMAT.put(REGISTER_INDIRECT, "(R%d)");
        FORMAT.put(POST_INCREMENTED_INDIRECT, "((R%d)+)");
        FORMAT.put(PRE_DECREMENTED_INDIRECT, "(-(R%d))");
        FORMAT.put(INDEXED_INDIRECT, "(%d(R%d))");
    }


    public static AddressMode fromInt(final int index) {
        return ARRAY[index];
    }

    public String asString(final int rrr) {
        return String.format(FORMAT.get(this), rrr);
    }

    public String asString(final int ddd, final int rrr) {
        return String.format(FORMAT.get(this), ddd, rrr);
    }

    public boolean isIndexed() {
        return this == INDEXED || this == INDEXED_INDIRECT;
    }

    public boolean isPostIncremented() {
        return this == REGISTER_POST_INCREMENTED || this == POST_INCREMENTED_INDIRECT;
    }
}