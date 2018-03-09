package com.belchingjalapeno.androidchip8.assembly.assembler.exceptions;

/**
 *
 */
public class InvalidLabelNameException extends AssemblerException {
    public InvalidLabelNameException(String detailMessage, int lineNumber, int start) {
        super(detailMessage, lineNumber, start);
    }
}
