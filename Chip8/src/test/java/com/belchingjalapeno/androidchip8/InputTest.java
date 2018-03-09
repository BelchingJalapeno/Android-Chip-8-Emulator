package com.belchingjalapeno.androidchip8;

import com.belchingjalapeno.androidchip8.chip8.Input;

import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 *
 */
public class InputTest {

    private final Input input = new Input();

    @Test
    public void testProcess() throws Exception {
        testFireEvent();
        try {
            input.process();
        } catch (Exception ignored) {
            fail();
        }

        for (int i = 0x00; i <= 0xF; i++) {
            boolean state = true;
            input.fireEvent(new Input.KeyEvent((short) i, state));
            try {
                input.process();
                assertEquals(input.get((short) i), state);
            } catch (RuntimeException e) {
                fail();
            }
        }

        for (int i = 0x00; i <= 0xF; i++) {
            boolean state = false;
            input.fireEvent(new Input.KeyEvent((short) i, state));
            try {
                input.process();
                assertEquals(input.get((short) i), state);
            } catch (RuntimeException e) {
                fail();
            }
        }

        Random r = new Random(0);
        for (int i = 0x00; i <= 0xF; i++) {
            boolean state = r.nextBoolean();
            input.fireEvent(new Input.KeyEvent((short) i, state));
            try {
                input.process();
                assertEquals(input.get((short) i), state);
            } catch (RuntimeException e) {
                fail();
            }
        }

        int i = -0x1;
        boolean state = r.nextBoolean();
        input.fireEvent(new Input.KeyEvent((short) i, state));
        try {
            input.process();
            assertEquals(input.get((short) i), state);
            fail();
        } catch (RuntimeException ignored) {
        }

        i = 0x10;
        state = r.nextBoolean();
        input.fireEvent(new Input.KeyEvent((short) i, state));
        try {
            input.process();
            assertEquals(input.get((short) i), state);
            fail();
        } catch (RuntimeException ignored) {
        }
    }

    @Test
    public void testWaitForKey() throws Exception {
        final Thread interupt = Thread.currentThread();
        input.set((short) 0x00, true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                assertTrue(input.get((short) 0x00));
                interupt.interrupt();
                input.fireEvent(new Input.KeyEvent((short) 0x00, false));
                interupt.interrupt();
                input.fireEvent(new Input.KeyEvent((short) 0x01, true));
            }
        }).start();
        input.waitForKey();
        assertFalse(input.get((short) 0x00));
        assertTrue(input.get((short) 0x01));
    }

    @Test
    public void testSet() throws Exception {
        try {
            for (int i = 0x00; i <= 0xF; i++) {
                input.set(i, true);
            }
            for (int i = 0x00; i <= 0xF; i++) {
                input.set(i, true);
            }
            for (int i = 0x00; i <= 0xF; i++) {
                input.set(i, false);
            }
            for (int i = 0x00; i <= 0xF; i++) {
                input.set(i, false);
            }
            for (int i = 0x00; i <= 0xF; i++) {
                input.set(i, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            assertFalse(e.getMessage(), true);
        }
        try {
            input.set(0x10, true);
            fail();
        } catch (RuntimeException ignored) {
        }
        try {
            input.set(-0x1, true);
            fail();
        } catch (RuntimeException ignored) {
        }
    }

    @Test
    public void testGet() throws Exception {
        try {
            for (int i = 0x00; i <= 0xF; i++) {
                input.set(i, true);
                assertTrue(input.get((short) i));
            }
            for (int i = 0x00; i <= 0xF; i++) {
                input.set(i, true);
                assertTrue(input.get((short) i));
            }
            for (int i = 0x00; i <= 0xF; i++) {
                input.set(i, false);
                assertFalse(input.get((short) i));
            }
            for (int i = 0x00; i <= 0xF; i++) {
                input.set(i, false);
                assertFalse(input.get((short) i));
            }
            for (int i = 0x00; i <= 0xF; i++) {
                input.set(i, true);
                assertTrue(input.get((short) i));
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
        try {
            input.get((short) 0x10);
            fail();
        } catch (RuntimeException ignored) {
        }
        try {
            input.get((short) -0x1);
            fail();
        } catch (RuntimeException ignored) {
        }
    }

    @Test
    public void testFireEvent() throws Exception {
        try {
            Input.KeyEvent event = new Input.KeyEvent();
            event.key = 0x00;
            event.state = true;
            input.fireEvent(event);
            event = new Input.KeyEvent();
            event.key = 0x0F;
            event.state = false;
            input.fireEvent(event);
            event = new Input.KeyEvent();
            event.key = 0x10;
            event.state = false;
            input.fireEvent(event);
            event = new Input.KeyEvent();
            event.key = -0x1;
            event.state = true;
            input.fireEvent(event);
        } catch (Exception e) {
            fail();
        }
    }
}
