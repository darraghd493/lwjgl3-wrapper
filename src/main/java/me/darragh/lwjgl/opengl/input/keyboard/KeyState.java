package me.darragh.lwjgl.opengl.input.keyboard;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Represents the state of a key pressed.
 *
 * @author darraghd493
 * @since 1.0.0
 */
@Getter
@RequiredArgsConstructor
public enum KeyState {
    PRESS(true),
    RELEASE(false),
    REPEAT(true);

    private final boolean pressed;
}
