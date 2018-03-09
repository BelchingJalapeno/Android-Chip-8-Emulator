package com.belchingjalapeno.androidchip8.chip8;

import com.belchingjalapeno.androidchip8.Util;

/**
 * Stores the 16 8-bit general purpose registers along with the 16-bit address register.
 * Register {@code 0xF} also doubles as a flag for many operations.
 */
public class Registers {

    /**
     * stores 8 bit values of the general purpose registers
     */
    private final short[] V = new short[16];

    /**
     * 16 bit address register used for memory manipulation operations with only the least significant 12 bits used.
     */
    private int I;

    /**
     * @param register the register to getCurrentCount (0-F);
     * @return the 8 bit value held in this register
     */
    public short getRegisterData(int register) throws IndexOutOfBoundsException {
        if (register < 0 || register > 0xF) {
            throw new IndexOutOfBoundsException("Trying to access an out of bounds register : 0x" + Integer.toHexString(register));
        }
        return V[register];
    }

    /**
     * @param register the register to set between 0x0 and 0xF
     * @param value    the 8 bit value to set the register to
     * @throws IndexOutOfBoundsException if attempting to access a register less than {@code 0x0} or greater than {@code 0xF}
     */
    public void setRegister(int register, short value) throws IndexOutOfBoundsException {
        if (register < 0 || register > 0xF) {
            throw new IndexOutOfBoundsException("Trying to access an out of bounds register : 0x" + Integer.toHexString(register));
        }
        value = Util.wrapToByte(value);
        V[register] = value;
    }

    /**
     * 16 bit address register used for memory manipulation operations with only the least significant 12 bits used.
     */
    public int getI() {
        return I;
    }

    /**
     * sets the register using only the least significant 12 bits.
     *
     * @param I the value to set the I register to
     */
    public void setI(int I) {
        this.I = I & 0xFFF;
    }
}