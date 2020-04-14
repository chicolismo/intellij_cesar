package cesar.hardware;

class Operand {
    public final short value;
    public final int address;
    public final Cpu.AddressMode addressMode;

    public Operand(final short value, final int address, final Cpu.AddressMode addressMode) {
        this.value = value;
        this.address = address;
        this.addressMode = addressMode;
    }
}
