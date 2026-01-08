package me.darragh.lwjgl.opengl.input.mouse;

import lombok.Value;
import lombok.experimental.Accessors;

/**
 * Represents a mouse scroll event.
 *
 * @author darraghd493
 * @since 1.0.0
 */
@Value
@Accessors(fluent = true)
public class MouseScrollEvent implements MouseEvent {
    double x, y;

    @Override
    public MouseEventType type() {
        return MouseEventType.SCROLL;
    }
}
