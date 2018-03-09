package com.belchingjalapeno.androidchip8.chip8.graphics;

import com.belchingjalapeno.androidchip8.chip8.Memory;
import com.belchingjalapeno.androidchip8.chip8.Registers;

/**
 * Holds the Chip-8
 */
public class Display {

    //this holds the actual display data
    private final boolean[][] pixels = new boolean[64][32];
    private final Memory memory;
    private final Registers registers;
    private final PlatformDependentDisplay platformDisplay;
    private boolean shouldDraw = false;

    public Display(Memory memory, Registers registers, PlatformDependentDisplay platformDisplay) {
        this.memory = memory;
        this.registers = registers;
        this.platformDisplay = platformDisplay;
    }

    /**
     * Turns all of the pixels in this {@link Display} off.
     */
    public void clear() {
        shouldDraw = true;
        for (int x = 0; x < pixels.length; x++) {
            for (int y = 0; y < pixels[x].length; y++) {
                pixels[x][y] = false;
            }
        }
    }

    /**
     * Draws a sprite stored in the memory at location {@code I} to {@code I + height}. It draws this
     * with the top left corner of the sprite starting at ({@code x}, {@code y}). If the sprite goes out of the
     * bounds of the {@link Display} it gets wrapped around to the other side.
     *
     * @param x      left corner of the sprite
     * @param y      top corner of the sprite
     * @param height height of the sprite in pixels
     */
    public void drawSprite(int x, int y, int height) {
        shouldDraw = true;
        registers.setRegister(0xF, (short) 0x00);
        for (int i = 0; i < height; i++) {
            short row = memory.getByte(registers.getI() + i);
            //go through each bit in this byte
            for (int j = 0; j < 8; j++) {
                int xm = x + j;
                int ym = y + i;
                //wrapping around the display if too large
                xm %= pixels.length;
                ym %= pixels[0].length;
                boolean before = pixels[(xm)][(ym)];
                boolean b = (((row >> 7 - j) & 0b0000_0000_0000_0000_0000_0000_0000_0001) == 1);
                pixels[(xm)][(ym)] ^= b;
                if (before && !pixels[(xm)][(ym)]) {
                    //collision flag
                    registers.setRegister(0xF, (short) 0x01);
                }
            }
        }
    }

    /**
     * Sets the {@link #isShouldDraw()} flag to false and then calls
     * {@link PlatformDependentDisplay#drawToScreen(boolean[][])}.
     */
    public void draw() {
        shouldDraw = false;
        platformDisplay.drawToScreen(pixels);
    }

    public boolean[][] getPixels() {
        return pixels;
    }

    /**
     * @return Flag if the {@link Display} should {@link #draw()}.
     */
    public boolean isShouldDraw() {
        return shouldDraw;
    }
}
