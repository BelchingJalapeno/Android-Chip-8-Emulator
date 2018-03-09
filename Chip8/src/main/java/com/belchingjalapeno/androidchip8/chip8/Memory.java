package com.belchingjalapeno.androidchip8.chip8;

import com.belchingjalapeno.androidchip8.Util;
import com.belchingjalapeno.androidchip8.chip8.graphics.Font;

/**
 * Holds program instructions and data.
 */
public class Memory {

    private final short[] memory = new short[4096];
    private int programCounter = 0x0200;
    private boolean initialized = false;

    /**
     * Reads 2 bytes starting at the program counter used for executing by the cpu.
     *
     * @return 2-byte instruction
     */
    public int next() {
        if (!initialized) {
            throw new RuntimeException("Chip-8 Memory not initialized before reading instructions! Must call initialize() method first.");
        }
        int s;
        s = memory[programCounter];
        programCounter++;
        outOfBoundsCheck(programCounter);
        s = ((s << 8) + (memory[programCounter]));
        programCounter++;
        outOfBoundsCheck(programCounter);
        return s;
    }

    /**
     * Loads the {@link Font} into memory starting at the {@link Font#LOCATION}.
     */
    private void loadFontToMemory() {
        programCounter = Font.LOCATION;
        for (int i = 0; i < Font.FONT.length; i++) {
            memory[programCounter + i] = Font.FONT[i];
        }
    }

    /**
     * Returns a single byte in memory at the address.
     *
     * @param address the address of the byte
     * @return a byte stored at the address
     */
    public short getByte(int address) {
        outOfBoundsCheck(address);
        return memory[address];
    }

    private void outOfBoundsCheck(int address) {
        if (address < 0 || address >= memory.length) {
            throw new IndexOutOfBoundsException();
        }
    }

    /**
     * Sets a byte in memory at address to the data.
     *
     * @param address the address of the byte to set
     * @param data    the byte to set at the address
     */
    public void setByte(int address, short data) {
        outOfBoundsCheck(address);
        memory[address] = Util.wrapToByte(data);
    }

    /**
     * Clears memory, loads the font to memory, and sets the program counter to 0x200.
     */
    public void initialize() {
        initialized = true;
        resetMemory();
        loadFontToMemory();

        programCounter = 0x200;
    }

    /**
     * Sets all memory locations to 0.
     */
    private void resetMemory() {
        for (int i = 0; i < memory.length; i++) {
            memory[i] = 0;
        }
    }

    /**
     * Loads data into memory starting at address 0x200 and sets the program counter to the start of the program
     * (0x200).
     *
     * @param data the data to load into memory.
     */
    public void loadProgram(short[] data) {
        if (data.length > this.memory.length - 0x200) {
            throw new IndexOutOfBoundsException();
        }
        programCounter = 0x200;
        for (int i = 0; i < data.length; i++) {
            memory[programCounter + i] = data[i];
        }
        programCounter = 0x200;
    }

    /**
     * @return the programs current location in memory
     */
    public int getProgramCounter() {
        return programCounter;
    }

    /**
     * Sets the program to the address.
     *
     * @param address the address to set the program counter
     */
    public void setProgramCounter(int address) {
        outOfBoundsCheck(address);
        programCounter = address;
    }
}