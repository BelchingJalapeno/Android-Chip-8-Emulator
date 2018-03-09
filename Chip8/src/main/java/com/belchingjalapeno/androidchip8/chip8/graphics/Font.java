package com.belchingjalapeno.androidchip8.chip8.graphics;

/**
 * Holds the Font sprite data and its location with a helper method to find the sprite character location
 * from a hexadecimal number({@code 0x0} to {@code 0xF}).
 */
public class Font {

    /**
     * The starting location in {@link com.belchingjalapeno.androidchip8.chip8.Memory Memory} that the {@link Font}
     * should be stored at.  It takes up 80 bytes.
     */
    public static final int LOCATION = 0x0050;
    /**
     * The font sprite data, 5 bytes per character sprite, going from {@code 0x0} to {@code 0xF}.
     */
    public static final short[] FONT = new short[]{
            (short) 0xF0, (short) 0x90, (short) 0x90, (short) 0x90, (short) 0xF0, // 0
            (short) 0x20, (short) 0x60, (short) 0x20, (short) 0x20, (short) 0x70, // 1
            (short) 0xF0, (short) 0x10, (short) 0xF0, (short) 0x80, (short) 0xF0, // 2
            (short) 0xF0, (short) 0x10, (short) 0xF0, (short) 0x10, (short) 0xF0, // 3
            (short) 0x90, (short) 0x90, (short) 0xF0, (short) 0x10, (short) 0x10, // 4
            (short) 0xF0, (short) 0x80, (short) 0xF0, (short) 0x10, (short) 0xF0, // 5
            (short) 0xF0, (short) 0x80, (short) 0xF0, (short) 0x90, (short) 0xF0, // 6
            (short) 0xF0, (short) 0x10, (short) 0x20, (short) 0x40, (short) 0x40, // 7
            (short) 0xF0, (short) 0x90, (short) 0xF0, (short) 0x90, (short) 0xF0, // 8
            (short) 0xF0, (short) 0x90, (short) 0xF0, (short) 0x10, (short) 0xF0, // 9
            (short) 0xF0, (short) 0x90, (short) 0xF0, (short) 0x90, (short) 0x90, // A
            (short) 0xE0, (short) 0x90, (short) 0xE0, (short) 0x90, (short) 0xE0, // B
            (short) 0xF0, (short) 0x80, (short) 0x80, (short) 0x80, (short) 0xF0, // C
            (short) 0xE0, (short) 0x90, (short) 0x90, (short) 0x90, (short) 0xE0, // D
            (short) 0xF0, (short) 0x80, (short) 0xF0, (short) 0x80, (short) 0xF0, // E
            (short) 0xF0, (short) 0x80, (short) 0xF0, (short) 0x80, (short) 0x80  // F
    };

    /**
     * Retrieves the location in {@link com.belchingjalapeno.androidchip8.chip8.Memory Memory} of
     * the character sprite.
     *
     * @param character the hexadecimal character({@code 0x0} to {@code 0xF}) to return the location of.
     * @return the position in memory that the {@code character} should be located.
     */
    public static int get(short character) {
        // 5 because each char is 5 bytes long
        return (LOCATION + (character * 5));
    }
}
