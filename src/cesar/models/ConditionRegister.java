package cesar.models;

import cesar.utils.Shorts;

class ConditionRegister {
    private boolean negative;

    private boolean zero;
    private boolean carry;
    private boolean overflow;
    private int bits;

    public ConditionRegister() {
        setNegative(false);
        setZero(true);
        setCarry(false);
        setOverflow(false);
        bits = 0b0100;
    }

    public void ccc(final int newValue) {
        // TODO: Testar
        bits &= ~newValue;
        update();
    }

    private void update() {
        setNegative((bits & 8) == 8);
        setZero((bits & 4) == 4);
        setCarry((bits & 2) == 2);
        setOverflow((bits & 1) == 1);
    }

    public boolean isCarry() {
        return carry;
    }

    public void setCarry(final boolean value) {
        carry = value;
        bits |= 0b0010;
    }

    public boolean isNegative() {
        return negative;
    }

    public void setNegative(final boolean value) {
        negative = value;
        bits |= 0b1000;
    }

    public boolean isOverflow() {
        return overflow;
    }

    public void setOverflow(final boolean value) {
        overflow = value;
        bits |= 0b0001;
    }

    public boolean isZero() {
        return zero;
    }

    public void setZero(final boolean value) {
        zero = value;
        bits |= 0b0100;
    }

    public void scc(final int newValue) {
        // TODO: Testar
        bits |= newValue;
        update();
    }

    public void testCarry(final short a, final short b, final CarryOperation operation) {
        final int ua = Shorts.toUnsignedInt(a);
        final int ub = Shorts.toUnsignedInt(b);
        int result;
        if (operation == CarryOperation.PLUS) {
            result = ua + ub;
        }
        else {
            result = ua - ub;
        }
        setCarry((result & 0x1_0000) == 0x1_0000);
    }

    public void testNegative(final short value) {
        setNegative(value < 0);
    }

    public void testOverflow(final short op1, final short op2, final short result) {
        setOverflow(op1 > 0 && op2 > 0 && result < 0 || op1 < 0 && op2 < 0 && result > 0);
    }

    public void testZero(final short value) {
        setZero(value == 0);
    }

    enum CarryOperation {
        PLUS, MINUS
    }
}
