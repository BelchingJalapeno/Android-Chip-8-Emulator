package com.belchingjalapeno.androidchip8.chip8.timers;

import com.belchingjalapeno.androidchip8.Util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A timer that counts down at a fixed rate of 60 hz.  The highest number this timer can count down from is
 * 255.  It will stop counting down once the count hits 0 and will start again after the count is set higher than
 * 0 via {@link #set(short)}.
 */
public class CountdownTimer {

    //This is what is used to count down at a 60 hz rate.
    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    //used to let listener know when the timer counts down
    private CountdownTimerListener tickEventListener;

    /**
     * Holds the number we are counting down from. This number is only decreased if it is > 0 and has a
     * max value of 255.
     */
    private short count = 0;

    /**
     * @see CountdownTimer
     */
    public CountdownTimer() {
    }

    /**
     * @param handler used to be notified every time the {@link CountdownTimer} counts down.
     * @see CountdownTimer
     */
    public CountdownTimer(CountdownTimerListener handler) {
        this.tickEventListener = handler;
    }

    /**
     * Starts this {@link CountdownTimer}, running at a rate of 60 hz.  This timer will only stop after
     * {@link #stop()} is called or if {@link CountdownTimerListener#onCountdown(int)} throws an exception.  After
     * this {@link CountdownTimer} has truly stopped, {@link CountdownTimer#isRunning()} will return {@code false}.
     */
    public void start() {
        final CountdownTimerTask task = new CountdownTimerTask(this, tickEventListener);
        scheduledExecutorService.scheduleAtFixedRate(task, 0, 1000 / 60, TimeUnit.MILLISECONDS);
    }

    /**
     * Stops this {@link CountdownTimer}.  The {@link CountdownTimer} internal timer thread stops the next time it is
     * ran, which should be within the range of 0-17 ms.
     */
    public void stop() {
        scheduledExecutorService.shutdownNow();
    }

    /**
     * Sets this timers current count, allowing it to restart/continue counting down.
     *
     * @param count the number to count down from, in the range of 0 - 255, wraps around if out of this range
     */
    public void set(short count) {
        this.count = Util.wrapToByte(count);
    }

    /**
     * Gets the current count from this {@link CountdownTimer}
     *
     * @return the current count, with a range of 0 - 255
     */
    public short getCurrentCount() {
        return count;
    }

    /**
     * Check to see if this {@link CountdownTimer} is running, may still return {@code true} for up to 17 ms after
     * {@link CountdownTimer#stop()} is called, as that is how long it may take to truly be stopped.
     *
     * @return if this {@link CountdownTimer} is currently running
     */
    public boolean isRunning() {
        return !scheduledExecutorService.isShutdown();
    }

    //this is where the actual counting down goes along with the calling of the eventListener
    //not private so we can test this
    static class CountdownTimerTask implements Runnable {

        CountdownTimer countdownTimer;
        CountdownTimerListener tickEventListener;

        CountdownTimerTask(CountdownTimer countdownTimer, CountdownTimerListener tickEventListener) {
            this.countdownTimer = countdownTimer;
            this.tickEventListener = tickEventListener;
        }

        @Override
        public void run() {
            //prevents the count going less than 0
            if (countdownTimer.count > 0) {
                countdownTimer.count--;
                if (tickEventListener != null) {
                    tickEventListener.onCountdown(countdownTimer.count);
                }
            }
        }
    }
}
