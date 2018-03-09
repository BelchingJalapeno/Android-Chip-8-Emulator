package com.belchingjalapeno.androidchip8;

import com.belchingjalapeno.androidchip8.chip8.Input;
import com.belchingjalapeno.androidchip8.chip8.Memory;
import com.belchingjalapeno.androidchip8.chip8.OpCodes;
import com.belchingjalapeno.androidchip8.chip8.Registers;
import com.belchingjalapeno.androidchip8.chip8.Stack;
import com.belchingjalapeno.androidchip8.chip8.UnknownOpcodeException;
import com.belchingjalapeno.androidchip8.chip8.graphics.Display;
import com.belchingjalapeno.androidchip8.chip8.graphics.Font;
import com.belchingjalapeno.androidchip8.chip8.graphics.PlatformDependentDisplay;
import com.belchingjalapeno.androidchip8.chip8.timers.CountdownTimer;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 *
 */
public class OpCodesTest {

    @Rule
    public ExpectedException rule = ExpectedException.none();
    private OpCodes opCodes;
    private Display display;
    private CountdownTimer soundTimer;
    private CountdownTimer delayTimer;
    private Stack stack;
    private Memory memory;
    private Input input;
    private Registers registers;

    @Before
    public void setUp() throws Exception {
        delayTimer = new CountdownTimer();
        delayTimer.set((short) 0x00);
        soundTimer = new CountdownTimer();
        soundTimer.set((short) 0x00);
//        delayTimer.start();
//        soundTimer.start();
        memory = new Memory();
        registers = new Registers();
        display = new Display(memory, registers, new PlatformDependentDisplay() {
            @Override
            public void drawToScreen(boolean[][] screen) {
            }
        });
        stack = new Stack(memory);
        input = new Input();
        opCodes = new OpCodes(memory, stack, input, delayTimer, soundTimer, registers, display);

        memory.initialize();

        new Font();
    }

    @Test
    public void testDisplayDraw() {
        display.drawSprite(0, 0, 3);
        assertTrue(display.isShouldDraw());
        display.draw();
        assertFalse(display.isShouldDraw());
    }

    //
//    @Test
//    public void testOpCode() throws Exception {
//        Ox1NNN();
//        Ox2NNN();
//        Ox0NNN();
//        Ox3VNN();
//        Ox4VNN();
//        Ox5VNN();
//        Ox6VNN();
//        Ox7VNN();
//        Ox8VNN();
//        Ox9VNN();
//        OxANNN();
//        OxBVNN();
//        OxCVNN();
//        OxDVNN();
//        OxEVNN();
//        OxFVNN();
//    }

    @Test
    public void Ox8VNN() {
        setVxToVyTest();
        setVxToVxOrVyTest();
        setVxToVxAndVyTest();
        setVxToVxXorVyTest();
        addVyToVxWithCarryTest();
        subtractVyToVxWithCarryTest();
        shiftVxRightOneTest();
        subtractVxToVyWithCarryTest();
        shiftVxLeftOneTest();
        testUnknownOpcode(0x800F);
    }

    private void testUnknownOpcode(int opcode) {
        rule.expect(UnknownOpcodeException.class);
        opCodes.opCode(opcode);
    }

    private void subtractVyToVxWithCarryTest() {
        int opcode = 0x8005;
        int vx = 0x0000;
        int vy = 0x0010;
        short valueX = 20;
        short valueY = 249;

        subtractVyToVxWithCarry(opcode, vx, vy, valueX, valueY);

        vx = 0x0E00;
        vy = 0x00F0;
        valueX = 0;
        valueY = 255;
        subtractVyToVxWithCarry(opcode, vx, vy, valueX, valueY);

        vx = 0x0E00;
        vy = 0x00F0;
        valueX = 255;
        valueY = 254;
        subtractVyToVxWithCarry(opcode, vx, vy, valueX, valueY);

        vx = 0x0100;
        vy = 0x0040;
        valueX = 1;
        valueY = 0;
        subtractVyToVxWithCarry(opcode, vx, vy, valueX, valueY);

        vx = 0x0600;
        vy = 0x0050;
        valueX = 0;
        valueY = 1;
        subtractVyToVxWithCarry(opcode, vx, vy, valueX, valueY);
    }

    private void subtractVyToVxWithCarry(int opcode, int vx, int vy, short valueX, short valueY) {
        int carry = 0;
        if (valueX > valueY) {
            carry = 1;
        }

        registers.setRegister(vx >> 8, valueX);
        registers.setRegister(vy >> 4, valueY);
        opCodes.opCode(opcode + vx + vy);

        assertEquals(carry, registers.getRegisterData(0xF));
        int expected = (((valueX - valueY) % 256) + 256) % 256;// to getCurrentCount rid of negative numbers
        if (vy >> 4 == 0xF || vx >> 8 == 0xF) {
            assertNotEquals(expected, registers.getRegisterData(vx >> 8));
        } else {
            assertEquals(expected, registers.getRegisterData(vx >> 8));
        }
    }

    private void addVyToVxWithCarryTest() {
        int opcode = 0x8004;
        int vx = 0x0000;
        int vy = 0x0010;
        short valueX = 20;
        short valueY = 249;

        addVyToVxWithCarry(opcode, vx, vy, valueX, valueY);

        vx = 0x0E00;
        vy = 0x00F0;
        valueX = 0;
        valueY = 255;
        addVyToVxWithCarry(opcode, vx, vy, valueX, valueY);

        vx = 0x0E00;
        vy = 0x00F0;
        valueX = 255;
        valueY = 254;
        addVyToVxWithCarry(opcode, vx, vy, valueX, valueY);

        vx = 0x0100;
        vy = 0x0040;
        valueX = 1;
        valueY = 0;
        addVyToVxWithCarry(opcode, vx, vy, valueX, valueY);

        vx = 0x0600;
        vy = 0x0050;
        valueX = 0;
        valueY = 1;
        addVyToVxWithCarry(opcode, vx, vy, valueX, valueY);
    }

    private void addVyToVxWithCarry(int opcode, int vx, int vy, short valueX, short valueY) {
        int carry = 0;
        if (valueY + valueX > 0xFF) {
            carry = 1;
        }

        registers.setRegister(vx >> 8, valueX);
        registers.setRegister(vy >> 4, valueY);
        opCodes.opCode(opcode + vx + vy);

        assertEquals(carry, registers.getRegisterData(0xF));
        int expected = ((valueY + valueX) % 256);
        if (vy >> 4 == 0xF || vx >> 8 == 0xF) {
            assertNotEquals(expected, registers.getRegisterData(vx >> 8));
        } else {
            assertEquals(expected, registers.getRegisterData(vx >> 8));
        }
    }

    private void shiftVxRightOneTest() {
        int opcode = 0x8006;
        int vx = 0x0000;
        short value = 0xFF;

        shiftVxRightOne(opcode, vx, value);

        vx = 0x0E00;
        value = 0x1;
        shiftVxRightOne(opcode, vx, value);

        vx = 0x0100;
        value = 0x0;
        shiftVxRightOne(opcode, vx, value);

        vx = 0x0000;
        value = 0xFE;
        shiftVxRightOne(opcode, vx, value);

        vx = 0x0000;
        value = 0xEF;
        shiftVxRightOne(opcode, vx, value);
    }

    private void shiftVxRightOne(int opcode, int vx, short value) {
        registers.setRegister(vx >> 8, value);
        opCodes.opCode(opcode + vx);
        assertEquals(value & 0x1, registers.getRegisterData(0xF));
        assertEquals((value >>> 1) & 0xFF, registers.getRegisterData(vx >> 8));
    }

    private void subtractVxToVyWithCarryTest() {
        int opcode = 0x8007;
        int vx = 0x0000;
        int vy = 0x0010;
        short valueX = 20;
        short valueY = 249;

        subtractVxToVyWithCarry(opcode, vx, vy, valueX, valueY);

        vx = 0x0E00;
        vy = 0x00F0;
        valueX = 0;
        valueY = 255;
        subtractVxToVyWithCarry(opcode, vx, vy, valueX, valueY);

        vx = 0x0E00;
        vy = 0x00F0;
        valueX = 255;
        valueY = 254;
        subtractVxToVyWithCarry(opcode, vx, vy, valueX, valueY);

        vx = 0x0100;
        vy = 0x0040;
        valueX = 1;
        valueY = 0;
        subtractVxToVyWithCarry(opcode, vx, vy, valueX, valueY);

        vx = 0x0600;
        vy = 0x0050;
        valueX = 0;
        valueY = 1;
        subtractVxToVyWithCarry(opcode, vx, vy, valueX, valueY);
    }

    private void subtractVxToVyWithCarry(int opcode, int vx, int vy, short valueX, short valueY) {
        int carry = 0;
        if (valueY > valueX) {
            carry = 1;
        }

        registers.setRegister(vx >> 8, valueX);
        registers.setRegister(vy >> 4, valueY);
        opCodes.opCode(opcode + vx + vy);

        assertEquals(carry, registers.getRegisterData(0xF));
        int expected = (((valueY - valueX) % 256) + 256) % 256;// to getCurrentCount rid of negative numbers
        if (vy >> 4 == 0xF || vx >> 8 == 0xF) {
            assertNotEquals(expected, registers.getRegisterData(vx >> 8));
        } else {
            assertEquals(expected, registers.getRegisterData(vx >> 8));
        }
    }

    private void shiftVxLeftOneTest() {
        int opcode = 0x800E;
        int vx = 0x0000;
        short value = 0xFF;

        shiftVxLeftOne(opcode, vx, value);

        vx = 0x0E00;
        value = 0x1;
        shiftVxLeftOne(opcode, vx, value);

        vx = 0x0100;
        value = 0x0;
        shiftVxLeftOne(opcode, vx, value);

        vx = 0x0000;
        value = 0xFE;
        shiftVxLeftOne(opcode, vx, value);

        vx = 0x0000;
        value = 0xEF;
        shiftVxLeftOne(opcode, vx, value);
    }

    private void shiftVxLeftOne(int opcode, int vx, short value) {
        registers.setRegister(vx >> 8, value);
        opCodes.opCode(opcode + vx);
        assertEquals(value >> 7, registers.getRegisterData(0xF));
        assertEquals((value << 1) & 0xFF, registers.getRegisterData(vx >> 8));
    }

    private void setVxToVyTest() {
        int opcode = 0x8000;
        int vx = 0x0000;
        int vy = 0x0010;
        short valueX = 20;
        short valueY = 249;

        setVxToVy(opcode, vx, vy, valueX, valueY);

        vx = 0x0E00;
        vy = 0x00F0;
        valueX = 0;
        valueY = 255;
        setVxToVy(opcode, vx, vy, valueX, valueY);

        vx = 0x0E00;
        vy = 0x00F0;
        valueX = 255;
        valueY = 254;
        setVxToVy(opcode, vx, vy, valueX, valueY);

        vx = 0x0100;
        vy = 0x0040;
        valueX = 1;
        valueY = 0;
        setVxToVy(opcode, vx, vy, valueX, valueY);

        vx = 0x0600;
        vy = 0x0050;
        valueX = 0;
        valueY = 1;
        setVxToVy(opcode, vx, vy, valueX, valueY);
    }

    private void setVxToVxOrVyTest() {
        int opcode = 0x8001;
        int vx = 0x0000;
        int vy = 0x0010;
        short valueX = 20;
        short valueY = 249;

        setVxToVxOrVy(opcode, vx, vy, valueX, valueY);

        vx = 0x0E00;
        vy = 0x00F0;
        valueX = 0;
        valueY = 255;
        setVxToVxOrVy(opcode, vx, vy, valueX, valueY);

        vx = 0x0E00;
        vy = 0x00F0;
        valueX = 255;
        valueY = 254;
        setVxToVxOrVy(opcode, vx, vy, valueX, valueY);

        vx = 0x0100;
        vy = 0x0040;
        valueX = 1;
        valueY = 0;
        setVxToVxOrVy(opcode, vx, vy, valueX, valueY);

        vx = 0x0600;
        vy = 0x0050;
        valueX = 0;
        valueY = 1;
        setVxToVxOrVy(opcode, vx, vy, valueX, valueY);
    }

    private void setVxToVxAndVyTest() {
        int opcode = 0x8002;
        int vx = 0x0000;
        int vy = 0x0010;
        short valueX = 20;
        short valueY = 249;

        setVxToVxAndVy(opcode, vx, vy, valueX, valueY);

        vx = 0x0E00;
        vy = 0x00F0;
        valueX = 0;
        valueY = 255;
        setVxToVxAndVy(opcode, vx, vy, valueX, valueY);

        vx = 0x0E00;
        vy = 0x00F0;
        valueX = 255;
        valueY = 254;
        setVxToVxAndVy(opcode, vx, vy, valueX, valueY);

        vx = 0x0100;
        vy = 0x0040;
        valueX = 1;
        valueY = 0;
        setVxToVxAndVy(opcode, vx, vy, valueX, valueY);

        vx = 0x0600;
        vy = 0x0050;
        valueX = 0;
        valueY = 1;
        setVxToVxAndVy(opcode, vx, vy, valueX, valueY);
    }

    private void setVxToVxXorVyTest() {
        int opcode = 0x8003;
        int vx = 0x0000;
        int vy = 0x0010;
        short valueX = 20;
        short valueY = 249;

        setVxToVxXorVy(opcode, vx, vy, valueX, valueY);

        vx = 0x0E00;
        vy = 0x00F0;
        valueX = 0;
        valueY = 255;
        setVxToVxXorVy(opcode, vx, vy, valueX, valueY);

        vx = 0x0E00;
        vy = 0x00F0;
        valueX = 255;
        valueY = 254;
        setVxToVxXorVy(opcode, vx, vy, valueX, valueY);

        vx = 0x0100;
        vy = 0x0040;
        valueX = 1;
        valueY = 0;
        setVxToVxXorVy(opcode, vx, vy, valueX, valueY);

        vx = 0x0600;
        vy = 0x0050;
        valueX = 0;
        valueY = 1;
        setVxToVxXorVy(opcode, vx, vy, valueX, valueY);
    }

    private void setVxToVy(int opcode, int vx, int vy, short valueX, short valueY) {
        registers.setRegister(vx >> 8, valueX);
        registers.setRegister(vy >> 4, valueY);

        opCodes.opCode(opcode + vx + vy);

        assertEquals(valueY, registers.getRegisterData(vx >> 8));
        assertEquals(valueY, registers.getRegisterData(vy >> 4));
    }

    private void setVxToVxOrVy(int opcode, int vx, int vy, short valueX, short valueY) {
        registers.setRegister(vx >> 8, valueX);
        registers.setRegister(vy >> 4, valueY);

        opCodes.opCode(opcode + vx + vy);

        assertEquals(valueX | valueY, registers.getRegisterData(vx >> 8));
        assertEquals(valueY, registers.getRegisterData(vy >> 4));
    }

    private void setVxToVxAndVy(int opcode, int vx, int vy, short valueX, short valueY) {
        registers.setRegister(vx >> 8, valueX);
        registers.setRegister(vy >> 4, valueY);

        opCodes.opCode(opcode + vx + vy);

        assertEquals(valueX & valueY, registers.getRegisterData(vx >> 8));
        assertEquals(valueY, registers.getRegisterData(vy >> 4));
    }

    private void setVxToVxXorVy(int opcode, int vx, int vy, short valueX, short valueY) {
        registers.setRegister(vx >> 8, valueX);
        registers.setRegister(vy >> 4, valueY);

        opCodes.opCode(opcode + vx + vy);

        assertEquals(valueX ^ valueY, registers.getRegisterData(vx >> 8));
        assertEquals(valueY, registers.getRegisterData(vy >> 4));
    }

    @Test
    public void Ox9VNN() {
        memory.setProgramCounter(0x200);
        int opcode = 0x9000;

        int vx = 0x0F00;
        int vy = 0x00F0;
        short vxValue = 0xFF;
        short vyValue = 0xFF;
        int expectedAddress = memory.getProgramCounter();

        testOpcode0x9(opcode, vx, vy, vxValue, vyValue, expectedAddress);

        vx = 0x0A00;
        vy = 0x00F0;
        vxValue = 0xFF;
        vyValue = 0xFF;
        expectedAddress = memory.getProgramCounter();

        testOpcode0x9(opcode, vx, vy, vxValue, vyValue, expectedAddress);

        vx = 0x0000;
        vy = 0x00F0;
        vxValue = 0xFE;
        vyValue = 0xFF;
        expectedAddress = memory.getProgramCounter() + 2;

        testOpcode0x9(opcode, vx, vy, vxValue, vyValue, expectedAddress);

        vx = 0x0000;
        vy = 0x0010;
        vxValue = 0x00;
        vyValue = 0x01;
        expectedAddress = memory.getProgramCounter() + 2;

        testOpcode0x9(opcode, vx, vy, vxValue, vyValue, expectedAddress);

        vx = 0x0000;
        vy = 0x0010;
        vxValue = 0x00;
        vyValue = 0x00;
        expectedAddress = memory.getProgramCounter();

        testOpcode0x9(opcode, vx, vy, vxValue, vyValue, expectedAddress);

        try {
            opCodes.opCode(0x900F);
            fail();
        } catch (UnknownOpcodeException ignored) {

        }
        try {
            opCodes.opCode(0x9001);
            fail();
        } catch (UnknownOpcodeException ignored) {

        }
        try {
            opCodes.opCode(0x9004);
            fail();
        } catch (UnknownOpcodeException ignored) {

        }
    }

    private void testOpcode0x9(int opcode, int vx, int vy, short vxValue, short vyValue, int expectedAddress) {
        registers.setRegister(vx >> 8, vxValue);
        registers.setRegister(vy >> 4, vyValue);
        opCodes.opCode(opcode + vx + vy);
        assertEquals(expectedAddress, memory.getProgramCounter());
    }


    @Test
    public void OxANNN() {
        int opcode = 0xA000;
        int address = 0x0FFF;

        opCodes.opCode(opcode + address);
        assertEquals(address, registers.getI());

        address = 0x0FFF;
        opCodes.opCode(opcode + address);
        assertEquals(address, registers.getI());

        address = 0x0EFF;
        opCodes.opCode(opcode + address);
        assertEquals(address, registers.getI());

        address = 0x0FFE;
        opCodes.opCode(opcode + address);
        assertEquals(address, registers.getI());

        address = 0x001;
        opCodes.opCode(opcode + address);
        assertEquals(address, registers.getI());

        address = 0x000;
        opCodes.opCode(opcode + address);
        assertEquals(address, registers.getI());
    }

    @Test
    public void OxBVNN() {
        memory.setProgramCounter(0x200);
        int opcode = 0xB000;
        int address = 0x0001;
        short v0Value = 0xFF;

        registers.setRegister(0x00, v0Value);
        opCodes.opCode(opcode + address);
        assertEquals(address + v0Value, memory.getProgramCounter());

        v0Value = 0xFE;
        registers.setRegister(0x00, v0Value);
        address = 0x000F;
        opCodes.opCode(opcode + address);
        assertEquals(address + v0Value, memory.getProgramCounter());

        v0Value = 0x01;
        registers.setRegister(0x00, v0Value);
        address = 0x0EFF;
        opCodes.opCode(opcode + address);
        assertEquals(address + v0Value, memory.getProgramCounter());

        v0Value = 0x00;
        registers.setRegister(0x00, v0Value);
        address = 0x0FFF;
        opCodes.opCode(opcode + address);
        assertEquals(address + v0Value, memory.getProgramCounter());

        v0Value = 0xEF;
        registers.setRegister(0x00, v0Value);
        address = 0x001;
        opCodes.opCode(opcode + address);
        assertEquals(address + v0Value, memory.getProgramCounter());

        v0Value = 0xFF;
        registers.setRegister(0x00, v0Value);
        address = 0x000;
        opCodes.opCode(opcode + address);
        assertEquals(address + v0Value, memory.getProgramCounter());
    }

    @Test
    public void OxCVNN() {
        memory.setProgramCounter(0x200);
        long seed = 23423414156L;
        Random rand = new Random(seed);
        opCodes.setRandom(new Random(seed));

        int opcode = 0xC000;

        int vx = 0x0F00;
        int value = 0x00FF;
        testOpcode0xC(opcode, vx, value, rand);

        vx = 0x0F00;
        value = 0x0000;
        testOpcode0xC(opcode, vx, value, rand);

        vx = 0x0A00;
        value = 0x00EF;
        testOpcode0xC(opcode, vx, value, rand);

        vx = 0x0000;
        value = 0x00FE;
        testOpcode0xC(opcode, vx, value, rand);

        vx = 0x0000;
        value = 0x0000;
        testOpcode0xC(opcode, vx, value, rand);

        vx = 0x0000;
        value = 0x0001;
        testOpcode0xC(opcode, vx, value, rand);

        vx = 0x0100;
        value = 0x0001;
        testOpcode0xC(opcode, vx, value, rand);

        vx = 0x0100;
        value = 0x0000;
        testOpcode0xC(opcode, vx, value, rand);

        vx = 0x0500;
        value = 0x0000;
        testOpcode0xC(opcode, vx, value, rand);
    }

    private void testOpcode0xC(int opcode, int vx, int value, Random random) {
        int r = random.nextInt(256) & value;
        opCodes.opCode(opcode + vx + value);
        assertEquals(r, registers.getRegisterData(vx >> 8));
    }

    @Test
    public void OxDVNN() {
        int opcode = 0xD000;
        int vx = 0x0E00;
        int vy = 0x00A0;
        int spriteHeight = 0x000F;
        int vxv = 0x0F;
        int vyv = 0x0F;

        registers.setI(0x200);

        checkD(opcode, vx, vy, spriteHeight, (short) vxv, (short) vyv);

        vx = 0x0100;
        vy = 0x0000;
        spriteHeight = 0x000F;
        vxv = 0xAF;
        vyv = 0xEF;
        checkD(opcode, vx, vy, spriteHeight, (short) vxv, (short) vyv);

        //setup for testing wrap around screen
        vx = 0x0000;
        vy = 0x0010;
        spriteHeight = 0x000F;
        vxv = 0xFF;
        vyv = 0xFF;

        display.clear();
        for (int i = registers.getI(); i < registers.getI() + spriteHeight; i++) {
            memory.setByte(i, (short) 0xFFFF);
        }

        registers.setRegister(vx >> 8, (short) vxv);
        registers.setRegister(vy >> 4, (short) vyv);

        boolean[][] dp = display.getPixels();
        boolean[][] before = new boolean[dp.length][dp[0].length];
        for (int i = 0; i < dp.length; i++) {
            for (int j = 0; j < dp[0].length; j++) {
                before[i][j] = dp[i][j];
            }
        }
        opCodes.opCode(opcode + vx + vy + spriteHeight);

        int x = registers.getRegisterData(vx >> 8);
        int y = registers.getRegisterData(vy >> 4);
        dp = display.getPixels();
        for (int i = 0; i < spriteHeight; i++) {
            for (int j = 0; j < 8; j++) {
                int i1 = (x + j) % dp.length;
                int i2 = (y + i) % dp[0].length;
                boolean b = ((memory.getByte(registers.getI() + i) >> (7 - j)) & 0x1) == 1;
                assertTrue(((dp[i1][i2] ^ b) == before[i1][i2]));
            }
        }
        //test for wrap around screen
        assertTrue(display.getPixels()[0][0]);
    }

    private void checkD(int opcode, int vx, int vy, int spriteHeight, short vxv, short vyv) {
        display.clear();
        for (int i = registers.getI(); i < registers.getI() + spriteHeight; i++) {
            if (i % 2 == 0) {
                memory.setByte(i, (short) 0xFFFF);
            } else {
                memory.setByte(i, (short) 0x0000);
            }
        }

        registers.setRegister(vx >> 8, vxv);
        registers.setRegister(vy >> 4, vyv);

        boolean[][] dp = display.getPixels();
        boolean[][] before = new boolean[dp.length][dp[0].length];
        for (int i = 0; i < dp.length; i++) {
            for (int j = 0; j < dp[0].length; j++) {
                before[i][j] = dp[i][j];
            }
        }
        opCodes.opCode(opcode + vx + vy + spriteHeight);

        int x = registers.getRegisterData(vx >> 8);
        int y = registers.getRegisterData(vy >> 4);
        dp = display.getPixels();
        for (int i = 0; i < spriteHeight; i++) {
            for (int j = 0; j < 8; j++) {
                int i1 = (x + j) % dp.length;
                int i2 = (y + i) % dp[0].length;
                boolean b = ((memory.getByte(registers.getI() + i) >> (7 - j)) & 0x1) == 1;
                assertTrue(((dp[i1][i2] ^ b) == before[i1][i2]));
            }
        }
        opCodes.opCode(opcode + vx + vy + spriteHeight);
        assertTrue(registers.getRegisterData(0xF) == 1);
    }

    @Test
    public void OxEVNN() {
        memory.setProgramCounter(0x200);
        int opcode = 0xE000;
        int opcode2 = 0x009E;

        int register = 0x0F00;
        boolean value = false;
        int address = memory.getProgramCounter();
        registers.setRegister(register >> 8, (short) (register >> 8));
        input.set(register >> 8, value);

        opCodes.opCode(opcode + register + opcode2);
        assertEquals(address, memory.getProgramCounter());

        register = 0x0000;
        value = true;
        address = memory.getProgramCounter();
        registers.setRegister(register >> 8, (short) (register >> 8));
        input.set(register >> 8, value);

        opCodes.opCode(opcode + register + opcode2);
        assertEquals(address + 2, memory.getProgramCounter());

        register = 0x0A00;
        value = true;
        address = memory.getProgramCounter();
        registers.setRegister(register >> 8, (short) (register >> 8));
        input.set(register >> 8, value);

        opCodes.opCode(opcode + register + opcode2);
        assertEquals(address + 2, memory.getProgramCounter());

        /*

         */
        opcode2 = 0x00A1;

        register = 0x0F00;
        value = false;
        address = memory.getProgramCounter();
        registers.setRegister(register >> 8, (short) (register >> 8));
        input.set(register >> 8, value);

        opCodes.opCode(opcode + register + opcode2);
        assertEquals(address + 2, memory.getProgramCounter());

        register = 0x0000;
        value = true;
        address = memory.getProgramCounter();
        registers.setRegister(register >> 8, (short) (register >> 8));
        input.set(register >> 8, value);

        opCodes.opCode(opcode + register + opcode2);
        assertEquals(address, memory.getProgramCounter());

        register = 0x0A00;
        value = true;
        address = memory.getProgramCounter();
        registers.setRegister(register >> 8, (short) (register >> 8));
        input.set(register >> 8, value);

        opCodes.opCode(opcode + register + opcode2);
        assertEquals(address, memory.getProgramCounter());

        testUnknownOpcode(0xE00E);
    }

    @Test
    public void OxFVNN() {
        setVxFromDelayTimerTest();
        waitForKeyTest();
        //set delay timer test
        setTimerTest(0xF015, delayTimer);
        //set sound timer test
        setTimerTest(0xF018, soundTimer);
        addVxToITest();
        setITOCharTest();
        bcdTest();
        storeRegistersTest();
        loadRegistersTest();
        testUnknownOpcode(0xF00E);
    }

    private void loadRegistersTest() {
        registers.setI(0x200);
        int opocode = 0xF065;
        int vx = 0x0F00;

        Random r = new Random(0);
        loadRegisters(opocode, vx, r);
        vx = 0x0E00;
        loadRegisters(opocode, vx, r);
        vx = 0x0000;
        loadRegisters(opocode, vx, r);
        vx = 0x0100;
        loadRegisters(opocode, vx, r);
    }

    private void loadRegisters(int opocode, int vx, Random r) {
        for (int i = 0; i <= 0xF; i++) {
            registers.setRegister(i, (short) 0);
        }
        for (int i = 0; i <= vx >> 8; i++) {
            memory.setByte(registers.getI() + i, (short) r.nextInt(256));
        }
        registers.setI(0x200);

        opCodes.opCode(opocode + vx);

        for (int i = 0; i <= vx >> 8; i++) {
            assertEquals(memory.getByte(registers.getI() + i), registers.getRegisterData(i));
        }
        for (int i = (vx >> 8) + 1; i <= 0xF; i++) {
            assertEquals(0, registers.getRegisterData(i));
        }
    }

    private void storeRegistersTest() {
        registers.setI(0x200);
        int opocode = 0xF055;
        int vx = 0x0F00;

        Random r = new Random(0);
        storeRegisters(opocode, vx, r);
        vx = 0x0E00;
        storeRegisters(opocode, vx, r);
        vx = 0x0000;
        storeRegisters(opocode, vx, r);
        vx = 0x0100;
        storeRegisters(opocode, vx, r);
    }

    private void storeRegisters(int opcode, int vx, Random r) {
        for (int i = 0; i <= 0xF; i++) {
            memory.setByte(registers.getI() + i, (short) 0);
        }
        for (int i = 0; i <= vx >> 8; i++) {
            registers.setRegister(i, (short) r.nextInt(256));
        }
        opCodes.opCode(opcode + vx);
        for (int i = 0; i <= vx >> 8; i++) {
            assertEquals(registers.getRegisterData(i), memory.getByte(registers.getI() + i));
        }
        for (int i = (vx >> 8) + 1; i <= 0xF; i++) {
            assertEquals(0, memory.getByte(registers.getI() + i));
        }
    }

    private void bcdTest() {
        int opcode = 0xF033;
        int vx = 0x0000;
        short value = 255;

        bcd(opcode, vx, value);

        vx = 0x0E00;
        value = 254;
        bcd(opcode, vx, value);

        vx = 0x0100;
        value = 0;
        bcd(opcode, vx, value);
    }

    private void bcd(int opcode, int vx, short value) {
        registers.setRegister(vx >> 8, value);

        opCodes.opCode(opcode + vx);

        int hundreds = value / 100;
        assertEquals(hundreds, memory.getByte(registers.getI()));
        int tens = (value - hundreds * 100) / 10;
        assertEquals(tens, memory.getByte(registers.getI() + 1));
        int ones = value - hundreds * 100 - tens * 10;
        assertEquals(ones, memory.getByte(registers.getI() + 2));
    }

    private void setITOCharTest() {
        int opcode = 0xF029;
        int vx = 0x0000;

        for (int value = 0; value < 0xF; value++) {
            registers.setRegister(vx >> 8, (short) value);

            opCodes.opCode(opcode + vx);

            assertEquals(Font.LOCATION + (value * 5), registers.getI());
        }
    }

    private void waitForKeyTest() {
        int opcode = 0xF00A;
        int vx = 0x0000;

        short key = 0xF;
        waitForKey(opcode, vx, key);

        vx = 0x0F00;
        key = 0x0;
        waitForKey(opcode, vx, key);

        vx = 0x0100;
        key = 0x1;
        waitForKey(opcode, vx, key);

        vx = 0x0E00;
        key = 0x1;
        waitForKey(opcode, vx, key);

        vx = 0x0E00;
        key = 0xE;
        waitForKey(opcode, vx, key);

        vx = 0x0F00;
        key = 0xF;
        waitForKey(opcode, vx, key);
    }

    private void waitForKey(int opcode, int vx, short key) {
        input.fireEvent(new Input.KeyEvent(key, false));
        input.fireEvent(new Input.KeyEvent(key, true));

        opCodes.opCode(opcode + vx);

        assertEquals(key, registers.getRegisterData(vx >> 8));
        assertTrue(input.get(key));
    }

    private void addVxToITest() {
        int opcode = 0xF01E;
        int vx = 0x0000;
        short value = 255;

        addVxToI(opcode, vx, value);

        vx = 0x0100;
        value = 0;
        addVxToI(opcode, vx, value);

        vx = 0x0E00;
        value = 0x0001;
        addVxToI(opcode, vx, value);

        vx = 0x0F00;
        value = 254;
        addVxToI(opcode, vx, value);

    }

    private void addVxToI(int opcode, int vx, short value) {
        registers.setRegister(vx >> 8, value);
        registers.setI(0x200);
        int expected = registers.getI() + value;
        opCodes.opCode(opcode + vx);
        assertEquals(expected, registers.getI());
    }

    private void setTimerTest(int opcode, CountdownTimer timer) {
        int vx = 0x0000;
        short value = 256;
        registers.setRegister(vx >> 8, value);
        opCodes.opCode(opcode + vx);
        assertEquals(value % 256, timer.getCurrentCount());

        vx = 0x0100;
        value = 255;
        registers.setRegister(vx >> 8, value);
        opCodes.opCode(opcode + vx);
        assertEquals(value % 256, timer.getCurrentCount());

        vx = 0x0E00;
        value = 1;
        registers.setRegister(vx >> 8, value);
        opCodes.opCode(opcode + vx);
        assertEquals(value % 256, timer.getCurrentCount());

        vx = 0x0F00;
        value = 0;
        registers.setRegister(vx >> 8, value);
        opCodes.opCode(opcode + vx);
        assertEquals(value % 256, timer.getCurrentCount());
    }

    private void setVxFromDelayTimerTest() {
        int opCode = 0xF007;
        int vx = 0x0F00;
        setVxDelayTimer((short) 60, opCode + vx, vx);

        vx = 0x0000;
        setVxDelayTimer((short) 256, opCode + vx, vx);

        vx = 0x0E00;
        setVxDelayTimer((short) 0, opCode + vx, vx);

        vx = 0x0100;
        setVxDelayTimer((short) 5, opCode + vx, vx);

        vx = 0x0E00;
        setVxDelayTimer((short) -1, opCode + vx, vx);
    }

    private void setVxDelayTimer(short time, int opCode2, int register) {
        delayTimer.set(time);
        opCodes.opCode(opCode2);
        assertEquals(registers.getRegisterData(register >> 8), delayTimer.getCurrentCount());
    }


    @Test
    public void Ox6VNN() {
        int opcode = 0x6000;
        int register = 0x0F00;
        int value = 0x00FF;

        opCodes.opCode(opcode + register + value);
        assertEquals(value, registers.getRegisterData(register >> 8));

        register = 0x0E00;
        value = 0x00FF;

        opCodes.opCode(opcode + register + value);
        assertEquals(value, registers.getRegisterData(register >> 8));

        register = 0x0000;
        value = 0x00EF;

        opCodes.opCode(opcode + register + value);
        assertEquals(value, registers.getRegisterData(register >> 8));

        register = 0x0100;
        value = 0x000F;

        opCodes.opCode(opcode + register + value);
        assertEquals(value, registers.getRegisterData(register >> 8));

        register = 0x0300;
        value = 0x0000;

        opCodes.opCode(opcode + register + value);
        assertEquals(value, registers.getRegisterData(register >> 8));

        register = 0x0200;
        value = 0x0001;

        opCodes.opCode(opcode + register + value);
        assertEquals(value, registers.getRegisterData(register >> 8));
    }

    @Test
    public void Ox7VNN() {
        int opcode = 0x7000;
        int register = 0x0F00;
        int toadd = 0x00FF;
        short init = 0x0F;

        registers.setRegister(register >> 8, init);
        opCodes.opCode(opcode + register + toadd);
        assertEquals((((toadd + init) % 256) + 256) % 256, registers.getRegisterData(register >> 8));

        register = 0x0E00;
        toadd = 0x00FF;

        init = 0xFF;
        registers.setRegister(register >> 8, init);
        opCodes.opCode(opcode + register + toadd);
        assertEquals((((toadd + init) % 256) + 256) % 256, registers.getRegisterData(register >> 8));

        register = 0x0000;
        toadd = 0x00EF;

        init = 0xEF;
        registers.setRegister(register >> 8, init);
        opCodes.opCode(opcode + register + toadd);
        assertEquals((((toadd + init) % 256) + 256) % 256, registers.getRegisterData(register >> 8));

        register = 0x0100;
        toadd = 0x000F;

        init = 0xEC;
        registers.setRegister(register >> 8, init);
        opCodes.opCode(opcode + register + toadd);
        assertEquals((((toadd + init) % 256) + 256) % 256, registers.getRegisterData(register >> 8));

        register = 0x0300;
        toadd = 0x0000;

        init = 0x01;
        registers.setRegister(register >> 8, init);
        opCodes.opCode(opcode + register + toadd);
        assertEquals((((toadd + init) % 256) + 256) % 256, registers.getRegisterData(register >> 8));

        register = 0x0200;
        toadd = 0x0001;

        init = 0xEF;
        registers.setRegister(register >> 8, init);
        opCodes.opCode(opcode + register + toadd);
        assertEquals((((toadd + init) % 256) + 256) % 256, registers.getRegisterData(register >> 8));
    }


    @Test
    public void Ox5VNN() {
        memory.setProgramCounter(0x200);
        int opcode = 0x5000;

        int vx = 0x0F00;
        int vy = 0x00F0;
        short vxValue = 0xFF;
        short vyValue = 0xFF;
        int expectedAddress = memory.getProgramCounter() + 2;

        testOpcode0x5(opcode, vx, vy, vxValue, vyValue, expectedAddress);

        vx = 0x0A00;
        vy = 0x00F0;
        vxValue = 0xFF;
        vyValue = 0xFF;
        expectedAddress = memory.getProgramCounter() + 2;

        testOpcode0x5(opcode, vx, vy, vxValue, vyValue, expectedAddress);

        vx = 0x0000;
        vy = 0x00F0;
        vxValue = 0xFE;
        vyValue = 0xFF;
        expectedAddress = memory.getProgramCounter();

        testOpcode0x5(opcode, vx, vy, vxValue, vyValue, expectedAddress);

        vx = 0x0000;
        vy = 0x0010;
        vxValue = 0x00;
        vyValue = 0x01;
        expectedAddress = memory.getProgramCounter();

        testOpcode0x5(opcode, vx, vy, vxValue, vyValue, expectedAddress);

        vx = 0x0E00;
        vy = 0x0010;
        vxValue = 0x00;
        vyValue = 0x00;
        expectedAddress = memory.getProgramCounter() + 2;

        testOpcode0x5(opcode, vx, vy, vxValue, vyValue, expectedAddress);

        try {
            opCodes.opCode(0x500F);
            fail();
        } catch (UnknownOpcodeException ignored) {

        }
        try {
            opCodes.opCode(0x5001);
            fail();
        } catch (UnknownOpcodeException ignored) {

        }
        try {
            opCodes.opCode(0x5004);
            fail();
        } catch (UnknownOpcodeException ignored) {

        }
    }

    private void testOpcode0x5(int opcode, int vx, int vy, short vxValue, short vyValue, int expectedAddress) {
        registers.setRegister(vx >> 8, vxValue);
        registers.setRegister(vy >> 4, vyValue);
        opCodes.opCode(opcode + vx + vy);
        assertEquals(expectedAddress, memory.getProgramCounter());
    }


    @Test
    public void Ox3VNN() {
        int opcode = 0x3000;
        int vx = 0x0F00;
        short vxValue = 0xFF;
        short value = 0xFF;
        memory.setProgramCounter(0x200);

        int expectedAddress = memory.getProgramCounter() + 2;

        testOpcode0x3And0x4(opcode, vx, vxValue, value, expectedAddress);

        vx = 0x0A00;
        vxValue = 0xFF;
        value = 0xFF;
        expectedAddress = memory.getProgramCounter() + 2;

        testOpcode0x3And0x4(opcode, vx, vxValue, value, expectedAddress);

        vx = 0x0000;
        vxValue = 0xFE;
        value = 0xFF;
        expectedAddress = memory.getProgramCounter();

        testOpcode0x3And0x4(opcode, vx, vxValue, value, expectedAddress);

        vx = 0x0000;
        vxValue = 0x00;
        value = 0x01;
        expectedAddress = memory.getProgramCounter();

        testOpcode0x3And0x4(opcode, vx, vxValue, value, expectedAddress);

        vx = 0x0E00;
        vxValue = 0x00;
        value = 0x00;
        expectedAddress = memory.getProgramCounter() + 2;

        testOpcode0x3And0x4(opcode, vx, vxValue, value, expectedAddress);
    }

    private void testOpcode0x3And0x4(int opcode, int vx, short vxValue, short value, int expectedAddress) {
        registers.setRegister(vx >> 8, vxValue);
        opCodes.opCode(opcode + vx + value);
        assertEquals(expectedAddress, memory.getProgramCounter());
    }

    @Test
    public void Ox4VNN() {
        int opcode = 0x4000;
        int vx = 0x0F00;
        short vxValue = 0xFF;
        short value = 0xFF;
        memory.setProgramCounter(0x200);

        int expectedAddress = memory.getProgramCounter();

        testOpcode0x3And0x4(opcode, vx, vxValue, value, expectedAddress);

        vx = 0x0A00;
        vxValue = 0xFF;
        value = 0xFF;
        expectedAddress = memory.getProgramCounter();

        testOpcode0x3And0x4(opcode, vx, vxValue, value, expectedAddress);

        vx = 0x0000;
        vxValue = 0xFE;
        value = 0xFF;
        expectedAddress = memory.getProgramCounter() + 2;

        testOpcode0x3And0x4(opcode, vx, vxValue, value, expectedAddress);

        vx = 0x0000;
        vxValue = 0x00;
        value = 0x01;
        expectedAddress = memory.getProgramCounter() + 2;

        testOpcode0x3And0x4(opcode, vx, vxValue, value, expectedAddress);

        vx = 0x0E00;
        vxValue = 0x00;
        value = 0x00;
        expectedAddress = memory.getProgramCounter();

        testOpcode0x3And0x4(opcode, vx, vxValue, value, expectedAddress);
    }


    @Test
    public void Ox0NNN() {
        int opcode = 0x00E0;
        opCodes.opCode(opcode);


        int opcode2 = 0x2000;

        int address = 0x100;
        opCodes.opCode(opcode2 + address);

        opCodes.opCode(opcode2 + 0x400);

        opcode = 0x00EE;
        opCodes.opCode(opcode);

        assertEquals(address, memory.getProgramCounter());
/**
 *
 */
        address = 0xFFF;
        opCodes.opCode(opcode2 + address);

        opCodes.opCode(opcode2 + 0x400);
        opCodes.opCode(opcode2 + 0x500);

        assertEquals(0x500, memory.getProgramCounter());
        opCodes.opCode(0x00EE);
        assertEquals(0x400, memory.getProgramCounter());
        opCodes.opCode(0x00EE);
        assertEquals(address, memory.getProgramCounter());

        testUnknownOpcode(0x0000);
    }


    @Test
    public void Ox2NNN() {
        int opcode = 0x2000;
        int address = 0x100;

        opCodes.opCode(opcode + address);
        assertEquals(address, memory.getProgramCounter());

        address = 0x300;
        opCodes.opCode(opcode + address);
        assertEquals(address, memory.getProgramCounter());

        address = 0x400;
        opCodes.opCode(opcode + address);
        assertEquals(address, memory.getProgramCounter());

        address = 0xFFF;
        opCodes.opCode(opcode + address);
        assertEquals(address, memory.getProgramCounter());

        address = 0xFFE;
        opCodes.opCode(opcode + address);
        assertEquals(address, memory.getProgramCounter());

        address = 0x000;
        opCodes.opCode(opcode + address);
        assertEquals(address, memory.getProgramCounter());

        address = 0x001;
        opCodes.opCode(opcode + address);
        assertEquals(address, memory.getProgramCounter());
    }


    @Test
    public void Ox1NNN() {
        int opcode = 0x1000;
        int address = 0x100;

        opCodes.opCode(opcode + address);
        assertEquals(address, memory.getProgramCounter());

        address = 0x300;
        opCodes.opCode(opcode + address);
        assertEquals(address, memory.getProgramCounter());

        address = 0x400;
        opCodes.opCode(opcode + address);
        assertEquals(address, memory.getProgramCounter());

        address = 0xFFF;
        opCodes.opCode(opcode + address);
        assertEquals(address, memory.getProgramCounter());

        address = 0xFFE;
        opCodes.opCode(opcode + address);
        assertEquals(address, memory.getProgramCounter());

        address = 0x000;
        opCodes.opCode(opcode + address);
        assertEquals(address, memory.getProgramCounter());

        address = 0x001;
        opCodes.opCode(opcode + address);
        assertEquals(address, memory.getProgramCounter());
//
//        try{
//            address = -0x001;
//            opCodes.opCode(opcode + address);
//            fail();
//        }catch (Exception e){
//
//        }
//        try{
//            address = 0xFFF + 1;
//            opCodes.opCode(opcode + address);
//            fail();
//        }catch (Exception e){
//
//        }
    }
}