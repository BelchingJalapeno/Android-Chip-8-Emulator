package com.belchingjalapeno.androidchip8.assembly.assembler.exceptions;

/**
 *
 */
public class UnsupportedOpCodeException extends AssemblerException {

    private final String opcode;
    private final String fullInstruction;

    public UnsupportedOpCodeException(String message, int line, int start, String opcode, String fullInstruction) {
        super(message, line, start);
        this.opcode = opcode;
        this.fullInstruction = fullInstruction;
    }

    public UnsupportedOpCodeException(int line, int start, String opcode, String fullInstruction) {
        this(fullInstruction, line, start, opcode, fullInstruction);
    }

    /**
     * @return the opcode part of an instruction.
     */
    public String getOpcode() {
        return opcode;
    }

    /**
     * @return the full instruction, opcode and operands both
     */
    public String getFullInstruction() {
        return fullInstruction;
    }
}
