package com.belchingjalapeno.androidchip8.assembly.disassembler;

import com.belchingjalapeno.androidchip8.assembly.assembler.Assembler;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 *
 */
public class DisassemblerTest {

    private Disassembler disassembler = new Disassembler();

    @Test
    public void Ox1000() throws Exception {
        assertEquals(".LABEL:0\nJMP .LABEL:0\t\t;0x200", disassembler.disAssemble(new short[]{0x12, 0x00}));
    }

    @Test
    public void dis() throws Exception {
        assertEquals("JMP 0xea5\t\t;0x200", new Disassembler().disAssemble(new short[]{0x1E, 0xA5}));
        short[] opCodes = {0x1E, 0xA5};
        assertArrayEquals(opCodes, new Assembler().assemble(new Disassembler().disAssemble(opCodes)));
    }

    @Test
    public void oddNumberJumpLocation() throws Exception {
        assertEquals("JMP 0x201\t\t;0x200", new Disassembler().disAssemble(new short[]{0x12, 0x01}));
    }
}