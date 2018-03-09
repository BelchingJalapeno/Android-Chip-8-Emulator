package com.belchingjalapeno.androidchip8.assembly.assembler.exceptions;

/**
 * Thrown by the {@link com.belchingjalapeno.androidchip8.assembly.assembler.Assembler} when trying to jump to a location that isn't
 * defined or is invalid.
 */
public class InvalidJumpLocationException extends AssemblerException {

    public InvalidJumpLocationException(String s, int lineNumber, int start) {
        super(s, lineNumber, start);
    }
}
