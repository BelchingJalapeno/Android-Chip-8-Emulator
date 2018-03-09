package com.belchingjalapeno.androidchip8;

import com.belchingjalapeno.androidchip8.chip8.Memory;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 *
 */
public class MemoryTest {

    private final Memory memory = new Memory();
    @Rule
    public ExpectedException rule = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        memory.initialize();
    }

    @Test
    public void testInitialize() throws Exception {
        try {
            Memory m = new Memory();
            m.initialize();
            m.next();
        } catch (Exception e) {
            Assert.fail();
        }
        try {
            Memory m = new Memory();
            m.next();
            Assert.fail();
        } catch (Exception ignored) {
        }
    }

    @Test
    public void testNext() throws Exception {
        short data = (short) 230;
        memory.setByte(0x200, (short) 0);
        memory.setByte(0x201, data);
        memory.setProgramCounter(0x200);
        Assert.assertEquals(memory.next(), data);
    }

    @Test
    public void testGetByte() throws Exception {
        short data = (short) 245;
        memory.setByte(0x200, data);
        Assert.assertEquals(memory.getByte(0x200), data);
        data = (short) 29239;
        memory.setByte(0x200, data);
        Assert.assertEquals(memory.getByte(0x200), data % 256);
        data = (short) -29239;
        memory.setByte(0x200, data);
        Assert.assertEquals(memory.getByte(0x200), 256 + data % 256);
        data = (short) -1;
        memory.setByte(0x200, data);
        Assert.assertEquals(memory.getByte(0x200), 255);
        data = (short) -128;
        memory.setByte(0x200, data);
        Assert.assertEquals(memory.getByte(0x200), 128);
        data = (short) -127;
        memory.setByte(0x200, data);
        Assert.assertEquals(memory.getByte(0x200), 129);
    }

    @Test
    public void testSetByte() throws Exception {
        short data = (short) 245;
        memory.setByte(0x200, data);
        Assert.assertEquals(memory.getByte(0x200), data % 256);
        data = (short) 29239;
        memory.setByte(0x200, data);
        Assert.assertEquals(memory.getByte(0x200), data % 256);
        data = (short) -29239;
        memory.setByte(0x200, data);
        Assert.assertEquals(memory.getByte(0x200), 256 + data % 256);
    }

    @Test
    public void testSetProgramCounter() throws Exception {
        short data = (short) 110;
        short data2 = (short) 243;
        memory.setByte(0x400, (short) 0);
        memory.setByte(0x401, data);
        memory.setByte(0x402, (short) 0);
        memory.setByte(0x403, data2);
        memory.setProgramCounter(0x400);
        Assert.assertEquals(data, memory.next());
        Assert.assertEquals(data2, memory.next());
    }

    @Test
    public void testGetProgramCounter() throws Exception {
        memory.setProgramCounter(0x400);
        Assert.assertEquals(memory.getProgramCounter(), 0x400);
        memory.next();
        Assert.assertEquals(memory.getProgramCounter(), 0x402);
    }

    @Test
    public void testLoadProgram() throws Exception {
        final short[] data = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16};
        memory.loadProgram(data);
        for (int i = 0; i < data.length; i += 2) {
            Assert.assertEquals(data[i] << 8 | data[i + 1], memory.next());
        }
        memory.loadProgram(new short[4096 - 0x200]);
        rule.expect(IndexOutOfBoundsException.class);
        memory.loadProgram(new short[4096 - 0x200 + 1]);
    }

    @Test
    public void testOutOfBounds() throws Exception {
        memory.setProgramCounter(4096 - 1);
        memory.setProgramCounter(0);
        rule.expect(IndexOutOfBoundsException.class);
        memory.setProgramCounter(-1);
        rule.expect(IndexOutOfBoundsException.class);
        memory.setProgramCounter(4096);
//        rule.expect(IndexOutOfBoundsException.class);
    }
}