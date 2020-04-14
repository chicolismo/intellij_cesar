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

    private final ConditionRegister conditionRegister;
    private final short[] registers;
    private final byte[] memory;

    private boolean memoryChanged;
    private int lastChangedAddress;
    private int memoryAccessCount;

    public Cpu() {
        registers = new short[8];
        memory = new byte[MEMORY_SIZE];
        conditionRegister = new ConditionRegister();
        memoryAccessCount = 0;
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

    public int getMemoryAccessCount() {
        return memoryAccessCount;
    }

    public boolean hasMemoryChanged() {
        return memoryChanged;
    }

    public int getLastChangedAddress() {
        return lastChangedAddress;
    }

    public boolean isNegative() {
        return conditionRegister.isNegative();
    }

    public boolean isZero() {
        return conditionRegister.isZero();
    }

    public boolean isOverflow() {
        return conditionRegister.isOverflow();
    }

    public boolean isCarry() {
        return conditionRegister.isCarry();
    }

    public void setMemory(final byte[] bytes) {
        final int maxSize = Math.min(bytes.length, memory.length);
        final int offset = (bytes.length > memory.length) ? (bytes.length - memory.length) : 0;
        System.arraycopy(bytes, 0 + offset, memory, 0, maxSize);
    }

    public byte getByte(final int address) {
        return memory[0xFFFF & address];
    }

    private byte readByte(final int address) {
        ++memoryAccessCount;
        return memory[0xFFFF & address];
    }

    private void writeByte(final int address, final byte value) {
        ++memoryAccessCount;
        memory[0xFFFF & address] = value;
    }

    private byte fetchNextByte() {
        final byte nextByte = readByte(Shorts.toUnsignedInt(registers[PC]));
        registers[PC]++;
        return nextByte;
    }

    private short readWord(int address) {
        // TODO: Verificar o quê deve acontecer nos endereços dos periféricos
        if (isIOAddress(address)) {
            final byte lsb = readByte(address);
            return Shorts.fromBytes((byte) 0, lsb);
        } else {
            final byte msb = readByte(address);
            final byte lsb = readByte(address + 1);
            return Shorts.fromBytes(msb, lsb);
        }
    }

    private void writeWord(int address, short word) {
        // TODO: Verificar o quê deve acontecer nos endereços dos periféricos
        final byte[] bytes = Shorts.toBytes(word);
        if (isIOAddress(address)) {
            writeByte(address, bytes[1]);
        } else {
            writeByte(address, bytes[0]);
            writeByte(address + 1, bytes[1]);
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

    private boolean isIOAddress(int address) {
        return address >= KEYBOARD_STATE_ADDRESS && address <= END_DISPLAY_ADDRESS;
    }

    public ExecutionResult executeNextInstruction() {
        memoryChanged = false;

        final byte firstByte = fetchNextByte();
        final int firstBits = 0x0F & ((firstByte & 0xF0) >> 4);
        final Instruction instruction = Instruction.fromInt(firstBits);

        switch (instruction) {
            case NOP:
                return ExecutionResult.NOOP;

            case CCC: {
                conditionRegister.ccc(firstByte & 0x0F);
                return ExecutionResult.OK;
            }

            case SCC: {
                conditionRegister.scc(firstByte & 0x0F);
                return ExecutionResult.OK;
            }

            case CONDITIONAL_BRANCH: {
                final ConditionalInstruction conditionalInstruction = ConditionalInstruction.fromInt(firstByte & 0x0F);
                final byte nextByte = fetchNextByte();
                System.out.println(conditionalInstruction.toString());
                executeConditionalInstruction(conditionalInstruction, nextByte);
                return ExecutionResult.OK;
            }

            case JMP: {
                final byte nextByte = fetchNextByte();
                final int mmm = (nextByte & 0b0011_1000) >> 3;
                final int rrr = (nextByte & 0b0000_0111);
                final AddressMode mode = AddressMode.fromInt(mmm);
                if (mode != AddressMode.REGISTER) {
                    registers[PC] = (short) getAddress(mode, rrr);
                    return ExecutionResult.OK;
                } else {
                    return ExecutionResult.NOOP;
                }
            }

            case SOB: {
                final byte offset = fetchNextByte();
                final int rrr = (firstByte & 0b0000_0111);
                registers[rrr] = (short) (0xFFFF & (registers[rrr] - 1));
                if (registers[rrr] != 0) {
                    registers[PC] = (short) (0xFFFF & (registers[PC] - offset));
                }
                return ExecutionResult.OK;
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
                return ExecutionResult.OK;
            }

            case RTS: {
                final int rrr = (firstByte & 0b0000_0111);
                registers[PC] = registers[rrr];
                registers[rrr] = pop();
                return ExecutionResult.OK;
            }

            case ONE_OPERAND_INSTRUCTION: {
                final OneOperandInstruction oneOperandInstruction = OneOperandInstruction.fromInt(firstByte & 0x0F);
                final byte nextByte = fetchNextByte();
                System.out.println(oneOperandInstruction.toString());
                executeOneOperandInstruction(oneOperandInstruction, nextByte);
                return ExecutionResult.OK;
            }

            case MOV:
            case ADD:
            case SUB:
            case CMP:
            case AND:
            case OR:
                final byte nextByte = fetchNextByte();
                final short word = Shorts.fromBytes(firstByte, nextByte);
                executeTwoOperandInstruction(instruction, word);
                return ExecutionResult.OK;

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
        final Operand operand = getOperand(mmm, rrr);
        short value = operand.value;
        short result = value;

        switch (instruction) {
            case CLR:
                result = 0;
                conditionRegister.testNegative(result);
                conditionRegister.testZero(result);
                conditionRegister.setCarry(false);
                conditionRegister.setOverflow(false);
                break;

            case NOT:
                result = (short) (0xFFFF & ~value);
                conditionRegister.testNegative(result);
                conditionRegister.testZero(result);
                conditionRegister.setCarry(true);
                conditionRegister.setOverflow(false);
                break;

            case INC:
                result = (short) (0xFFFF & (value + 1));
                conditionRegister.testNegative(result);
                conditionRegister.testZero(result);
                conditionRegister.testCarry(value, result, ConditionRegister.CarryOperation.PLUS);
                conditionRegister.testOverflow(value, (short) (value + 1), result);
                break;

            case DEC:
                result = (short) (0xFFFF & (value - 1));
                conditionRegister.testNegative(result);
                conditionRegister.testZero(result);
                conditionRegister.testCarry(value, result, ConditionRegister.CarryOperation.MINUS);
                conditionRegister.testOverflow(value, (short) (0xFFFF & (value - 1)), result);
                break;

            case NEG:
                result = (short) (0xFFFF & -value);
                conditionRegister.testNegative(result);
                conditionRegister.testZero(result);
                conditionRegister.testCarry(value, result, ConditionRegister.CarryOperation.MINUS);
                conditionRegister.testOverflow(value, (short) (0xFFFF & -value), result);
                break;

            case TST:
                result = value;
                conditionRegister.testNegative(result);
                conditionRegister.testZero(result);
                conditionRegister.setCarry(false);
                conditionRegister.setOverflow(false);
                break;

            case ROR: {
                final int lsb = (value & 0x0001);
                final int c = conditionRegister.isCarry() ? 1 : 0;
                result = (short) (c & (value >> 1));
                conditionRegister.testNegative(result);
                conditionRegister.testZero(result);
                conditionRegister.setCarry(lsb == 1);
                conditionRegister.setOverflow(conditionRegister.isNegative() ^ conditionRegister.isCarry());
                break;
            }

            case ROL: {
                final int msb = (value & 0x8000) >> 0xF;
                final int c = conditionRegister.isCarry() ? 1 : 0;
                result = (short) ((value << 1) & c);
                conditionRegister.testNegative(result);
                conditionRegister.testZero(result);
                conditionRegister.setCarry(msb == 1);
                conditionRegister.setOverflow(conditionRegister.isNegative() ^ conditionRegister.isCarry());
                break;
            }

            case ASR: {
                final int msb = (value & 0x8000);
                final int lsb = (value & 1);
                result = (short) (msb & (value >> 1));
                conditionRegister.testNegative(result);
                conditionRegister.testZero(result);
                conditionRegister.setCarry(lsb == 1);
                conditionRegister.setOverflow(conditionRegister.isNegative() ^ conditionRegister.isCarry());
                break;
            }

            case ASL: {
                final int msb = (value & 0x8000) >> 0xF;
                result = (short) ((value << 1) & (0xFFFE));
                conditionRegister.testNegative(result);
                conditionRegister.testZero(result);
                conditionRegister.setCarry(msb == 1);
                conditionRegister.setOverflow(conditionRegister.isNegative() ^ conditionRegister.isCarry());
                break;
            }

            case ADC: {
                final int c = conditionRegister.isCarry() ? 1 : 0;
                result = (short) (value + c);
                conditionRegister.testNegative(result);
                conditionRegister.testZero(result);
                conditionRegister.testCarry(value, (short) c, ConditionRegister.CarryOperation.PLUS);
                conditionRegister.testOverflow(value, (short) c, result);
                break;
            }

            case SBC: {
                final int c = conditionRegister.isCarry() ? 1 : 0;
                result = (short) (value - c);
                conditionRegister.testNegative(result);
                conditionRegister.testZero(result);
                conditionRegister.testCarry(value, (short) c, ConditionRegister.CarryOperation.MINUS);
                conditionRegister.testOverflow(value, (short) c, result);
                break;
            }
        }

        if (instruction != OneOperandInstruction.TST) {
            if (operand.addressMode == AddressMode.REGISTER) {
                registers[rrr] = result;
            } else {
                writeWord(operand.address, result);
                memoryChanged = true;
                lastChangedAddress = operand.address;
            }
        }
    }

    private void executeTwoOperandInstruction(final Instruction instruction, final short word) {

        final int uWord = Shorts.toUnsignedInt(word);
        final int mmm1 = (uWord & 0b0000_1110_0000_0000) >> 9;
        final int rrr1 = (uWord & 0b0000_0001_1100_0000) >> 6;
        final int mmm2 = (uWord & 0b0000_0000_0011_1000) >> 3;
        final int rrr2 = (uWord & 0b0000_0000_0000_0111);

        final Operand srcOperand = getOperand(mmm1, rrr1);
        final short src = srcOperand.value;

        final Operand dstOperand = getOperand(mmm2, rrr2);
        final short dst = dstOperand.value;

        short result = dst;

        switch (instruction) {
            case MOV:
                result = src;
                conditionRegister.testNegative(result);
                conditionRegister.testZero(result);
                conditionRegister.setOverflow(false);
                break;

            case ADD:
                result = (short) (0xFFFF & (dst + src));
                conditionRegister.testNegative(result);
                conditionRegister.testZero(result);
                conditionRegister.testOverflow(dst, src, result);
                conditionRegister.testCarry(dst, src, ConditionRegister.CarryOperation.PLUS);
                break;

            case SUB:
                result = (short) (0xFFFF & (dst - src));
                conditionRegister.testNegative(result);
                conditionRegister.testZero(result);
                conditionRegister.testOverflow(dst, src, result);
                conditionRegister.testCarry(dst, src, ConditionRegister.CarryOperation.MINUS);
                break;

            case CMP:
                result = (short) (0xFFFF & (src - dst));
                conditionRegister.testNegative(result);
                conditionRegister.testZero(result);
                conditionRegister.testOverflow(src, dst, result);
                conditionRegister.testCarry(src, dst, ConditionRegister.CarryOperation.MINUS);
                break;

            case AND:
                result = (short) (0xFFFF & (dst & src));
                conditionRegister.testNegative(result);
                conditionRegister.testZero(result);
                conditionRegister.setOverflow(false);
                break;

            case OR:
                result = (short) (0xFFFF & (dst | src));
                conditionRegister.testNegative(result);
                conditionRegister.testZero(result);
                conditionRegister.setOverflow(false);
                break;
        }

        if (dstOperand.addressMode == AddressMode.REGISTER) {
            registers[rrr2] = result;
        } else {
            writeWord(dstOperand.address, result);
            memoryChanged = true;
            lastChangedAddress = dstOperand.address;
        }
    }

    private Operand getOperand(final int mmm, final int rrr) {
        final AddressMode mode = AddressMode.fromInt(mmm);
        int address = getAddress(mode, rrr);
        if (mode == AddressMode.REGISTER) {
            return new Operand(registers[rrr], address, mode);
        } else {
            return new Operand(readWord(address), address, mode);
        }
    }

    private int getAddress(final AddressMode mode, int registerNumber) {
        int address = Shorts.toUnsignedInt(registers[registerNumber]);

        switch (mode) {
            case REGISTER:
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

    private enum Instruction {
        NOP, CCC, SCC, CONDITIONAL_BRANCH, JMP, SOB, JSR, RTS, ONE_OPERAND_INSTRUCTION, MOV, ADD, SUB, CMP, AND, OR,
        HLT;

        private static Instruction[] array = Instruction.values();

        public static Instruction fromInt(final int index) {
            return array[index];
        }
    }

    private enum ConditionalInstruction {
        BR, BNE, BEQ, BPL, BMI, BVC, BVS, BCC, BCS, BGE, BLT, BGT, BLE, BHI, BLS;

        private static ConditionalInstruction[] array = ConditionalInstruction.values();

        public static ConditionalInstruction fromInt(final int index) {
            return array[index];
        }
    }

    private enum OneOperandInstruction {
        CLR, NOT, INC, DEC, NEG, TST, ROR, ROL, ASR, ASL, ADC, SBC;

        private static OneOperandInstruction[] array = OneOperandInstruction.values();

        public static OneOperandInstruction fromInt(int index) {
            return array[index];
        }
    }

    public enum ExecutionResult {
        HALT, INVALID_INSTRUCTION, NOOP, OK
    }

    private enum AddressMode {
        REGISTER, REGISTER_POST_INCREMENTED, REGISTER_PRE_DECREMENTED, INDEXED, REGISTER_INDIRECT,
        POST_INCREMENTED_INDIRECT, PRE_DECREMENTED_INDIRECT, INDEX_INDIRECT;

        private static AddressMode[] array = AddressMode.values();

        public static AddressMode fromInt(final int index) {
            return array[index];
        }
    }


    private static class ConditionRegister {
        enum CarryOperation {
            PLUS, MINUS
        }

        ;

        private boolean negative;
        private boolean zero;
        private boolean carry;
        private boolean overflow;
        private int value;

        public ConditionRegister() {
            setNegative(false);
            setZero(true);
            setCarry(false);
            overflow = false;

            value = 0b0100;
        }

        public void ccc(int newValue) {
            // TODO: Testar
            value &= ~(newValue);
            update();
        }

        public void scc(int newValue) {
            // TODO: Testar
            value |= newValue;
            update();
        }

        private void update() {
            setNegative((value & 8) == 8);
            setZero((value & 4) == 4);
            setCarry((value & 2) == 2);
            overflow = (value & 1) == 1;
        }

        public boolean isNegative() {
            return negative;
        }

        public void setNegative(boolean negative) {
            this.negative = negative;
            value |= 0b1000;
        }

        public boolean isZero() {
            return zero;
        }

        public void setZero(boolean zero) {
            this.zero = zero;
            value |= 0b0100;
        }

        public boolean isCarry() {
            return carry;
        }

        public void setCarry(boolean carry) {
            this.carry = carry;
            value |= 0b0010;
        }

        public boolean isOverflow() {
            return overflow;
        }

        public void setOverflow(boolean overflow) {
            this.overflow = overflow;
            value |= 0b0001;
        }

        public void testNegative(final short value) {
            setNegative(value < 0);
        }

        public void testZero(final short value) {
            setZero(value == 0);
        }

        public void testOverflow(final short op1, final short op2, final short result) {
            setOverflow(((op1 > 0) && (op2 > 0) && (result < 0)) || ((op1 < 0) && (op2 < 0) && (result > 0)));
        }

        public void testCarry(final short a, final short b, CarryOperation operation) {
            final int ua = Shorts.toUnsignedInt(a);
            final int ub = Shorts.toUnsignedInt(b);
            int result;
            if (operation == CarryOperation.PLUS) {
                result = ua + ub;
            } else {
                result = ua - ub;
            }
            setCarry((result & 0x1_0000) == 0x1_0000);
        }
    }

    class Operand {
        public short value;
        public int address;
        public AddressMode addressMode;

        public Operand(final short value, final int address, final AddressMode addressMode) {
            this.value = value;
            this.address = address;
            this.addressMode = addressMode;
        }
    }
}
