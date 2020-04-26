package cesar.hardware;

import cesar.utils.Shorts;

public class Cpu {
    enum CpuConditionalInstruction {
        BR, BNE, BEQ, BPL, BMI, BVC, BVS, BCC, BCS, BGE, BLT, BGT, BLE, BHI, BLS;

        private static final CpuConditionalInstruction[] array = CpuConditionalInstruction.values();

        public static CpuConditionalInstruction fromInt(final int index) {
            return array[index];
        }
    }

    enum CpuInstruction {
        NOP, CCC, SCC, CONDITIONAL_BRANCH, JMP, SOB, JSR, RTS, ONE_OPERAND_INSTRUCTION, MOV, ADD, SUB, CMP, AND, OR,
        HLT;

        private static final CpuInstruction[] array = CpuInstruction.values();

        public static CpuInstruction fromInt(final int index) {
            return array[index];
        }
    }

    enum CpuOneOperandInstruction {
        CLR, NOT, INC, DEC, NEG, TST, ROR, ROL, ASR, ASL, ADC, SBC;

        private static final CpuOneOperandInstruction[] array = CpuOneOperandInstruction.values();

        public static CpuOneOperandInstruction fromInt(final int index) {
            return array[index];
        }
    }

    public enum ExecutionResult {
        HALT, INVALID_INSTRUCTION, NOOP, OK, BREAK_POINT
    }

    public static final int REGISTER_COUNT = 8;
    public static final int MEMORY_SIZE = 1 << 16;
    public static final int KEYBOARD_STATE_ADDRESS = 65498;
    public static final int LAST_CHAR_ADDRESS = 65499;
    public static final int BEGIN_DISPLAY_ADDRESS = 65500;

    public static final int END_DISPLAY_ADDRESS = 65535;

    public static final int DATA_START_ADDRESS = 1024;
    public static final int PC = 7;
    public static final int SP = 6;
    private static final byte ZERO_BYTE = 0;

    public static boolean isIOAddress(final int address) {
        return address >= KEYBOARD_STATE_ADDRESS && address <= END_DISPLAY_ADDRESS;
    }

    private final ConditionRegister conditionRegister;
    private final short[] registers;
    private final byte[] memory;
    private final String[] mnemonics;
    private short breakPoint;
    private boolean memoryChanged;

    private int lastChangedAddress;

    private int lastChangedMnemonic;

    private int memoryAccessCount;

    private String readInstruction;

    private String readMnemonic;

    public Cpu() {
        registers = new short[REGISTER_COUNT];
        memory = new byte[MEMORY_SIZE];
        mnemonics = new String[MEMORY_SIZE];
        updateMnemonics();
        breakPoint = (short) 0xFFFF;
        conditionRegister = new ConditionRegister();
        memoryAccessCount = 0;
        memoryChanged = false;
        lastChangedAddress = 0;
        readInstruction = "0";
        readMnemonic = Instruction.NOP.toString();
    }

    private void executeConditionalInstruction(final CpuConditionalInstruction instruction, final byte offset) {
        switch (instruction) {
            case BR:
                registers[PC] = (short) (registers[PC] + offset);
                break;

            case BNE:
                if (!conditionRegister.isZero()) {
                    registers[PC] = (short) (registers[PC] + offset);
                }
                break;

            case BEQ:
                if (conditionRegister.isZero()) {
                    registers[PC] = (short) (registers[PC] + offset);
                }
                break;

            case BPL:
                if (!conditionRegister.isNegative()) {
                    registers[PC] = (short) (registers[PC] + offset);
                }
                break;

            case BMI:
                if (conditionRegister.isNegative()) {
                    registers[PC] = (short) (registers[PC] + offset);
                }
                break;

            case BVC:
                if (!conditionRegister.isOverflow()) {
                    registers[PC] = (short) (registers[PC] + offset);
                }
                break;

            case BVS:
                if (conditionRegister.isOverflow()) {
                    registers[PC] = (short) (registers[PC] + offset);
                }
                break;

            case BCC:
                if (!conditionRegister.isCarry()) {
                    registers[PC] = (short) (registers[PC] + offset);
                }
                break;

            case BCS:
                if (conditionRegister.isCarry()) {
                    registers[PC] = (short) (registers[PC] + offset);
                }
                break;

            case BGE:
                if (conditionRegister.isNegative() == conditionRegister.isOverflow()) {
                    registers[PC] = (short) (registers[PC] + offset);
                }
                break;

            case BLT:
                if (conditionRegister.isNegative() != conditionRegister.isOverflow()) {
                    registers[PC] = (short) (registers[PC] + offset);
                }
                break;

            case BGT:
                if (conditionRegister.isNegative() == conditionRegister.isOverflow() && !conditionRegister.isZero()) {
                    registers[PC] = (short) (registers[PC] + offset);
                }
                break;

            case BLE:
                if (conditionRegister.isNegative() != conditionRegister.isOverflow() || conditionRegister.isZero()) {
                    registers[PC] = (short) (registers[PC] + offset);
                }
                break;

            case BHI:
                if (!conditionRegister.isCarry() && !conditionRegister.isZero()) {
                    registers[PC] = (short) (registers[PC] + offset);
                }
                break;

            case BLS:
                if (conditionRegister.isCarry() || conditionRegister.isZero()) {
                    registers[PC] = (short) (registers[PC] + offset);
                }
                break;
        }
    }

    public ExecutionResult executeNextInstruction() {
        memoryChanged = false;

        if (registers[PC] == breakPoint) {
            return ExecutionResult.BREAK_POINT;
        }

        readInstruction = "";
        readMnemonic = getMnemonic(registers[PC]);

        final byte firstByte = fetchNextByte();
        final int firstBits = 0x0F & (firstByte & 0xF0) >> 4;
        final CpuInstruction instruction = CpuInstruction.fromInt(firstBits);

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
                final CpuConditionalInstruction conditionalInstruction = CpuConditionalInstruction
                        .fromInt(firstByte & 0x0F);
                final byte nextByte = fetchNextByte();
                executeConditionalInstruction(conditionalInstruction, nextByte);
                return ExecutionResult.OK;
            }

            case JMP: {
                final byte nextByte = fetchNextByte();
                final int mmm = (nextByte & 0b0011_1000) >> 3;
                final int rrr = nextByte & 0b0000_0111;
                final AddressMode mode = AddressMode.fromInt(mmm);
                if (mode == AddressMode.REGISTER) {
                    return ExecutionResult.NOOP;
                }
                registers[PC] = (short) getAddress(mode, rrr);
                return ExecutionResult.OK;
            }

            case SOB: {
                final byte offset = fetchNextByte();
                final int rrr = firstByte & 0b0000_0111;
                registers[rrr] = (short) (0xFFFF & registers[rrr] - 1);
                if (registers[rrr] != 0) {
                    registers[PC] = (short) (0xFFFF & registers[PC] - offset);
                }
                return ExecutionResult.OK;
            }

            case JSR: {
                final byte nextByte = fetchNextByte();
                final int reg = firstByte & 0b0000_0111;
                final int mmm = (nextByte & 0b0011_1000) >> 3;
                final int rrr = nextByte & 0b0000_0111;
                final AddressMode mode = AddressMode.fromInt(mmm);
                if (mode == AddressMode.REGISTER) {
                    return ExecutionResult.NOOP;
                }
                final int subAddress = getAddress(mode, rrr);
                push(registers[reg]);
                registers[reg] = registers[PC];
                registers[PC] = (short) subAddress;
                return ExecutionResult.OK;
            }

            case RTS: {
                final int rrr = firstByte & 0b0000_0111;
                registers[PC] = registers[rrr];
                registers[rrr] = pop();
                return ExecutionResult.OK;
            }

            case ONE_OPERAND_INSTRUCTION: {
                final int code = firstByte & 0x0F;
                final CpuOneOperandInstruction oneOperandInstruction = CpuOneOperandInstruction.fromInt(code);
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
                executeTwoOperandInstruction(instruction, word);
                return ExecutionResult.OK;
            }

            case HLT:
                return ExecutionResult.HALT;
        }

        return ExecutionResult.INVALID_INSTRUCTION;
    }

    private void executeOneOperandInstruction(final CpuOneOperandInstruction instruction, final byte nextByte) {
        final int mmm = (nextByte & 0b0011_1000) >> 3;
        final int rrr = nextByte & 0b0000_0111;
        final Operand operand = getOperand(mmm, rrr);
        final short value = operand.value;
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
                result = (short) (0xFFFF & value + 1);
                conditionRegister.testNegative(result);
                conditionRegister.testZero(result);
                conditionRegister.testCarry(value, result, ConditionRegister.CarryOperation.PLUS);
                conditionRegister.testOverflow(value, (short) (value + 1), result);
                break;

            case DEC:
                result = (short) (0xFFFF & value - 1);
                conditionRegister.testNegative(result);
                conditionRegister.testZero(result);
                conditionRegister.testCarry(value, result, ConditionRegister.CarryOperation.MINUS);
                conditionRegister.testOverflow(value, (short) (0xFFFF & value - 1), result);
                break;

            case NEG:
                result = (short) (0xFFFF & -value);
                conditionRegister.testNegative(result);
                conditionRegister.testZero(result);
                conditionRegister.testCarry(value, result, ConditionRegister.CarryOperation.MINUS);
                conditionRegister.testOverflow(value, (short) (0xFFFF & -value), result);
                break;

            case TST:
                // result = value
                conditionRegister.testNegative(result);
                conditionRegister.testZero(result);
                conditionRegister.setCarry(false);
                conditionRegister.setOverflow(false);
                break;

            case ROR: {
                final int lsb = (value & 0x0001) << 0xF;
                result = (short) (lsb | (0xFFFF & value) >> 1);
                conditionRegister.testNegative(result);
                conditionRegister.testZero(result);
                conditionRegister.setCarry(lsb == 0x8000);
                conditionRegister.setOverflow(conditionRegister.isNegative() ^ conditionRegister.isCarry());
                break;
            }

            case ROL: {
                final int msb = (value & 0x8000) >> 0xF;
                result = (short) ((0xFFFF & value) << 1 | msb);
                conditionRegister.testNegative(result);
                conditionRegister.testZero(result);
                conditionRegister.setCarry(msb == 1);
                conditionRegister.setOverflow(conditionRegister.isNegative() ^ conditionRegister.isCarry());
                break;
            }

            case ASR: {
                final int msb = value & 0x8000;
                final int lsb = value & 1;
                result = (short) (msb | 0x7FFF & value >> 1);
                conditionRegister.testNegative(result);
                conditionRegister.testZero(result);
                conditionRegister.setCarry(lsb == 1);
                conditionRegister.setOverflow(conditionRegister.isNegative() ^ conditionRegister.isCarry());
                break;
            }

            case ASL: {
                final int msb = value & 0x8000;
                result = (short) (value << 1 & 0xFFFE);
                conditionRegister.testNegative(result);
                conditionRegister.testZero(result);
                conditionRegister.setCarry(msb == 0x8000);
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

        if (instruction != CpuOneOperandInstruction.TST) {
            if (operand.addressMode == AddressMode.REGISTER) {
                registers[rrr] = result;
            }
            else {
                writeWord(operand.address, result);
                memoryChanged = true;
                lastChangedAddress = operand.address;
            }
        }
    }

    private void executeTwoOperandInstruction(final CpuInstruction instruction, final short word) {
        final int uWord = Shorts.toUnsignedInt(word);
        final int mmm1 = (uWord & 0b0000_1110_0000_0000) >> 9;
        final int rrr1 = (uWord & 0b0000_0001_1100_0000) >> 6;
        final int mmm2 = (uWord & 0b0000_0000_0011_1000) >> 3;
        final int rrr2 = uWord & 0b0000_0000_0000_0111;

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
                result = (short) (0xFFFF & dst + src);
                conditionRegister.testNegative(result);
                conditionRegister.testZero(result);
                conditionRegister.testOverflow(dst, src, result);
                conditionRegister.testCarry(dst, src, ConditionRegister.CarryOperation.PLUS);
                break;

            case SUB:
                result = (short) (0xFFFF & dst - src);
                conditionRegister.testNegative(result);
                conditionRegister.testZero(result);
                conditionRegister.testOverflow(dst, src, result);
                conditionRegister.testCarry(dst, src, ConditionRegister.CarryOperation.MINUS);
                break;

            case CMP:
                result = (short) (0xFFFF & src - dst);
                conditionRegister.testNegative(result);
                conditionRegister.testZero(result);
                conditionRegister.testOverflow(src, dst, result);
                conditionRegister.testCarry(src, dst, ConditionRegister.CarryOperation.MINUS);
                break;

            case AND:
                result = (short) (0xFFFF & dst & src);
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

            default:
                System.err.println("Instrução de 2 operandos inválida");
                System.exit(1);
                break;
        }

        if (dstOperand.addressMode == AddressMode.REGISTER) {
            registers[rrr2] = result;
        }
        else {
            writeWord(dstOperand.address, result);
            memoryChanged = true;
            lastChangedAddress = dstOperand.address;
        }
    }

    private byte fetchNextByte() {
        final byte nextByte = readByte(Shorts.toUnsignedInt(registers[PC]));
        readInstruction += String.format(" %d", 0xFF & nextByte);
        registers[PC]++;
        return nextByte;
    }

    private int getAddress(final AddressMode mode, final int registerNumber) {
        int address = Shorts.toUnsignedInt(registers[registerNumber]);

        switch (mode) {
            case REGISTER:
                break;

            case REGISTER_POST_INCREMENTED:
                address = Shorts.toUnsignedInt(registers[registerNumber]);
                registers[registerNumber] = (short) (registers[registerNumber] + 2);
                break;

            case REGISTER_PRE_DECREMENTED:
                registers[registerNumber] = (short) (registers[registerNumber] - 2);
                address = Shorts.toUnsignedInt(registers[registerNumber]);
                break;

            case INDEXED: {
                final short word = readWord(registers[PC]);
                registers[PC] = (short) (registers[PC] + 2);
                address = 0xFFFF & registers[registerNumber] + word;
                break;
            }

            case REGISTER_INDIRECT:
                address = Shorts.toUnsignedInt(registers[registerNumber]);
                break;

            case POST_INCREMENTED_INDIRECT: {
                final int firstAddress = Shorts.toUnsignedInt(registers[registerNumber]);
                registers[registerNumber] = (short) (registers[registerNumber] + 2);
                address = Shorts.toUnsignedInt(readWord(firstAddress));
                break;
            }

            case PRE_DECREMENTED_INDIRECT: {
                registers[registerNumber] = (short) (registers[registerNumber] - 2);
                final int firstAddress = Shorts.toUnsignedInt(registers[registerNumber]);
                address = Shorts.toUnsignedInt(readWord(firstAddress));
                break;
            }

            case INDEXED_INDIRECT: {
                final short nextWord = readWord(registers[PC]);
                registers[PC] = (short) (registers[PC] + 2);
                final int firstAddress = 0xFFFF & nextWord + registers[registerNumber];
                address = Shorts.toUnsignedInt(readWord(firstAddress));
                break;
            }
        }

        return address;
    }

    public byte getByte(final int address) {
        return memory[0xFFFF & address];
    }

    public int getLastChangedAddress() {
        return lastChangedAddress;
    }

    public int getLastChangedMnemonic() {
        return lastChangedMnemonic;
    }

    public byte[] getMemory() {
        return memory;
    }

    public int getMemoryAccessCount() {
        return memoryAccessCount;
    }

    public String getMnemonic(final int address) {
        return mnemonics[0xFFFF & address];
    }

    public String[] getMnemonics() {
        return mnemonics;
    }

    private Operand getOperand(final int mmm, final int rrr) {
        final AddressMode mode = AddressMode.fromInt(mmm);
        if (mode == AddressMode.REGISTER) {
            return new Operand(registers[rrr], rrr, AddressMode.REGISTER);
        }
        else {
            final int address = getAddress(mode, rrr);
            return new Operand(readWord(address), address, mode);
        }
    }

    public int getProgramCounter() {
        return Shorts.toUnsignedInt(registers[PC]);
    }

    public String getReadInstruction() {
        return readInstruction.trim();
    }

    public String getReadMnemonic() {
        return readMnemonic;
    }

    public short getRegister(final int registerNumber) {
        return registers[registerNumber];
    }

    public boolean hasMemoryChanged() {
        return memoryChanged;
    }

    public boolean isCarry() {
        return conditionRegister.isCarry();
    }

    public boolean isNegative() {
        return conditionRegister.isNegative();
    }

    public boolean isOverflow() {
        return conditionRegister.isOverflow();
    }

    public boolean isZero() {
        return conditionRegister.isZero();
    }

    private short pop() {
        final short word = readWord(registers[SP]);
        registers[SP] = (short) (registers[SP] + 2);
        return word;
    }

    private void push(final short word) {
        registers[SP] = (short) (registers[SP] - 2);
        writeWord(Shorts.toUnsignedInt(registers[SP]), word);
    }

    private byte readByte(final int address) {
        ++memoryAccessCount;
        return memory[0xFFFF & address];
    }

    private short readWord(final int address) {
        if (isIOAddress(address)) {
            final byte lsb = readByte(address);
            return Shorts.fromBytes(ZERO_BYTE, lsb);
        }
        else {
            final byte msb = readByte(address);
            final byte lsb = readByte(address + 1);
            return Shorts.fromBytes(msb, lsb);
        }
    }

    public void setBreakPoint(final int bp) {
        breakPoint = (short) (0xFFFF & bp);
    }

    public void setBreakPoint(final short bp) {
        breakPoint = bp;
    }

    public void setByte(final int address, final byte value) {
        memory[0xFFFF & address] = value;
        lastChangedMnemonic = Mnemonic.updateMnemonics(this, address);
    }

    public void setMemory(final byte[] bytes) {
        System.arraycopy(bytes, 4, memory, 0, Cpu.MEMORY_SIZE);
        Mnemonic.updateMnemonics(this, 0, true);
    }

    public void setMemory(final byte[] bytes, final int start, final int end, final int target) {
        final int offset = 4;
        for (int i = target, j = start; i < MEMORY_SIZE && j <= end; ++i, ++j) {
            memory[i] = bytes[j + offset];
        }
        Mnemonic.updateMnemonics(this, 0, true);
    }

    public void setMnemonic(final int address, final String value) {
        mnemonics[0xFFFF & address] = value;
    }

    public void setRegister(final int registerNumber, final short value) {
        registers[registerNumber] = value;
    }


    public void setTypedKey(final byte keyValue) {
        // O valor do último byte digitado só é alterado quando o endereço do estado do
        // teclado for 0 (ZERO).
        // (que indica que está esperando uma tecla).

        // if (readByte(KEYBOARD_STATE_ADDRESS) == ZERO_BYTE) {
        setByte(KEYBOARD_STATE_ADDRESS, (byte) 0x80);
        setByte(LAST_CHAR_ADDRESS, keyValue);
        // }
    }


    private void writeByte(final int address, final byte value) {
        ++memoryAccessCount;
        memory[0xFFFF & address] = value;
    }


    private void writeWord(final int address, final short word) {
        final byte[] bytes = Shorts.toBytes(word);
        if (isIOAddress(address)) {
            writeByte(address, bytes[1]);
        }
        else {
            writeByte(address, bytes[0]);
            writeByte(address + 1, bytes[1]);
        }
        lastChangedMnemonic = Mnemonic.updateMnemonics(this, address);
    }

    public void updateMnemonics() {
        Mnemonic.updateMnemonics(this, 0, true);
    }
}
