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
    }

    public void setMemory(byte[] bytes) {
        final int maxSize = Math.min(bytes.length, memory.length);
        final int offset = (bytes.length > memory.length) ? (bytes.length - memory.length) : 0;
        for (int i = 0; i < maxSize; ++i) {
            memory[i] = bytes[i + offset];
        }
    }

    public byte[] getMemory() {
        return memory;
    }

    public byte getByte(int address) {
        return memory[address];
    }
}
