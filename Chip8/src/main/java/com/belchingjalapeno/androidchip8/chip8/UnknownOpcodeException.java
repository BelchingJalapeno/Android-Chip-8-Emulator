package com.belchingjalapeno.androidchip8.chip8;

/**
 * Throw when the cpu tries to run an invalid opcode.
 */
public class UnknownOpcodeException extends RuntimeException {

    /**
     * Formats this opcode to {@code "Unknown opCode [0x0000]: 0x%X"} and then passes it on to {@link RuntimeException}
     *
     * @param opcode the invalid opcode
     */
    public UnknownOpcodeException(int opcode) {
        super(String.format("Unknown opCode [0x0000]: 0x%X", opcode));
    }
}
