package me.darragh.lwjgl.opengl.input.keyboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Represents a key event.
 *
 * @author darraghd493
 * @since 1.0.0
 */
@Data
@Accessors(fluent = true) // To mimic record-like access
@AllArgsConstructor
public class KeyEvent {
    private int key;
    private char keyChar;
    private KeyState state;
    private final long timePressed;
    private boolean outOfOrder; // Designates the release being out of order

    public KeyEvent(int key, char keyChar, KeyState state, long timePressed) {
        this(key, keyChar, state, timePressed, false);
    }

    public KeyEvent copy() {
        return new KeyEvent(key, keyChar, state, timePressed, outOfOrder);
    }
}
