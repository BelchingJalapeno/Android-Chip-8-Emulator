package com.belchingjalapeno.androidchip8;

import com.belchingjalapeno.androidchip8.chip8.Input;
import com.belchingjalapeno.androidchip8.chip8.Memory;
import com.belchingjalapeno.androidchip8.chip8.OpCodes;
import com.belchingjalapeno.androidchip8.chip8.Registers;
import com.belchingjalapeno.androidchip8.chip8.Stack;
import com.belchingjalapeno.androidchip8.chip8.TonePlayer;
import com.belchingjalapeno.androidchip8.chip8.graphics.Display;
import com.belchingjalapeno.androidchip8.chip8.graphics.PlatformDependentDisplay;
import com.belchingjalapeno.androidchip8.chip8.timers.CountdownTimer;
import com.belchingjalapeno.androidchip8.chip8.timers.CountdownTimerListener;

/**
 *
 */
public class Chip8System {

    //all of the chip 8 components
    private final Display display;
    private final CountdownTimer soundTimer;
    private final CountdownTimer delayTimer;
    private final Stack stack;
    private final Memory memory;
    private final Input input;
    private final OpCodes opCodes;
    private final Registers registers;
    //Stored here for easier stopping of playing tone and cleanup
    private final TonePlayer tonePlayer;
    //the number of times the chip 8 system executed an instruction
    private long cycle;

    public Chip8System(final CustomCanvas surfaceView, final TonePlayer tonePlayer) {
        this.tonePlayer = tonePlayer;
        soundTimer = new CountdownTimer(new CountdownTimerListener() {

            private boolean isPlaying = false;

            @Override
            public void onCountdown(int count) {
                if (count == 0 && isPlaying) {
                    //stop playing sound
                    tonePlayer.stop();
                    isPlaying = false;
                } else if (!isPlaying && count > 0) {
                    //start playing sound
                    tonePlayer.play();
                    isPlaying = true;
                }
            }
        });
        delayTimer = new CountdownTimer();
        delayTimer.set((short) 0x00);
        soundTimer.set((short) 0x00);
        delayTimer.start();
        soundTimer.start();
        memory = new Memory();
        registers = new Registers();
        display = new Display(memory, registers, new PlatformDependentDisplay() {
            @Override
            public void drawToScreen(boolean[][] screen) {
                surfaceView.draw(screen);
            }
        });
        stack = new Stack(memory);
        input = new Input();
        opCodes = new OpCodes(memory, stack, input, delayTimer, soundTimer, registers, display);

        memory.initialize();
    }

    /**
     * Executes one cycle, processes all input event if any, and draws the display if it needs drawing.
     */
    public void step() {
        cycle++;
        int next = memory.next();
        input.process();
        opCodes.opCode(next);
        if (display.isShouldDraw()) {
            display.draw();
        }
    }

    public long getCycle() {
        return cycle;
    }

    /**
     * Use to send key events to the chip 8 system.
     *
     * @param event
     */
    public void fireKeyEvent(Input.KeyEvent event) {
        input.fireEvent(event);
    }

    /**
     * Loads a program into the chip-8 memory, and resets the program counter.
     *
     * @param program
     */
    public void load(short[] program) {
        memory.loadProgram(program);
    }

    /**
     * Useful for debugging.
     *
     * @return the current value of the program counter
     */
    public int getProgramCounter() {
        return memory.getProgramCounter();
    }

    /**
     * Useful for debugging.
     *
     * @return the value of the I Register
     */
    public int getIReg() {
        return registers.getI();
    }

    /**
     * Useful for debugging.
     *
     * @param register
     * @return the value of the general purpose V register
     */
    public int getVReg(int register) {
        return registers.getRegisterData(register);
    }

    /**
     *
     */
    public void shutDown() {
        tonePlayer.stop();
        tonePlayer.dispose();
        delayTimer.stop();
        soundTimer.stop();
    }
}
