package cesar.hardware;

import cesar.utils.Shorts;

public class Mnemonic {
    private static final int WORD_INCREMENT = 2;
    private static final String NEGATIVE = "N";
    private static final String ZERO = "Z";
    private static final String OVERFLOW = "V";
    private static final String CARRY = "C";
    private static final String EMPTY_STRING = "";

    public static int updateMnemonics(final Cpu cpu, final int startAt) {
        return updateMnemonics(cpu, startAt, false);
    }

    public static int updateMnemonics(final Cpu cpu, final int startAt, final boolean refreshAll) {
        int row = Math.max(0, startAt);

        /*
         * Só avança no (R7)+ ou nos casos de ddd(Rx) ou (ddd(Rx))
         */
        while (row < Cpu.MEMORY_SIZE) {
            // Termina quando chegar no final ou quando o mnemônico produzido para um
            // determinado índice for igual ao do arranjo de mnemônicos.
            final byte opCode = cpu.getByte(row);
            final String mnemonic;
            int increment = 1;
            final Instruction instruction = Instruction.getInstruction(opCode);
            final String format = instruction.getFormatString();

            switch (instruction) {
                case NOP:
                case HLT:
                    mnemonic = format;
                    break;

                case CCC:
                case SCC: {
                    mnemonic = String.format(format, conditionToString(opCode));
                    break;
                }

                case BR:
                case BNE:
                case BEQ:
                case BPL:
                case BMI:
                case BVC:
                case BVS:
                case BCC:
                case BCS:
                case BGE:
                case BLT:
                case BGT:
                case BLE:
                case BHI:
                case BLS: {
                    final byte nextByte = cpu.getByte(row + increment);
                    ++increment;
                    mnemonic = String.format(format, 0xFF & nextByte);
                    break;
                }

                case JMP: {
                    final byte nextByte = cpu.getByte(row + increment);
                    ++increment;
                    final int mmm = (nextByte & 0b00111000) >> 3;
                    final int rrr = nextByte & 0b00000111;
                    final AddressMode addressMode = AddressMode.fromInt(mmm);
                    if (addressMode.isIndexed()) {
                        final byte msb = cpu.getByte(row + increment);
                        final byte lsb = cpu.getByte(row + increment + 1);
                        increment += WORD_INCREMENT;
                        final int ddd = Shorts.toUnsignedInt(Shorts.fromBytes(msb, lsb));
                        mnemonic = String.format(format, addressMode.asString(ddd, rrr));
                    }
                    else {
                        mnemonic = String.format(format, addressMode.asString(rrr));
                    }
                    break;
                }
                case SOB: {
                    final int rrr = opCode & 0b0000_0111;
                    final int ddd = 0xFF & cpu.getByte(row + increment);
                    ++increment;
                    mnemonic = String.format(format, rrr, ddd);
                    break;
                }
                case JSR: {
                    final int register = opCode & 0b0000_0111;
                    final byte nextByte = cpu.getByte(row + increment);
                    ++increment;
                    final int mmm = (nextByte & 0b00111000) >> 3;
                    final int rrr = nextByte & 0b00000111;
                    final AddressMode addressMode = AddressMode.fromInt(mmm);
                    if (addressMode.isIndexed()) {
                        final byte msb = cpu.getByte(row + increment);
                        final byte lsb = cpu.getByte(row + increment + 1);
                        increment += WORD_INCREMENT;
                        final int ddd = Shorts.toUnsignedInt(Shorts.fromBytes(msb, lsb));
                        mnemonic = String.format(format, register, addressMode.asString(ddd, rrr));
                    }
                    else {
                        mnemonic = String.format(format, register, addressMode.asString(rrr));
                    }

                    if (addressMode.isPostIncremented() && rrr == Cpu.PC) {
                        increment += WORD_INCREMENT;
                    }
                    break;
                }

                case RTS: {
                    final int rrr = opCode & 0b0000_0111;
                    mnemonic = String.format(format, rrr);
                    break;
                }

                case CLR:
                case NOT:
                case INC:
                case DEC:
                case NEG:
                case TST:
                case ROR:
                case ROL:
                case ASR:
                case ASL:
                case ADC:
                case SBC: {
                    final byte nextByte = cpu.getByte(row + increment);
                    ++increment;
                    final int mmm = (nextByte & 0b00111000) >> 3;
                    final int rrr = nextByte & 0b00000111;
                    final AddressMode addressMode = AddressMode.fromInt(mmm);

                    if (addressMode.isIndexed()) {
                        final byte msb = cpu.getByte(row + increment);
                        final byte lsb = cpu.getByte(row + increment + 1);
                        increment += WORD_INCREMENT;
                        final int ddd = Shorts.toUnsignedInt(Shorts.fromBytes(msb, lsb));
                        mnemonic = String.format(format, addressMode.asString(ddd, rrr));
                    }
                    else {
                        mnemonic = String.format(format, addressMode.asString(rrr));
                    }

                    if (addressMode.isPostIncremented() && rrr == Cpu.PC) {
                        increment += WORD_INCREMENT;
                    }

                    break;
                }

                case MOV:
                case ADD:
                case SUB:
                case CMP:
                case AND:
                case OR: {
                    final byte nextByte = cpu.getByte(row + increment);
                    ++increment;
                    final int word = Shorts.toUnsignedInt(Shorts.fromBytes(opCode, nextByte));
                    final int mmm1 = (word & 0b0000_1110_0000_0000) >> 9;
                    final int rrr1 = (word & 0b0000_0001_1100_0000) >> 6;
                    final int mmm2 = (word & 0b0000_0000_0011_1000) >> 3;
                    final int rrr2 = word & 0b0000_0000_0000_0111;

                    final AddressMode srcMode = AddressMode.fromInt(mmm1);

                    final String srcString;
                    if (srcMode.isIndexed()) {
                        final byte msb = cpu.getByte(row + increment);
                        final byte lsb = cpu.getByte(row + increment + 1);
                        increment += WORD_INCREMENT;
                        final int ddd = Shorts.toUnsignedInt(Shorts.fromBytes(msb, lsb));
                        srcString = srcMode.asString(ddd, rrr1);
                    }
                    else {
                        srcString = srcMode.asString(rrr1);
                    }

                    if (srcMode.isPostIncremented() && rrr1 == Cpu.PC) {
                        increment += WORD_INCREMENT;
                    }

                    final AddressMode dstMode = AddressMode.fromInt(mmm2);
                    final String dstString;
                    if (dstMode.isIndexed()) {
                        final byte msb = cpu.getByte(row + increment);
                        final byte lsb = cpu.getByte(row + increment + 1);
                        increment += WORD_INCREMENT;
                        final int ddd = Shorts.toUnsignedInt(Shorts.fromBytes(msb, lsb));
                        dstString = dstMode.asString(ddd, rrr2);
                    }
                    else {
                        dstString = dstMode.asString(rrr2);
                    }

                    if (dstMode.isPostIncremented() && rrr2 == Cpu.PC) {
                        increment += WORD_INCREMENT;
                    }

                    mnemonic = String.format(format, srcString, dstString);
                    break;
                }

                default:
                    mnemonic = Instruction.NOP.getFormatString();
            }

            if (refreshAll) {
                cpu.setMnemonic(row, mnemonic);
                for (int j = 1; j < increment; ++j) {
                    cpu.setMnemonic(row + j, EMPTY_STRING);
                }
            }
            else {
                if (cpu.getMnemonic(row) == null || !cpu.getMnemonic(row).equals(mnemonic)) {
                    cpu.setMnemonic(row, mnemonic);
                    for (int j = 1; j < increment; ++j) {
                        cpu.setMnemonic(row + j, EMPTY_STRING);
                    }
                }
                else {
                    break;
                }
            }
            row += increment;
        }
        return row;
    }

    private static String conditionToString(final byte opCode) {
        final int lsb = opCode & 0x0F;
        final String n = (lsb & 0b1000) > 0 ? NEGATIVE : EMPTY_STRING;
        final String z = (lsb & 0b0100) > 0 ? ZERO : EMPTY_STRING;
        final String v = (lsb & 0b0010) > 0 ? OVERFLOW : EMPTY_STRING;
        final String c = (lsb & 0b0001) > 0 ? CARRY : EMPTY_STRING;
        return n + z + v + c;
    }
}
