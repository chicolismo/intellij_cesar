package cesar.hardware;

import cesar.utils.Shorts;

public class Cpu {
    public static final int MEMORY_SIZE = 1 << 16;
    public static final int KEYBOARD_STATE_ADDRESS = 65498;
    public static final int LAST_CHAR_ADDRESS = 65499;
    public static final int BEGIN_DISPLAY_ADDRESS = 65500;
    public static final int END_DISPLAY_ADDRESS = 65535;
    private static final int PC = 7;
    private static final int SP = 6;
    private boolean memoryChanged;
    private int lastChangedAddress;
    private long memoryAccesses;
    private short[] registers;
    private byte[] memory;
    public Cpu() {
        registers = new short[8];
        memory = new byte[MEMORY_SIZE];
        memoryAccesses = 0L;
        memoryChanged = false;
        lastChangedAddress = 0;
    }

    public void setRegister(final int registerNumber, short value) {
        registers[registerNumber] = value;
    }

    public short getRegister(final int registerNumber) {
        return registers[registerNumber];
    }

    public byte[] getMemory() {
        return memory;
    }

    public boolean isMemoryChanged() {
        return memoryChanged;
    }

    public int getLastChangedAddress() {
        return lastChangedAddress;
    }

    public void setMemory(final byte[] bytes) {
        final int maxSize = Math.min(bytes.length, memory.length);
        final int offset = (bytes.length > memory.length) ? (bytes.length - memory.length) : 0;
        System.arraycopy(bytes, 0 + offset, memory, 0, maxSize);
    }

    public byte getByte(final int address) {
        return memory[address];
    }

    public byte readByte(final short address) {
        ++memoryAccesses;
        return memory[Shorts.toUnsignedInt(address)];
    }

    public byte readByte(final int address) {
        ++memoryAccesses;
        return memory[address];
    }

    public byte fetchNextByte() {
        final byte nextByte = readByte(registers[PC]);
        registers[PC]++;
        return nextByte;
    }

    public void writeByte(final short address, final byte value) {
        ++memoryAccesses;
        memory[Shorts.toUnsignedInt(address)] = value;
    }

    public void writeByte(final int address, final byte value) {
        ++memoryAccesses;
        memory[address] = value;
    }

    public ExecutionResult executeNextInstruction() {
        memoryChanged = false;

        final byte firstByte = fetchNextByte();
        final Instruction instruction = Instruction.fromInt((firstByte & 0xF0) >> 4);

        switch (instruction) {
            case NOP:
                return ExecutionResult.NOOP;

            case CCC:
                break;

            case SCC:
                break;

            case CONDITIONAL_BRANCH: {
                final int code = firstByte & 0x0F;
                final ConditionalInstruction conditionalInstruction = ConditionalInstruction.fromInt(code);
                executeConditionalInstruction(conditionalInstruction);
                return ExecutionResult.OK;
            }

            case JMP:
                break;

            case SOB:
                break;

            case JSR:
                break;

            case RTS:
                break;

            case ONE_OPERAND_INSTRUCTION: {
                final int code = 0x0F;
                final OneOperandInstruction oneOperandInstruction = OneOperandInstruction.fromInt(code);
                executeOneOperandInstruction(oneOperandInstruction);
                return ExecutionResult.OK;
            }

            case MOV:
            case ADD:
            case SUB:
            case CMP:
            case AND:
            case OR: {
                executeTwoOperandInstruction(instruction, firstByte);
                return ExecutionResult.OK;
            }

            case HLT:
                return ExecutionResult.HALT;
        }
        return ExecutionResult.INVALID_INSTRUCTION;
    }

    private void executeConditionalInstruction(final ConditionalInstruction instruction) {
    }

    private void executeOneOperandInstruction(final OneOperandInstruction instruction) {
    }

    private void executeTwoOperandInstruction(final Instruction instruction, final byte firstByte) {
    }

    enum Instruction {
        NOP, CCC, SCC, CONDITIONAL_BRANCH, JMP, SOB, JSR, RTS, ONE_OPERAND_INSTRUCTION, MOV, ADD, SUB, CMP, AND, OR, HLT;

        private static Instruction[] array = Instruction.values();

        public static Instruction fromInt(final int index) {
            return array[index];
        }
    }

    enum ConditionalInstruction {
        BR, BNE, BEQ, BPL, BMI, BVC, BVS, BCC, BCS, BGE, BLT, BGT, BLE, BHI, BLS;

        private static ConditionalInstruction[] array = ConditionalInstruction.values();

        public static ConditionalInstruction fromInt(final int index) {
            return array[index];
        }
    }

    enum OneOperandInstruction {
        CLR, NOT, INC, DEC, NEG, TST, ROR, ROL, ASR, ASL, ADC, SBC;

        private static OneOperandInstruction[] array = OneOperandInstruction.values();

        public static OneOperandInstruction fromInt(int index) {
            return array[index];
        }
    }

    enum ExecutionResult {
        HALT, INVALID_INSTRUCTION, NOOP, OK
    }
}
