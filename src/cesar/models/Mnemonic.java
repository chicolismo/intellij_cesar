package cesar.models;

import cesar.utils.Shorts;

public class Mnemonic {
    private static final int WORD_INCREMENT = 2;
    private static final String NEGATIVE = "N";
    private static final String ZERO = "Z";
    private static final String OVERFLOW = "V";
    private static final String CARRY = "C";
    private static final String EMPTY_STRING = "";

    private static String conditionToString(final byte opCode) {
        final var lsb = opCode & 0x0F;
        final var n = (lsb & 0b1000) > 0 ? NEGATIVE : EMPTY_STRING;
        final var z = (lsb & 0b0100) > 0 ? ZERO : EMPTY_STRING;
        final var v = (lsb & 0b0010) > 0 ? OVERFLOW : EMPTY_STRING;
        final var c = (lsb & 0b0001) > 0 ? CARRY : EMPTY_STRING;
        return n + z + v + c;
    }

    public static int updateMnemonics(final Cpu cpu, final int startAt) {
        return updateMnemonics(cpu, startAt, false);
    }

    public static int updateMnemonics(final Cpu cpu, final int startAt, final boolean forceAllRows) {
        var currentRow = Math.max(0, startAt);

        // Só avança no (r7)+ ou nos casos de ddd(rx) ou (ddd(rx))
        while (currentRow < Cpu.MEMORY_SIZE) {
            // Termina quando chegar no final ou quando o mnemônico produzido para um
            // determinado índice for igual ao do arranjo de mnemônicos.
            final var opCode = cpu.getByte(currentRow);
            final String mnemonic;
            var rowIncrement = 1;
            final var instruction = InstructionString.getInstruction(opCode);
            final var format = instruction.getFormatString();

            switch (instruction) {
            case NOP:
            case HLT:
            case RTI:
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
                final var nextByte = cpu.getByte(currentRow + rowIncrement);
                ++rowIncrement;
                mnemonic = String.format(format, 0xFF & nextByte);
                break;
            }

            case JMP: {
                final var nextByte = cpu.getByte(currentRow + rowIncrement);
                ++rowIncrement;
                final var mmm = (nextByte & 0b0011_1000) >> 3;
                final var rrr = nextByte & 0b0000_0111;
                final var addressMode = AddressMode.fromInt(mmm);
                if (addressMode.isIndexed()) {
                    final var msb = cpu.getByte(currentRow + rowIncrement);
                    final var lsb = cpu.getByte(currentRow + rowIncrement + 1);
                    rowIncrement += WORD_INCREMENT;
                    final var ddd = Shorts.toUnsignedInt(Shorts.fromBytes(msb, lsb));
                    mnemonic = String.format(format, addressMode.asString(ddd, rrr));
                }
                else if (addressMode.isPostIncremented()) {
                    mnemonic = String.format(format, addressMode.asString(rrr));
                    rowIncrement += WORD_INCREMENT;
                }
                else {
                    mnemonic = String.format(format, addressMode.asString(rrr));
                }
                break;
            }
            case SOB: {
                final var rrr = opCode & 0b0000_0111;
                final var ddd = 0xFF & cpu.getByte(currentRow + rowIncrement);
                ++rowIncrement;
                mnemonic = String.format(format, rrr, ddd);
                break;
            }
            case JSR: {
                final var register = opCode & 0b0000_0111;
                final var nextByte = cpu.getByte(currentRow + rowIncrement);
                ++rowIncrement;
                final var mmm = (nextByte & 0b00111000) >> 3;
                final var rrr = nextByte & 0b00000111;
                final var addressMode = AddressMode.fromInt(mmm);
                if (addressMode.isIndexed()) {
                    final var msb = cpu.getByte(currentRow + rowIncrement);
                    final var lsb = cpu.getByte(currentRow + rowIncrement + 1);
                    rowIncrement += WORD_INCREMENT;
                    final var ddd = Shorts.toUnsignedInt(Shorts.fromBytes(msb, lsb));
                    mnemonic = String.format(format, register, addressMode.asString(ddd, rrr));
                }
                else if (addressMode.isPostIncremented()) {
                    mnemonic = String.format(format, register, addressMode.asString(rrr));
                    rowIncrement += WORD_INCREMENT;
                }
                else {
                    mnemonic = String.format(format, register, addressMode.asString(rrr));
                }

                if (addressMode.isPostIncremented() && rrr == Cpu.PC) {
                    rowIncrement += WORD_INCREMENT;
                }
                break;
            }

            case RTS: {
                final var rrr = opCode & 0b0000_0111;
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
                final var nextByte = cpu.getByte(currentRow + rowIncrement);
                ++rowIncrement;
                final var mmm = (nextByte & 0b00111000) >> 3;
                final var rrr = nextByte & 0b00000111;
                final var addressMode = AddressMode.fromInt(mmm);

                if (addressMode.isIndexed()) {
                    final var msb = cpu.getByte(currentRow + rowIncrement);
                    final var lsb = cpu.getByte(currentRow + rowIncrement + 1);
                    rowIncrement += WORD_INCREMENT;
                    final var ddd = Shorts.toUnsignedInt(Shorts.fromBytes(msb, lsb));
                    mnemonic = String.format(format, addressMode.asString(ddd, rrr));
                }
                else if (addressMode.isPostIncremented()) {
                    mnemonic = String.format(format, addressMode.asString(rrr));
                    rowIncrement += WORD_INCREMENT;
                }
                else {
                    mnemonic = String.format(format, addressMode.asString(rrr));
                }

                if (addressMode.isPostIncremented() && rrr == Cpu.PC) {
                    rowIncrement += WORD_INCREMENT;
                }

                break;
            }

            case MOV:
            case ADD:
            case SUB:
            case CMP:
            case AND:
            case OR: {
                final var nextByte = cpu.getByte(currentRow + rowIncrement);
                ++rowIncrement;
                final var word = Shorts.toUnsignedInt(Shorts.fromBytes(opCode, nextByte));
                final var mmm1 = (word & 0b0000_1110_0000_0000) >> 9;
                final var rrr1 = (word & 0b0000_0001_1100_0000) >> 6;
                final var mmm2 = (word & 0b0000_0000_0011_1000) >> 3;
                final var rrr2 = word & 0b0000_0000_0000_0111;

                final var srcMode = AddressMode.fromInt(mmm1);

                final String srcString;
                if (srcMode.isIndexed()) {
                    final var msb = cpu.getByte(currentRow + rowIncrement);
                    final var lsb = cpu.getByte(currentRow + rowIncrement + 1);
                    rowIncrement += WORD_INCREMENT;
                    final var ddd = Shorts.toUnsignedInt(Shorts.fromBytes(msb, lsb));
                    srcString = srcMode.asString(ddd, rrr1);
                }
                else {
                    srcString = srcMode.asString(rrr1);
                }

                if (srcMode.isPostIncremented() && rrr1 == Cpu.PC) {
                    rowIncrement += WORD_INCREMENT;
                }

                final var dstMode = AddressMode.fromInt(mmm2);
                final String dstString;
                if (dstMode.isIndexed()) {
                    final var msb = cpu.getByte(currentRow + rowIncrement);
                    final var lsb = cpu.getByte(currentRow + rowIncrement + 1);
                    rowIncrement += WORD_INCREMENT;
                    final var ddd = Shorts.toUnsignedInt(Shorts.fromBytes(msb, lsb));
                    dstString = dstMode.asString(ddd, rrr2);
                }
                else {
                    dstString = dstMode.asString(rrr2);
                }

                if (dstMode.isPostIncremented() && rrr2 == Cpu.PC) {
                    rowIncrement += WORD_INCREMENT;
                }

                mnemonic = String.format(format, srcString, dstString);
                break;
            }

            default:
                mnemonic = InstructionString.NOP.getFormatString();
            }

            if (forceAllRows || (cpu.getMnemonic(currentRow) == null || !cpu.getMnemonic(currentRow).equals(mnemonic))) {
                cpu.setMnemonic(currentRow, mnemonic);
                for (var j = 1; j < rowIncrement; ++j) {
                    cpu.setMnemonic(currentRow + j, EMPTY_STRING);
                }
            }
            else {
                break;
            }
            currentRow += rowIncrement;
        }
        return currentRow;
    }
}
