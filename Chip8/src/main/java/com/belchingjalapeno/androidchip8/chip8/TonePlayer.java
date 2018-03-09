package com.belchingjalapeno.androidchip8.chip8;

/**
 * Plays a tone until it is stopped.
 */
public interface TonePlayer {
    /**
     * Should start playing a tone, and keep playing that tone until {@link #stop()} is called.
     */
    void play();

    /**
     * Should immediately stop playing the tone, and not start playing again until {@klink #play()} is called.
     */
    void stop();

    /**
     * Should release all resources, as we are done using this {@link TonePlayer}.
     */
    void dispose();
}
