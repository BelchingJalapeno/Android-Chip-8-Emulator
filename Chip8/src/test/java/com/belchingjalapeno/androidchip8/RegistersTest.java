package com.belchingjalapeno.androidchip8;

import com.belchingjalapeno.androidchip8.chip8.Memory;
import com.belchingjalapeno.androidchip8.chip8.Registers;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 *
 */
public class RegistersTest {

    private Registers r;
    private Memory m;

    @Before
    public void setUp() throws Exception {
        r = new Registers();
        m = new Memory();
        m.initialize();
    }

    @Test
    public void testGetRegisterData() throws Exception {
        int register;
        try {
            register = 0x10;
            r.getRegisterData(register);
            fail();
        } catch (Exception e) {
        }

        try {
            register = -0x01;
            r.getRegisterData(register);
            fail();
        } catch (Exception e) {
        }
        try {
            register = -0x0F;
            r.getRegisterData(register);
            fail();
        } catch (Exception e) {
        }
        try {
            register = -0x0E;
            r.getRegisterData(register);
            fail();
        } catch (Exception e) {
        }
    }

    @Test
    public void testSetI() throws Exception {
        short[] values = {0xFFF + 1, 0xFF, 0xFFF, 0x0 - 1, 0x00};
        short[] expectedValues = {0, 0xFF, 0xFFF, 0xFFF, 0x00};
        for (int i = 0; i < values.length; i++) {
            r.setI(values[i]);
            assertEquals(expectedValues[i], r.getI());
        }
    }

    @Test
    public void testSetRegister() throws Exception {
        short value = (short) 3245;
        int register = 0x00;
        r.setRegister(register, value);

        value = (short) 33245;
        register = 0x0F;
        r.setRegister(register, value);
        if (value < 0) {
            assertEquals(r.getRegisterData(register), 256 + (value % 256));
        } else {
            assertEquals(r.getRegisterData(register), value % 256);
        }

        try {
            value = (short) 33245;
            register = 0x10;
            r.setRegister(register, value);
            fail();
        } catch (Exception e) {
        }

        try {
            value = (short) 33245;
            register = -0x01;
            r.setRegister(register, value);
            fail();
        } catch (Exception e) {
        }
        try {
            value = (short) 33245;
            register = -0x0F;
            r.setRegister(register, value);
            fail();
        } catch (Exception e) {
        }
        try {
            value = (short) 33245;
            register = -0x0E;
            r.setRegister(register, value);
            fail();
        } catch (Exception e) {
        }
    }
}