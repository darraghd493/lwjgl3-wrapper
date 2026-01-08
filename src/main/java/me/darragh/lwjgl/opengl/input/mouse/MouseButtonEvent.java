package me.darragh.lwjgl.opengl.input.mouse;

import lombok.Value;
import lombok.experimental.Accessors;

/**
 * Represents a mouse move event.
 *
 * @author darraghd493
 * @since 1.0.0
 */
@Value
@Accessors(fluent = true)
public class MouseButtonEvent implements MouseEvent {
    int button;
    boolean state;

    @Override
    public MouseEventType type() {
        return MouseEventType.BUTTON;
    }
}
