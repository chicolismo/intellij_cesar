package cesar.hardware;

import java.util.HashMap;
import java.util.HashSet;

public enum Instruction {
    NOP, CCC, SCC, BR, BNE, BEQ, BPL, BMI, BVC, BVS, BCC, BCS, BGE, BLT, BGT, BLE, BHI, BLS, JMP, SOB, JSR, RTS, CLR,
    NOT, INC, DEC, NEG, TST, ROR, ROL, ASR, ASL, ADC, SBC, MOV, ADD, SUB, CMP, AND, OR, HLT;

    private static final HashMap<Integer, Instruction> INSTRUCTION_MAP;
    private static final HashMap<Instruction, String> FORMAT;

    private static final HashSet<Instruction> CONDITIONAL_BRANCH_INSTRUCTIONS;
    private static final HashSet<Instruction> ONE_OP_INSTRUCTIONS;
    private static final HashSet<Instruction> TWO_OP_INSTRUCTIONS;

    static {
        INSTRUCTION_MAP = new HashMap<>();
        INSTRUCTION_MAP.put(0b0000, NOP);
        INSTRUCTION_MAP.put(0b0001, CCC);
        INSTRUCTION_MAP.put(0b0010, SCC);
        // Desvio condicional
        INSTRUCTION_MAP.put(0b00110000, BR);
        INSTRUCTION_MAP.put(0b00110001, BNE);
        INSTRUCTION_MAP.put(0b00110010, BEQ);
        INSTRUCTION_MAP.put(0b00110011, BPL);
        INSTRUCTION_MAP.put(0b00110100, BMI);
        INSTRUCTION_MAP.put(0b00110101, BVC);
        INSTRUCTION_MAP.put(0b00110110, BVS);
        INSTRUCTION_MAP.put(0b00110111, BCC);
        INSTRUCTION_MAP.put(0b00111000, BCS);
        INSTRUCTION_MAP.put(0b00111001, BGE);
        INSTRUCTION_MAP.put(0b00111010, BLT);
        INSTRUCTION_MAP.put(0b00111011, BGT);
        INSTRUCTION_MAP.put(0b00111100, BLE);
        INSTRUCTION_MAP.put(0b00111101, BHI);
        INSTRUCTION_MAP.put(0b00111110, BLS);
        // Outras
        INSTRUCTION_MAP.put(0b0100, JMP);
        INSTRUCTION_MAP.put(0b0101, SOB);
        INSTRUCTION_MAP.put(0b0110, JSR);
        INSTRUCTION_MAP.put(0b0111, RTS);
        // Instruções de um operando
        INSTRUCTION_MAP.put(0b10000000, CLR);
        INSTRUCTION_MAP.put(0b10000001, NOT);
        INSTRUCTION_MAP.put(0b10000010, INC);
        INSTRUCTION_MAP.put(0b10000011, DEC);
        INSTRUCTION_MAP.put(0b10000100, NEG);
        INSTRUCTION_MAP.put(0b10000101, TST);
        INSTRUCTION_MAP.put(0b10000110, ROR);
        INSTRUCTION_MAP.put(0b10000111, ROL);
        INSTRUCTION_MAP.put(0b10001000, ASR);
        INSTRUCTION_MAP.put(0b10001001, ASL);
        INSTRUCTION_MAP.put(0b10001010, ADC);
        INSTRUCTION_MAP.put(0b10001011, SBC);
        // Instruções de dois operandos
        INSTRUCTION_MAP.put(0b1001, MOV);
        INSTRUCTION_MAP.put(0b1010, ADD);
        INSTRUCTION_MAP.put(0b1011, SUB);
        INSTRUCTION_MAP.put(0b1100, CMP);
        INSTRUCTION_MAP.put(0b1101, AND);
        INSTRUCTION_MAP.put(0b1110, OR);
        // Instrução de parada
        INSTRUCTION_MAP.put(0b1111, HLT);

        CONDITIONAL_BRANCH_INSTRUCTIONS = new HashSet<>(15);
        CONDITIONAL_BRANCH_INSTRUCTIONS.add(BR);
        CONDITIONAL_BRANCH_INSTRUCTIONS.add(BNE);
        CONDITIONAL_BRANCH_INSTRUCTIONS.add(BEQ);
        CONDITIONAL_BRANCH_INSTRUCTIONS.add(BPL);
        CONDITIONAL_BRANCH_INSTRUCTIONS.add(BMI);
        CONDITIONAL_BRANCH_INSTRUCTIONS.add(BVC);
        CONDITIONAL_BRANCH_INSTRUCTIONS.add(BVS);
        CONDITIONAL_BRANCH_INSTRUCTIONS.add(BCC);
        CONDITIONAL_BRANCH_INSTRUCTIONS.add(BCS);
        CONDITIONAL_BRANCH_INSTRUCTIONS.add(BGE);
        CONDITIONAL_BRANCH_INSTRUCTIONS.add(BLT);
        CONDITIONAL_BRANCH_INSTRUCTIONS.add(BGT);
        CONDITIONAL_BRANCH_INSTRUCTIONS.add(BLE);
        CONDITIONAL_BRANCH_INSTRUCTIONS.add(BHI);
        CONDITIONAL_BRANCH_INSTRUCTIONS.add(BLS);

        ONE_OP_INSTRUCTIONS = new HashSet<>(12);
        ONE_OP_INSTRUCTIONS.add(CLR);
        ONE_OP_INSTRUCTIONS.add(NOT);
        ONE_OP_INSTRUCTIONS.add(INC);
        ONE_OP_INSTRUCTIONS.add(DEC);
        ONE_OP_INSTRUCTIONS.add(NEG);
        ONE_OP_INSTRUCTIONS.add(TST);
        ONE_OP_INSTRUCTIONS.add(ROR);
        ONE_OP_INSTRUCTIONS.add(ROL);
        ONE_OP_INSTRUCTIONS.add(ASR);
        ONE_OP_INSTRUCTIONS.add(ASL);
        ONE_OP_INSTRUCTIONS.add(ADC);
        ONE_OP_INSTRUCTIONS.add(SBC);

        TWO_OP_INSTRUCTIONS = new HashSet<>(6);
        TWO_OP_INSTRUCTIONS.add(MOV);
        TWO_OP_INSTRUCTIONS.add(ADD);
        TWO_OP_INSTRUCTIONS.add(SUB);
        TWO_OP_INSTRUCTIONS.add(CMP);
        TWO_OP_INSTRUCTIONS.add(AND);
        TWO_OP_INSTRUCTIONS.add(OR);

        FORMAT = new HashMap<>();
        FORMAT.put(NOP, "NOP");
        FORMAT.put(HLT, "HLT");
        FORMAT.put(CCC, "CCC %s");
        FORMAT.put(SCC, "SCC %s");
        for (final Instruction instruction : Instruction.CONDITIONAL_BRANCH_INSTRUCTIONS) {
            FORMAT.put(instruction, instruction.toString() + " %d");
        }
        FORMAT.put(JMP, "JMP %s"); // modo
        FORMAT.put(SOB, "SOB R%d, %d"); // registrador, ddd
        FORMAT.put(JSR, "JSR R%d, %s"); // registrador, modo
        FORMAT.put(RTS, "RTS R%d"); // registrador
        for (final Instruction instruction : Instruction.ONE_OP_INSTRUCTIONS) {
            FORMAT.put(instruction, instruction.toString() + " %s"); // modo
        }
        for (final Instruction instruction : Instruction.TWO_OP_INSTRUCTIONS) {
            FORMAT.put(instruction, instruction.toString() + " %s, %s"); // modo, modo
        }
    }

    public static Instruction getInstruction(final byte opcode) {
        return INSTRUCTION_MAP.get(0xFF & opcode);
    }

    public String getFormatString() {
        return FORMAT.get(this);
    }
}
