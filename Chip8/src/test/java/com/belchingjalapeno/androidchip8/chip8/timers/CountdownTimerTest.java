package com.belchingjalapeno.androidchip8.chip8.timers;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class CountdownTimerTest {

    private CountdownTimer timer;

    @Before
    public void setUp() throws Exception {
        timer = new CountdownTimer();
    }

    @After
    public void tearDown() throws Exception {
        timer.stop();
    }

    @Test
    public void testStart() throws Exception {
        timer.start();
        assertTrue(timer.isRunning());
    }

    @Test
    public void testStop() throws Exception {
        timer.start();
        assertTrue(timer.isRunning());
        timer.stop();
        assertFalse(timer.isRunning());
    }

    @Test
    public void testSetAndGet() throws Exception {
        short[] testVal = {255, 256, 0, 1, -1, 127, 128};
        short[] expected = {255, 0, 0, 1, 255, 127, 128};
        for (int i = 0; i < testVal.length; i++) {
            timer.set(testVal[i]);
            assertEquals(expected[i], timer.getCurrentCount());
        }
    }

    @Test
    public void testTask() throws Exception {
        short[] testVal = {15, 1, 256, 255, 0, 258, -1};
        short[] testIter = {14, 1, 10, 200, 0, 1, 250};
        short[] testExpected = {1, 0, 0, 55, 0, 1, 5};
        final CountdownTimerListener handler = new CountdownTimerListener() {
            @Override
            public void onCountdown(int count) {
            }
        };
        timer = new CountdownTimer(handler);

        final CountdownTimer.CountdownTimerTask countdownTimerTask = new CountdownTimer.CountdownTimerTask(timer, handler);
        assertEquals(handler, countdownTimerTask.tickEventListener);
        assertEquals(timer, countdownTimerTask.countdownTimer);
        for (int i = 0; i < testVal.length; i++) {
            timer.set(testVal[i]);
            for (int j = 0; j < testIter[i]; j++) {
                countdownTimerTask.run();
            }
            assertEquals(testExpected[i], timer.getCurrentCount());
        }
    }
}
