package com.belchingjalapeno.androidchip8.chip8;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Used to interact with the Chip-8 system.
 */
public class Input {

    private final boolean[] keys = new boolean[16];
    private final Deque<Input.KeyEvent> que = new ArrayDeque<>();

    /**
     * Empties the {@link KeyEvent} que and sets the key states.
     */
    public void process() {
        KeyEvent event;
        while ((event = que.poll()) != null) {
            try {
                set(event.key, event.state);
            } catch (IndexOutOfBoundsException ignored) {
                //catch all KeyEvents that are out of bounds, and do nothing with them, but keep processing the
                //rest of the KeyEvents in the que
            }
        }
    }

    /**
     * Waits for a key to be pressed down and returns it. If the {@link KeyEvent} que isn't empty,
     * it checks there first.
     *
     * @return the key that was pressed
     */
    public synchronized short waitForKey() {
        KeyEvent event;
        while ((event = que.poll()) == null || !event.state) {
            if (event != null) {
                set(event.key, event.state);
            } else {
                try {
                    this.wait();
                } catch (InterruptedException ignored) {
                }
            }
        }
        set(event.key, event.state);
        return event.key;
    }

    public void set(int key, boolean state) {
        checkOutOfBounds(key);
        keys[key] = state;
    }

    /**
     * Checks the state of a key to tell if it is pressed or not.
     *
     * @param key the key to check the state of
     * @return the state of the key
     */
    public boolean get(short key) {
        checkOutOfBounds(key);
        return keys[key];
    }

    /**
     * Adds a {@link KeyEvent} to the que to be {@link #process()}
     *
     * @param event
     */
    public synchronized void fireEvent(KeyEvent event) {
        this.que.add(event);
        this.notifyAll();
    }

    private void checkOutOfBounds(int key) {
        if (key < 0 || key > 0xF) {
            throw new IndexOutOfBoundsException("key out of bounds : 0x" + Integer.toHexString(key));
        }
    }

    public static class KeyEvent {
        public short key;
        public boolean state;

        public KeyEvent() {
        }

        public KeyEvent(short key, boolean state) {
            this.key = key;
            this.state = state;
        }
    }
}