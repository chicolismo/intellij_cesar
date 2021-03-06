package cesar.models;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;

public enum InstructionString {
    NOP, CCC, SCC, BR, BNE, BEQ, BPL, BMI, BVC, BVS, BCC, BCS, BGE, BLT, BGT, BLE, BHI, BLS, JMP, SOB, JSR, RTS, RTI,
    CLR, NOT, INC, DEC, NEG, TST, ROR, ROL, ASR, ASL, ADC, SBC, MOV, ADD, SUB, CMP, AND, OR, HLT;

    private static final HashMap<Integer, InstructionString> INSTRUCTION_MAP;
    private static final EnumMap<InstructionString, String> FORMAT;

    private static final EnumSet<InstructionString> CONDITIONAL_BRANCH_INSTRUCTIONS;
    private static final EnumSet<InstructionString> ONE_OP_INSTRUCTIONS;
    private static final EnumSet<InstructionString> TWO_OP_INSTRUCTIONS;

    static {
        INSTRUCTION_MAP = new HashMap<>();
        INSTRUCTION_MAP.put(0b0000, NOP);
        INSTRUCTION_MAP.put(0b0001, CCC);
        INSTRUCTION_MAP.put(0b0010, SCC);
        // Desvio condicional
        INSTRUCTION_MAP.put(0b0011_0000, BR);
        INSTRUCTION_MAP.put(0b0011_0001, BNE);
        INSTRUCTION_MAP.put(0b0011_0010, BEQ);
        INSTRUCTION_MAP.put(0b0011_0011, BPL);
        INSTRUCTION_MAP.put(0b0011_0100, BMI);
        INSTRUCTION_MAP.put(0b0011_0101, BVC);
        INSTRUCTION_MAP.put(0b0011_0110, BVS);
        INSTRUCTION_MAP.put(0b0011_0111, BCC);
        INSTRUCTION_MAP.put(0b0011_1000, BCS);
        INSTRUCTION_MAP.put(0b0011_1001, BGE);
        INSTRUCTION_MAP.put(0b0011_1010, BLT);
        INSTRUCTION_MAP.put(0b0011_1011, BGT);
        INSTRUCTION_MAP.put(0b0011_1100, BLE);
        INSTRUCTION_MAP.put(0b0011_1101, BHI);
        INSTRUCTION_MAP.put(0b0011_1110, BLS);
        // Outras
        INSTRUCTION_MAP.put(0b0100, JMP);
        INSTRUCTION_MAP.put(0b0101, SOB);
        INSTRUCTION_MAP.put(0b0110, JSR);
        INSTRUCTION_MAP.put(0b0111, RTS);

        // TODO: Verificar se está correto
        // RTI
        INSTRUCTION_MAP.put(0b0111_1000, RTI);

        // Instruções de um operando
        INSTRUCTION_MAP.put(0b1000_0000, CLR);
        INSTRUCTION_MAP.put(0b1000_0001, NOT);
        INSTRUCTION_MAP.put(0b1000_0010, INC);
        INSTRUCTION_MAP.put(0b1000_0011, DEC);
        INSTRUCTION_MAP.put(0b1000_0100, NEG);
        INSTRUCTION_MAP.put(0b1000_0101, TST);
        INSTRUCTION_MAP.put(0b1000_0110, ROR);
        INSTRUCTION_MAP.put(0b1000_0111, ROL);
        INSTRUCTION_MAP.put(0b1000_1000, ASR);
        INSTRUCTION_MAP.put(0b1000_1001, ASL);
        INSTRUCTION_MAP.put(0b1000_1010, ADC);
        INSTRUCTION_MAP.put(0b1000_1011, SBC);
        // Instruções de dois operandos
        INSTRUCTION_MAP.put(0b1001, MOV);
        INSTRUCTION_MAP.put(0b1010, ADD);
        INSTRUCTION_MAP.put(0b1011, SUB);
        INSTRUCTION_MAP.put(0b1100, CMP);
        INSTRUCTION_MAP.put(0b1101, AND);
        INSTRUCTION_MAP.put(0b1110, OR);
        // Instrução de parada
        INSTRUCTION_MAP.put(0b1111, HLT);

        CONDITIONAL_BRANCH_INSTRUCTIONS = EnumSet.of(BR, BNE, BEQ, BPL, BMI, BVC, BVS, BCC, BCS, BGE, BLT, BGT, BLE,
            BHI, BLS);
        ONE_OP_INSTRUCTIONS = EnumSet.of(CLR, NOT, INC, DEC, NEG, TST, ROR, ROL, ASR, ASL, ADC, SBC);
        TWO_OP_INSTRUCTIONS = EnumSet.of(MOV, ADD, SUB, CMP, AND, OR);

        FORMAT = new EnumMap<>(InstructionString.class);
        FORMAT.put(NOP, "NOP");
        FORMAT.put(HLT, "HLT");
        FORMAT.put(CCC, "CCC %s");
        FORMAT.put(SCC, "SCC %s");
        for (final InstructionString instruction : InstructionString.CONDITIONAL_BRANCH_INSTRUCTIONS) {
            FORMAT.put(instruction, instruction.toString() + " %d");
        }
        FORMAT.put(JMP, "JMP %s"); // modo
        FORMAT.put(SOB, "SOB R%d, %d"); // registrador, ddd
        FORMAT.put(JSR, "JSR R%d, %s"); // registrador, modo
        FORMAT.put(RTS, "RTS R%d"); // registrador
        FORMAT.put(RTI, "RTI"); //
        for (final InstructionString instruction : InstructionString.ONE_OP_INSTRUCTIONS) {
            FORMAT.put(instruction, instruction.toString() + " %s"); // modo
        }
        for (final InstructionString instruction : InstructionString.TWO_OP_INSTRUCTIONS) {
            FORMAT.put(instruction, instruction.toString() + " %s, %s"); // modo, modo
        }
    }

    public String getFormatString() {
        return FORMAT.get(this);
    }

    public static InstructionString getInstruction(final byte opCode) {
        var key = 0xFF & opCode;
        if (INSTRUCTION_MAP.containsKey(key)) {
            return INSTRUCTION_MAP.get(key);
        }

        key = (0xF0 & key) >> 4;
        if (INSTRUCTION_MAP.containsKey(key)) {
            return INSTRUCTION_MAP.get(key);
        }

        return NOP;
    }
}
