package cesar.models;

import static cesar.utils.Integers.clamp;
import static cesar.utils.Integers.clampToShort;

import java.util.Arrays;
import java.util.EnumMap;

import cesar.utils.Bytes;
import cesar.utils.Integers;
import cesar.utils.Shorts;

public class Cpu {
    public static final int REGISTER_COUNT = 8;
    public static final int MEMORY_SIZE = 1 << 16; // 2^16
    public static final int FIRST_ADDRESS = 0;
    public static final int LAST_ADDRESS = MEMORY_SIZE - 1;
    public static final int KEYBOARD_STATE_ADDRESS = 65498;
    public static final int KEYBOARD_INPUT_ADDRESS = 65499;
    public static final int BEGIN_DISPLAY_ADDRESS = 65500;
    public static final int END_DISPLAY_ADDRESS = 65535;
    public static final int DATA_START_ADDRESS = 1024;
    public static final int PC = 7;
    public static final int SP = 6;
    public static final byte ZERO_BYTE = 0;
    private final ConditionRegister conditionRegister;
    private final short[] registers;
    private final byte[] memory;
    private final byte[] originalMemory;
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
        originalMemory = new byte[MEMORY_SIZE];
        mnemonics = new String[MEMORY_SIZE];
        breakPoint = (short) 0xFFFF;
        conditionRegister = new ConditionRegister();
        memoryAccessCount = 0;
        memoryChanged = false;
        lastChangedAddress = 0;
        readInstruction = "0";
        readMnemonic = InstructionString.NOP.toString();
        updateMnemonics();
    }

    public static boolean isIOAddress(final int address) {
        return Integers.isInInterval(address, KEYBOARD_STATE_ADDRESS, END_DISPLAY_ADDRESS);
    }

    public static boolean isValidAddress(final int address) {
        return Integers.isInInterval(address, FIRST_ADDRESS, LAST_ADDRESS);
    }

    public ConditionRegister getConditionRegister() {
        return conditionRegister;
    }

    private void executeConditionalInstruction(final BranchInstruction instruction, final byte offset) {
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

    private void executeOneOperandInstruction(final OneOperandInstruction instruction, final byte nextByte) {
        final int mmm = (nextByte & 0b0011_1000) >> 3;
        final int rrr = nextByte & 0b0000_0111;
        final Operand operand = getOperand(mmm, rrr);
        final short value = operand.value;
        short result = value;
        final int carryBit = 0x10000;

        switch (instruction) {
            case CLR:
                result = 0;
                conditionRegister.testNegative(result);
                conditionRegister.testZero(result);
                conditionRegister.setOverflow(false);
                conditionRegister.setCarry(false);
                break;

            case NOT:
                result = clampToShort(~value);
                conditionRegister.testNegative(result);
                conditionRegister.testZero(result);
                conditionRegister.setOverflow(false);
                conditionRegister.setCarry(true);
                break;

            case INC:
                result = clampToShort(value + 1);
                conditionRegister.testNegative(result);
                conditionRegister.testZero(result);
                conditionRegister.testOverflow(value, (short) 1, result);
                conditionRegister.testCarry(value, (short) 1, ConditionRegister.CarryOperation.PLUS);
                break;

            case DEC:
                result = clampToShort(value - 1);
                conditionRegister.testNegative(result);
                conditionRegister.testZero(result);
                conditionRegister.testOverflow(value, (short) -1, result);
                conditionRegister.setCarry(((0xFFFF & value) - 1 & carryBit) != carryBit);
                break;

            case NEG:
                result = clampToShort(-value);
                conditionRegister.testNegative(result);
                conditionRegister.testZero(result);
                conditionRegister.setOverflow(MEMORY_SIZE / 2 == (0xFFFF & value));
                conditionRegister.setCarry(((0xFFFF & ~value) + 1 & carryBit) != carryBit);
                break;

            case TST:
                // result = value
                conditionRegister.testNegative(result);
                conditionRegister.testZero(result);
                conditionRegister.setOverflow(false);
                conditionRegister.setCarry(false);
                break;

            case ROR: {
                final int lsb = (value & 0x0001) << 0xF;
                result = (short) (lsb | clamp(value) >> 1);
                conditionRegister.testNegative(result);
                conditionRegister.testZero(result);
                conditionRegister.setOverflow(conditionRegister.isNegative() ^ conditionRegister.isCarry());
                conditionRegister.setCarry(lsb == 0x8000);
                break;
            }

            case ROL: {
                final int msb = (value & 0x8000) >> 0xF;
                result = (short) (clamp(value) << 1 | msb);
                conditionRegister.testNegative(result);
                conditionRegister.testZero(result);
                conditionRegister.setOverflow(conditionRegister.isNegative() ^ conditionRegister.isCarry());
                conditionRegister.setCarry(msb == 1);
                break;
            }

            case ASR: {
                final int msb = value & 0x8000;
                final int lsb = value & 1;
                result = (short) (msb | 0x7FFF & value >> 1);
                conditionRegister.testNegative(result);
                conditionRegister.testZero(result);
                conditionRegister.setOverflow(conditionRegister.isNegative() ^ conditionRegister.isCarry());
                conditionRegister.setCarry(lsb == 1);
                break;
            }

            case ASL: {
                final int msb = value & 0x8000;
                result = (short) (value << 1 & 0xFFFE);
                conditionRegister.testNegative(result);
                conditionRegister.testZero(result);
                conditionRegister.setOverflow(conditionRegister.isNegative() ^ conditionRegister.isCarry());
                conditionRegister.setCarry(msb == 0x8000);
                break;
            }

            case ADC: {
                final int c = conditionRegister.isCarry() ? 1 : 0;
                result = (short) (value + c);
                conditionRegister.testNegative(result);
                conditionRegister.testZero(result);
                conditionRegister.testOverflow(value, (short) c, result);
                conditionRegister.testCarry(value, (short) c, ConditionRegister.CarryOperation.PLUS);
                break;
            }

            case SBC: {
                final int c = conditionRegister.isCarry() ? 1 : 0;
                result = (short) (value - c);
                conditionRegister.testNegative(result);
                conditionRegister.testZero(result);
                conditionRegister.testOverflow(value, (short) c, result);
                conditionRegister.testCarry(value, (short) c, ConditionRegister.CarryOperation.MINUS);
                break;
            }
        }

        if (instruction != OneOperandInstruction.TST) {
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

    private void executeTwoOperandInstruction(final Instruction instruction, final short word) {
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
                result = clampToShort(dst + src);
                conditionRegister.testNegative(result);
                conditionRegister.testZero(result);
                conditionRegister.testOverflow(dst, src, result);
                conditionRegister.testCarry(dst, src, ConditionRegister.CarryOperation.PLUS);
                break;

            case SUB:
                result = clampToShort(dst - src);
                conditionRegister.testNegative(result);
                conditionRegister.testZero(result);
                conditionRegister.testOverflow(dst, src, result);
                conditionRegister.testCarry(dst, src, ConditionRegister.CarryOperation.MINUS);
                break;

            case CMP:
                result = clampToShort(src - dst);
                conditionRegister.testNegative(result);
                conditionRegister.testZero(result);
                conditionRegister.testOverflow(src, dst, result);
                conditionRegister.testCarry(src, dst, ConditionRegister.CarryOperation.MINUS);
                result = dst;
                break;

            case AND:
                result = clampToShort(dst & src);
                conditionRegister.testNegative(result);
                conditionRegister.testZero(result);
                conditionRegister.setOverflow(false);
                break;

            case OR:
                result = clampToShort(dst | src);
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

    private byte fetchNextByte() throws EndOfMemoryException {
        final byte nextByte = readByte(Shorts.toUnsignedInt(registers[PC]));
        // TODO: Verificar se readInstruction pode ser hexadecimal
        readInstruction += String.format(" %d", Bytes.toUnsignedInt(nextByte));

        if (registers[PC] == (short) 0xFFFF) {
            throw new EndOfMemoryException();
        }
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
                address = clamp(registers[registerNumber] + word);
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
                final int firstAddress = clamp(nextWord + registers[registerNumber]);
                address = Shorts.toUnsignedInt(readWord(firstAddress));
                break;
            }
        }

        return address;
    }

    private Operand getOperand(final int mmm, final int rrr) {
        final Operand operand;
        final AddressMode mode = AddressMode.fromInt(mmm);
        if (mode == AddressMode.REGISTER) {
            operand = new Operand(registers[rrr], rrr, AddressMode.REGISTER);
        }
        else {
            final int address = getAddress(mode, rrr);
            operand = new Operand(readWord(address), address, mode);
        }
        return operand;
    }

    private byte readByte(final int address) {
        ++memoryAccessCount;
        return memory[clamp(address)];
    }

    private short readWord(final int address) {
        final short word;
        if (isIOAddress(address)) {
            word = Shorts.fromBytes(ZERO_BYTE, readByte(address));
        }
        else {
            word = Shorts.fromBytes(readByte(address), readByte(address + 1));
        }
        return word;
    }

    private short stackPop() {
        final short word = readWord(registers[SP]);
        registers[SP] = (short) (registers[SP] + 2);
        return word;
    }

    private void stackPush(final short word) {
        registers[SP] = (short) (registers[SP] - 2);
        writeWord(Shorts.toUnsignedInt(registers[SP]), word);
    }

    private void writeByte(final int address, final byte value) {
        ++memoryAccessCount;
        memory[clamp(address)] = value;
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

    public void copyMemory(final int startAddress, final int endAddress, final int dstAddress) {
        for (int i = startAddress, j = dstAddress; i <= endAddress && j <= Cpu.LAST_ADDRESS; ++i, ++j) {
            setByte(j, getByte(i));
        }
        updateMnemonics();
    }

    public ExecutionResult executeNextInstruction() {
        memoryChanged = false;

        if (registers[PC] == breakPoint) {
            return ExecutionResult.BREAK_POINT;
        }

        try {
            readInstruction = "";
            readMnemonic = getMnemonic(registers[PC]);
            final byte firstByte = fetchNextByte();
            final int firstBits = 0x0F & (firstByte & 0xF0) >> 4;
            final Instruction instruction = Instruction.fromInt(firstBits);
            switch (instruction) {
                case NOP:
                    return ExecutionResult.NOOP;

                case CCC:
                    conditionRegister.ccc(firstByte & 0x0F);
                    return ExecutionResult.OK;

                case SCC:
                    conditionRegister.scc(firstByte & 0x0F);
                    return ExecutionResult.OK;

                case CONDITIONAL_BRANCH: {
                    final BranchInstruction branchInstruction = BranchInstruction.fromInt(firstByte & 0x0F);
                    final byte nextByte = fetchNextByte();
                    executeConditionalInstruction(branchInstruction, nextByte);
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
                    registers[rrr] = clampToShort(registers[rrr] - 1);
                    if (registers[rrr] != 0) {
                        registers[PC] = clampToShort(registers[PC] - offset);
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
                    final short subRoutineAddress = clampToShort(getAddress(mode, rrr));
                    stackPush(registers[reg]);
                    if (reg != PC) {
                        registers[reg] = registers[PC];
                    }
                    registers[PC] = subRoutineAddress;
                    return ExecutionResult.OK;
                }

                case RETURN_INSTRUCTION: {
                    // noinspection StatementWithEmptyBody
                    if (firstByte == (byte) 0b0111_1000) {
                        // TODO: TRATAR RTI
                    }
                    else {
                        // RTS
                        final int rrr = firstByte & 0b0000_0111;
                        if (rrr != PC) {
                            registers[PC] = registers[rrr];
                        }
                        registers[rrr] = stackPop();
                    }
                    return ExecutionResult.OK;
                }

                case ONE_OPERAND_INSTRUCTION: {
                    final int code = firstByte & 0x0F;
                    final OneOperandInstruction oneOperandInstruction = OneOperandInstruction.fromInt(code);
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
        }
        catch (EndOfMemoryException ignored) {
            return ExecutionResult.END_OF_MEMORY;
        }

        return ExecutionResult.NOOP;
    }

    public byte getByte(final int address) {
        return memory[clamp(address)];
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
        return mnemonics[clamp(address)];
    }

    public String[] getMnemonics() {
        return mnemonics;
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

    public short getRegisterValue(final int registerNumber) {
        return registers[registerNumber];
    }

    public boolean hasMemoryChanged() {
        return memoryChanged;
    }

    public boolean hasOriginalMemoryChanged() {
        return !Arrays.equals(memory, originalMemory);
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

    public short getBreakPoint() {
        return breakPoint;
    }

    public void setBreakPoint(final int bp) {
        breakPoint = clampToShort(bp);
    }

    public void setBreakPoint(final short bp) {
        breakPoint = bp;
    }

    public void setByte(final int address, final byte value) {
        memory[clamp(address)] = value;
        memoryChanged = true;
        lastChangedMnemonic = Mnemonic.updateMnemonics(this, address);
    }

    public void setMemory(final byte[] bytes) {
        assert bytes.length == MEMORY_SIZE;
        System.arraycopy(bytes, 0, memory, 0, MEMORY_SIZE);
        System.arraycopy(bytes, 0, originalMemory, 0, MEMORY_SIZE);
        Mnemonic.updateMnemonics(this, 0, true);
    }

    public void setMemory(final byte[] bytes, final int start, final int end, final int target) {
        assert end <= MEMORY_SIZE;
        for (int i = target, j = start; i < MEMORY_SIZE && j <= end; ++i, ++j) {
            memory[i] = bytes[j];
            originalMemory[i] = bytes[j];
        }
        Mnemonic.updateMnemonics(this, 0, true);
    }

    public void setMnemonic(final int address, final String value) {
        mnemonics[clamp(address)] = value;
    }

    public void setRegisterValue(final int registerNumber, final short value) {
        registers[registerNumber] = value;
    }

    public void setTypedKey(final byte keyValue) {
        // O valor do último byte digitado só é alterado quando o endereço do estado do
        // teclado for 0 (ZERO).
        // (que indica que está esperando uma tecla).
        if (readByte(KEYBOARD_STATE_ADDRESS) == ZERO_BYTE) {
            setByte(KEYBOARD_STATE_ADDRESS, (byte) 0x80);
            setByte(KEYBOARD_INPUT_ADDRESS, keyValue);
            memoryChanged = true;
            lastChangedAddress = KEYBOARD_INPUT_ADDRESS;
        }
    }

    public void updateMnemonics() {
        Mnemonic.updateMnemonics(this, 0, true);
    }

    public void zeroMemory(final int startAddress, final int endAddress) {
        Arrays.fill(memory, startAddress, endAddress + 1, ZERO_BYTE);
        updateMnemonics();
    }

    private static class Operand {
        public final short value;
        public final int address;
        public final AddressMode addressMode;

        public Operand(final short value, final int address, final AddressMode addressMode) {
            this.value = value;
            this.address = address;
            this.addressMode = addressMode;
        }
    }


    enum BranchInstruction {
        BR, BNE, BEQ, BPL, BMI, BVC, BVS, BCC, BCS, BGE, BLT, BGT, BLE, BHI, BLS;

        private static final BranchInstruction[] array = BranchInstruction.values();

        public static BranchInstruction fromInt(final int index) {
            return array[index];
        }
    }


    enum Instruction {
        NOP, CCC, SCC, CONDITIONAL_BRANCH, JMP, SOB, JSR, RETURN_INSTRUCTION, ONE_OPERAND_INSTRUCTION, MOV, ADD, SUB, CMP, AND, OR, HLT;

        private static final Instruction[] array = Instruction.values();

        public static Instruction fromInt(final int index) {
            return array[index];
        }
    }


    enum OneOperandInstruction {
        CLR, NOT, INC, DEC, NEG, TST, ROR, ROL, ASR, ASL, ADC, SBC;

        private static final OneOperandInstruction[] array = OneOperandInstruction.values();

        public static OneOperandInstruction fromInt(final int index) {
            return array[index];
        }
    }

    // enum CpuReturnInstruction {
    // RTS, RTI
    // }


    public enum ExecutionResult {
        HALT, NOOP, OK, BREAK_POINT, END_OF_MEMORY;

        private static final EnumMap<ExecutionResult, String> TO_STRING = new EnumMap<>(ExecutionResult.class);

        static {
            TO_STRING.put(HALT, "Halted");
            TO_STRING.put(NOOP, "No-op");
            TO_STRING.put(OK, " ");
            TO_STRING.put(BREAK_POINT, "Breakpoint");
            TO_STRING.put(END_OF_MEMORY, "End of memory");
        }

        @Override
        public String toString() {
            return TO_STRING.get(this);
        }
    }

    private static class EndOfMemoryException extends Exception {
    }
}
