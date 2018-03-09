package com.belchingjalapeno.androidchip8.assembly.assembler.exceptions;

import com.belchingjalapeno.androidchip8.Util;

/**
 *
 */
public class UnsupportedOperandException extends AssemblerException {
    private final String opcode;
    private final String[] spaceSeparatedOperation;
    private final int unsupportedOperandIndex;

    public UnsupportedOperandException(String message, int lineNumber, int start, String opcode, String[] spaceSeparatedOperation, int unsupportedOperandIndex) {
        super(message, lineNumber, start);
        this.opcode = opcode;
        this.spaceSeparatedOperation = spaceSeparatedOperation;
        this.unsupportedOperandIndex = unsupportedOperandIndex;
    }

    public UnsupportedOperandException(int lineNumber, int start, String opcode, String[] spaceSeparatedOperation, int unsupportedOperandIndex) {
        this(spaceSeparatedOperation[unsupportedOperandIndex] + " in : " + Util.joinStrings(spaceSeparatedOperation, " "), lineNumber, start, opcode, spaceSeparatedOperation, unsupportedOperandIndex);
    }

    public String getOpcode() {
        return opcode;
    }

    public String[] getSpaceSeparatedOperation() {
        return spaceSeparatedOperation;
    }

    public int getUnsupportedOperandIndex() {
        return unsupportedOperandIndex;
    }
}
