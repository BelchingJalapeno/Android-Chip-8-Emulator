package com.belchingjalapeno.androidchip8.assembly.assembler.exceptions;

/**
 *
 */
public class ExtraOperandsException extends AssemblerException {
    public ExtraOperandsException(String detailMessage, int lineNumber, int start) {
        super(detailMessage, lineNumber, start);
    }
}
