package me.darragh.lwjgl.mouse;

/**
 * Represents a mouse scroll event.
 *
 * @author darraghd493
 * @since 1.0.0
 */
public record MouseScrollEvent(double x, double y) implements MouseEvent {
    @Override
    public MouseEventType type() {
        return MouseEventType.SCROLL;
    }
}
