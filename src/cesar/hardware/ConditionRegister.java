package cesar.hardware;

import cesar.utils.Shorts;

class ConditionRegister {
    enum CarryOperation {
        PLUS, MINUS
    }
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
