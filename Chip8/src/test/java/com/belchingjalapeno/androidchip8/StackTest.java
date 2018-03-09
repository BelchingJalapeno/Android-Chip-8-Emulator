package com.belchingjalapeno.androidchip8;

import com.belchingjalapeno.androidchip8.chip8.Memory;
import com.belchingjalapeno.androidchip8.chip8.Stack;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 *
 */
public class StackTest {
    private final Memory memory = new Memory();
    private final Stack stack = new Stack(memory);
    private final int start = 0x200;

    @Before
    public void setUp() throws Exception {
        memory.setProgramCounter(start);
    }

    @Test
    public void testPush() throws Exception {
        try {
            for (int i = 0; i < 16; i++) {
                stack.push(i);
            }
        } catch (Exception e) {
            fail();
        }
        try {
            stack.push(16);
            fail();
        } catch (Exception ignored) {
        }
    }

    @Test
    public void testPop() throws Exception {
        int start = memory.getProgramCounter();
        for (int i = 0; i < 16; i++) {
            stack.push(i);
            assertEquals(memory.getProgramCounter(), i);
        }
        for (int i = 0; i < 16; i++) {
            assertEquals(memory.getProgramCounter(), 15 - i);
            stack.pop();
            if (i != 15) {
                assertEquals("i : " + i, 15 - i - 1, memory.getProgramCounter());
            } else {
                assertEquals("i : " + i, start, memory.getProgramCounter());
            }
        }
        try {
            stack.pop();
            fail();
        } catch (Exception ignored) {
        }
    }
}