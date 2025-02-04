package me.darragh.lwjgl.mouse;

/**
 * Represents a mouse move event.
 *
 * @author darraghd493
 * @since 1.0.0
 */
public record MouseMoveEvent(double x, double y) implements MouseEvent {
    @Override
    public MouseEventType type() {
        return MouseEventType.MOVE;
    }
}
