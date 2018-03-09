package com.belchingjalapeno.androidchip8.chip8.graphics;

/**
 * Used by the {@link Display} to let the platform dependent code draw the {@link Display} to the device.
 */
public interface PlatformDependentDisplay {
    /**
     * Used to draw the {@link Display} to the screen on a device.  The booleans indicate whether a pixel is on or off.
     *
     * @param screen DO NOT MODIFY THE {@code boolean[][] screen}!!! array telling whether a pixel is on or off.
     */
    void drawToScreen(boolean[][] screen);
}
