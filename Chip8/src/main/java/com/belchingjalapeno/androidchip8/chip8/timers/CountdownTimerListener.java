package com.belchingjalapeno.androidchip8.chip8.timers;

/**
 * Listens for a {@link CountdownTimer} countdown event which happens at a rate of 60 hz while is it running.
 */
public interface CountdownTimerListener {
    /**
     * Called every time the {@link CountdownTimer} counts down.
     *
     * @param count the current count of the {@link CountdownTimer}
     */
    void onCountdown(int count);
}
