package com.belchingjalapeno.androidchip8.assembly.assembler.exceptions;

import com.belchingjalapeno.androidchip8.assembly.assembler.Assembler;

/**
 * Base class thrown when the {@link Assembler} finds an error while assembling.
 */
public abstract class AssemblerException extends Exception {

    private final int lineNumber;
    private final int start;

    public AssemblerException(String detailMessage, int lineNumber, int start) {
        super("line:" + lineNumber + ": " + detailMessage);
        this.lineNumber = lineNumber;
        this.start = start;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public int getStart() {
        return start;
    }
}
