package com.belchingjalapeno.androidchip8.chip8;

/**
 * Used to store and return from memory locations of subroutines.
 */
public class Stack {

    private final Memory memory;
    private final int[] stack = new int[16];
    private int stackPointer = 0;

    /**
     * Stores the {@link Memory} to use for program counter manipulation.
     *
     * @param memory the memory to use for program counter manipulation
     */
    public Stack(Memory memory) {
        this.memory = memory;
    }

    private void checkOutOfBounds() throws IndexOutOfBoundsException {
        if (stackPointer < 0 || stackPointer > stack.length) {
            throw new IndexOutOfBoundsException("Stack pointer out of bounds : " + stackPointer);
        }
    }

    /**
     * Stores the current address of the program counter on top of the stack, then sets the program counter
     * to {@code address}.
     *
     * @param address The address of where the program counter should now be set to.
     * @throws IndexOutOfBoundsException if this {@link Stack} is full(holds 16)
     */
    public void push(int address) throws IndexOutOfBoundsException {
        stack[stackPointer] = memory.getProgramCounter();
        stackPointer++;
        checkOutOfBounds();
        memory.setProgramCounter(address);
    }

    /**
     * Removes a address off the top of the stack and sets the program counter to it.
     * Called to return from a subroutine.
     *
     * @throws IndexOutOfBoundsException if this {@link Stack} is empty
     */
    public void pop() throws IndexOutOfBoundsException {
        stackPointer--;
        checkOutOfBounds();
        memory.setProgramCounter(stack[stackPointer]);
    }
}