package com.belchingjalapeno.androidchip8.assembly.assembler.exceptions;

/**
 *
 */
public class InvalidRegisterException extends AssemblerException {

    public InvalidRegisterException(String invalidRegister, int lineNumber, int start) {
        super("invalid register : " + invalidRegister, lineNumber, start);
    }
}
