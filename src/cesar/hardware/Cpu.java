package cesar.hardware;

public class Cpu {
    public static final int MEMORY_SIZE = 1 << 16;
    public static final int KEYBOARD_STATE_ADDRESS = 65498;
    public static final int LAST_CHAR_ADDRESS = 65499;
    public static final int BEGIN_DISPLAY_ADDRESS = 65500;
    public static final int END_DISPLAY_ADDRESS = 65535;

    private byte[] memory;

    public Cpu() {
        memory = new byte[MEMORY_SIZE];
        for (int i = 0; i < MEMORY_SIZE; ++i) {
//            memory[i] = (byte) i;
            memory[i] = (byte) 60;
        }
    }

    public byte[] getMemory() {
        return memory;
    }
}
