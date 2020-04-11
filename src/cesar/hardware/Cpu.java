package cesar.hardware;

import cesar.hardware.ConditionRegister.CarryOperation;
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
    private ConditionRegister conditionRegister;
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
        return memory[0xFFFF & address];
    }

    public byte readByte(final int address) {
        ++memoryAccesses;
        return memory[0xFFFF & address];
    }

    public byte fetchNextByte() {
        final byte nextByte = readByte(Shorts.toUnsignedInt(registers[PC]));
        registers[PC]++;
        return nextByte;
    }

    private void writeByte(final int address, final byte value) {
        ++memoryAccesses;
        memory[0xFFFF & address] = value;
    }

    private short readWord(int address) {
        final byte msb = readByte(address);
        final byte lsb = readByte(address + 1);
        return Shorts.fromBytes(msb, lsb);
    }

    private void writeWord(int address, short word) {
        final byte[] bytes = Shorts.toBytes(word);
        writeByte(address, bytes[0]);
        writeByte(address + 1, bytes[1]);
    }

    public short getWordFromAddress(int address, AddressMode mode) {
        if (mode == AddressMode.REGISTER) {
            return registers[address];
        }
        else {
            return readWord(address);
        }
    }

    public void setWordToAddress(short word, int address, AddressMode mode) {
        if (mode == AddressMode.REGISTER) {
            registers[address] = word;
        }
        else {
            writeWord(address, word);
        }
    }

    private void push(final short word) {
        registers[SP] -= 2;
        writeWord(Shorts.toUnsignedInt(registers[SP]), word);
    }

    private short pop() {
        final short word = readWord(registers[SP]);
        registers[SP] += 2;
        return word;
    }

    public ExecutionResult executeNextInstruction() {
        memoryChanged = false;

        final byte firstByte = fetchNextByte();
        final int cccc = ((firstByte & 0xF0) >> 4);
        final Instruction instruction = Instruction.fromInt(cccc);

        switch (instruction) {
            case NOP:
                return ExecutionResult.NOOP;

            case CCC: {
                conditionRegister.ccc(firstByte & 0x0F);
                break;
            }

            case SCC: {
                conditionRegister.scc(firstByte & 0x0F);
                break;
            }

            case CONDITIONAL_BRANCH: {
                final ConditionalInstruction conditionalInstruction = ConditionalInstruction.fromInt(firstByte & 0x0F);
                final byte nextByte = fetchNextByte();
                executeConditionalInstruction(conditionalInstruction, nextByte);
                return ExecutionResult.OK;
            }

            case JMP: {
                final byte nextByte = fetchNextByte();
                final int mmm = (nextByte & 0b0011_1000) >> 3;
                final int rrr = (nextByte & 0b0000_0111);
                AddressMode mode = AddressMode.fromInt(mmm);
                if (mode != AddressMode.REGISTER) {
                    registers[PC] = (short) getAddress(mode, rrr);
                }
                else {
                    return ExecutionResult.NOOP;
                }
                break;
            }

            case SOB: {
                final byte offset = fetchNextByte();
                final int rrr = (firstByte & 0b0000_0111);
                --registers[rrr];
                if (registers[rrr] != 0) {
                    registers[PC] -= offset;
                }
                break;
            }

            case JSR: {
                final byte nextByte = fetchNextByte();
                final int reg = (firstByte & 0b0000_0111);
                final int mmm = ((nextByte & 0b0011_1000) >> 3);
                final int rrr = (nextByte & 0b0000_0111);
                final AddressMode mode = AddressMode.fromInt(mmm);
                if (mode == AddressMode.REGISTER) {
                    return ExecutionResult.NOOP;
                }
                final int subAddress = getAddress(mode, rrr);
                push(registers[reg]);
                registers[reg] = registers[PC];
                registers[PC] = (short) (subAddress);
                break;
            }

            case RTS: {
                final int rrr = (firstByte & 0b0000_0111);
                registers[PC] = registers[rrr];
                registers[rrr] = pop();
                break;
            }

            case ONE_OPERAND_INSTRUCTION: {
                final OneOperandInstruction oneOperandInstruction = OneOperandInstruction.fromInt(firstByte & 0x0F);
                final byte nextByte = fetchNextByte();
                executeOneOperandInstruction(oneOperandInstruction, nextByte);
                return ExecutionResult.OK;
            }

            case MOV:
            case ADD:
            case SUB:
            case CMP:
            case AND:
            case OR: {
                final byte nextByte = fetchNextByte();
                final short word = Shorts.fromBytes(firstByte, nextByte);
                executeTwoOperandInstruction(TwoOperandInstruction.fromInt(cccc), word);
                return ExecutionResult.OK;
            }

            case HLT:
                return ExecutionResult.HALT;
        }
        return ExecutionResult.INVALID_INSTRUCTION;
    }

    private void executeConditionalInstruction(final ConditionalInstruction instruction, final byte offset) {
        switch (instruction) {
            case BR:
                registers[PC] += offset;
                break;
            case BNE:
                if (!conditionRegister.isZero()) {
                    registers[PC] += offset;
                }
                break;
            case BEQ:
                if (conditionRegister.isZero()) {
                    registers[PC] += offset;
                }
                break;
            case BPL:
                if (!conditionRegister.isNegative()) {
                    registers[PC] += offset;
                }
                break;
            case BMI:
                if (conditionRegister.isNegative()) {
                    registers[PC] += offset;
                }
                break;
            case BVC:
                if (!conditionRegister.isOverflow()) {
                    registers[PC] += offset;
                }
                break;
            case BVS:
                if (conditionRegister.isOverflow()) {
                    registers[PC] += offset;
                }
                break;
            case BCC:
                if (!conditionRegister.isCarry()) {
                    registers[PC] += offset;
                }
                break;
            case BCS:
                if (conditionRegister.isCarry()) {
                    registers[PC] += offset;
                }
                break;
            case BGE:
                if (conditionRegister.isNegative() == conditionRegister.isOverflow()) {
                    registers[PC] += offset;
                }
                break;
            case BLT:
                if (conditionRegister.isNegative() != conditionRegister.isOverflow()) {
                    registers[PC] += offset;
                }
                break;
            case BGT:
                if (conditionRegister.isNegative() == conditionRegister.isOverflow() && !conditionRegister.isZero()) {
                    registers[PC] += offset;
                }
                break;
            case BLE:
                if (conditionRegister.isNegative() != conditionRegister.isOverflow() || conditionRegister.isZero()) {
                    registers[PC] += offset;
                }
                break;
            case BHI:
                if (!conditionRegister.isCarry() && !conditionRegister.isZero()) {
                    registers[PC] += offset;
                }
                break;
            case BLS:
                if (conditionRegister.isCarry() || conditionRegister.isZero()) {
                    registers[PC] += offset;
                }
                break;
        }
    }

    private void executeOneOperandInstruction(final OneOperandInstruction instruction, final byte nextByte) {
        final int mmm = ((nextByte & 0b0011_1000) >> 3);
        final int rrr = (nextByte & 0b0000_0111);
        AddressMode mode = AddressMode.fromInt(mmm);
        final int address = getAddress(mode, rrr);
        final short operand = getWordFromAddress(address, mode);

        switch (instruction) {
            case CLR:
                break;
            case NOT:
                break;
            case INC:
                break;
            case DEC:
                break;
            case NEG:
                break;
            case TST:
                break;
            case ROR:
                break;
            case ROL:
                break;
            case ASR:
                break;
            case ASL:
                break;
            case ADC:
                break;
            case SBC:
                break;
        }
    }

    private void executeTwoOperandInstruction(final TwoOperandInstruction instruction, final short word) {
        final int mmm1 = (word & 0b0000_1110_0000_0000) >> 9;
        final int rrr1 = (word & 0b0000_0001_1100_0000) >> 6;
        final int mmm2 = (word & 0b0000_0000_0011_1000) >> 3;
        final int rrr2 = (word & 0b0000_0000_0000_0111);

        final AddressMode srcMode = AddressMode.fromInt(mmm1);
        final int srcAddress = getAddress(srcMode, rrr1);
        final short src = getWordFromAddress(srcAddress, srcMode);

        final AddressMode dstMode = AddressMode.fromInt(mmm2);
        final int dstAddress = getAddress(dstMode, rrr2);
        final short dst = getWordFromAddress(dstAddress, dstMode);

        short result = dst;
        switch (instruction) {
            case MOV:
                result = src;
                conditionRegister.testNegative(result);
                conditionRegister.testZero(result);
                conditionRegister.setOverflow(false);
                break;

            case ADD:
                result = (short) (dst + src);
                conditionRegister.testNegative(result);
                conditionRegister.testZero(result);
                conditionRegister.testOverflow(dst, src, result);
                conditionRegister.testCarry(dst, src, CarryOperation.PLUS);
                break;

            case SUB:
                result = (short) (dst - src);
                conditionRegister.testNegative(result);
                conditionRegister.testZero(result);
                conditionRegister.testOverflow(dst, src, result);
                conditionRegister.testCarry(dst, src, CarryOperation.MINUS);
                break;

            case CMP:
                result = (short) (src - dst);
                conditionRegister.testNegative(result);
                conditionRegister.testZero(result);
                conditionRegister.testOverflow(src, dst, result);
                conditionRegister.testCarry(src, dst, CarryOperation.MINUS);
                break;

            case AND:
                result = (short) (dst & src);
                conditionRegister.testNegative(result);
                conditionRegister.testZero(result);
                conditionRegister.setOverflow(false);
                break;

            case OR:
                result = (short) (dst | src);
                conditionRegister.testNegative(result);
                conditionRegister.testZero(result);
                conditionRegister.setOverflow(false);
                break;
        }

        if (instruction != TwoOperandInstruction.CMP) {
            writeWord(dstAddress, result);
        }
    }

    private int getAddress(final AddressMode mode, int registerNumber) {
        int address = 0;
        switch (mode) {
            case REGISTER:
                address = Shorts.toUnsignedInt(registers[registerNumber]);
                break;
            case REGISTER_POST_INCREMENTED:
                address = Shorts.toUnsignedInt(registers[registerNumber]);
                registers[registerNumber] += 2;
                break;
            case REGISTER_PRE_DECREMENTED:
                registers[registerNumber] -= 2;
                address = Shorts.toUnsignedInt(registers[registerNumber]);
                break;
            case INDEXED: {
                final short word = readWord(registers[PC]);
                registers[PC] += 2;
                address = 0xFFFF & (registers[registerNumber] + word);
                break;
            }
            case REGISTER_INDIRECT:
                address = Shorts.toUnsignedInt(registers[registerNumber]);
                break;
            case POST_INCREMENTED_INDIRECT: {
                final int firstAddress = Shorts.toUnsignedInt(registers[registerNumber]);
                registers[registerNumber] += 2;
                address = Shorts.toUnsignedInt(readWord(firstAddress));
                break;
            }
            case PRE_DECREMENTED_INDIRECT: {
                registers[registerNumber] -= 2;
                final int firstAddress = Shorts.toUnsignedInt(registers[registerNumber]);
                address = Shorts.toUnsignedInt(readWord(firstAddress));
                break;
            }
            case INDEX_INDIRECT: {
                final short nextWord = readWord(registers[PC]);
                registers[PC] += 2;
                final int firstAddress = 0xFFFF & (nextWord + registers[registerNumber]);
                address = Shorts.toUnsignedInt(readWord(firstAddress));
                break;
            }
        }
        return address;
    }

    enum Instruction {
        NOP, CCC, SCC, CONDITIONAL_BRANCH, JMP, SOB, JSR, RTS, ONE_OPERAND_INSTRUCTION, MOV, ADD, SUB, CMP, AND, OR,
        HLT;

        private static Instruction[] array = Instruction.values();

        public static Instruction fromInt(final int index) {
            return array[index];
        }
    }

    enum TwoOperandInstruction {
        MOV, ADD, SUB, CMP, AND, OR;
        private static TwoOperandInstruction[] array = TwoOperandInstruction.values();

        public static TwoOperandInstruction fromInt(final int index) {
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

    enum AddressMode {
        REGISTER, REGISTER_POST_INCREMENTED, REGISTER_PRE_DECREMENTED, INDEXED, REGISTER_INDIRECT,
        POST_INCREMENTED_INDIRECT, PRE_DECREMENTED_INDIRECT, INDEX_INDIRECT;

        private static AddressMode[] array = AddressMode.values();

        public static AddressMode fromInt(final int index) {
            return array[index];
        }
    }
}
