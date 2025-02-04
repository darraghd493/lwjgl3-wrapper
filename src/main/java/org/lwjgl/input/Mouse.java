package org.lwjgl.input;

import lombok.Getter;
import me.darragh.lwjgl.event.CircularEventQueue;
import me.darragh.lwjgl.mouse.MouseButtonEvent;
import me.darragh.lwjgl.mouse.MouseMoveEvent;
import me.darragh.lwjgl.mouse.MouseScrollEvent;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.LWJGLUtil;
import org.lwjgl.Sys;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.Display;

import static org.lwjgl.glfw.GLFW.glfwGetMouseButton;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPos;

/**
 * This is a wrapper implementation of the LWJGL2 Mouse class.
 *
 * @author darraghd493
 * @since 1.0.0
 */
public class Mouse {
    private static final CircularEventQueue EVENT_QUEUE = new CircularEventQueue(256);
    private static final MouseMoveEvent[] MOUSE_MOVE_EVENTS = new MouseMoveEvent[EVENT_QUEUE.getMaxCapacity()];
    private static final MouseButtonEvent[] MOUSE_BUTTON_EVENTS = new MouseButtonEvent[EVENT_QUEUE.getMaxCapacity()];
    private static final MouseScrollEvent[] MOUSE_SCROLL_EVENTS = new MouseScrollEvent[EVENT_QUEUE.getMaxCapacity()];
    private static final long[] MOUSE_EVENT_TIMINGS = new long[EVENT_QUEUE.getMaxCapacity()];

    /**
     * The storage of mouse coordinates is <b>lossy</b>. This is due to the original LWJGL2 implementation using integers and not doubles.
     * <p>
     * For example, if the mouse is at position (0.4, 0.6), the coordinates will be stored as (0, 1).
     * Changing this would be a breaking change and therefore will not be done.
     */
    private static int prevX, prevY;
    private static int latestX, latestY;

    // Mouse data
    /**
     * -- GETTER --
     *  Returns the absolute x position of the mouse.
     */
    @Getter
    private static int x;

    /**
     * -- GETTER --
     *  Returns the absolute y position of the mouse.
     */
    @Getter
    private static int y;

    private static int dx, dy;

    // Wheel data
    private static int dWheelX, dWheelY;

    private static boolean clipPosition;

    /**
     * -- GETTER --
     *  Returns whether the mouse is grabbed.
     */
    @Getter
    private static boolean grabbed;

    private static int ignoreNextDelta, ignoreNextMove; // Used to prevent mouse issues

    /**
     * Enqueues a mouse move event.
     *
     * @param x The x position of the mouse.
     * @param y The y position of the mouse.
     *
     * @apiNote Custom method.
     */
    public static void addMoveEvent(double x, double y) {
        if (ignoreNextMove > 0) {
            ignoreNextMove--;
            return;
        }

        // Update delta values
        dx += (int) (x - latestX);
        dy += Display.getHeight() - (int) y - latestY;

        // Keep track of event history
        prevX = latestX;
        prevY = latestY;

        latestX = (int) x;
        latestY = Display.getHeight() - (int) y;

        // Reset the delta if ignored
        if (ignoreNextDelta > 0) {
            ignoreNextDelta--;

            x = latestX;
            y = latestY;

            prevX = latestX;
            prevY = latestY;

            dx = 0;
            dy = 0;
        }

        // Enqueue event
        MOUSE_MOVE_EVENTS[EVENT_QUEUE.getWritePosition()] = new MouseMoveEvent(x, y);
        MOUSE_BUTTON_EVENTS[EVENT_QUEUE.getWritePosition()] = null;
        MOUSE_SCROLL_EVENTS[EVENT_QUEUE.getWritePosition()] = null;
        MOUSE_EVENT_TIMINGS[EVENT_QUEUE.getWritePosition()] = Sys.getTime();
        EVENT_QUEUE.push();
    }

    /**
     * Enqueues a mouse button event.
     *
     * @param button The button that was pressed or released.
     * @param state Whether the button was pressed or released.
     *
     * @apiNote Custom method.
     */
    public static void addButtonEvent(int button, boolean state) {
        MOUSE_MOVE_EVENTS[EVENT_QUEUE.getWritePosition()] = null;
        MOUSE_BUTTON_EVENTS[EVENT_QUEUE.getWritePosition()] = new MouseButtonEvent(button, state);
        MOUSE_SCROLL_EVENTS[EVENT_QUEUE.getWritePosition()] = null;
        MOUSE_EVENT_TIMINGS[EVENT_QUEUE.getWritePosition()] = Sys.getTime();
        EVENT_QUEUE.push();
    }

    /**
     * Enqueues a mouse scroll event.
     *
     * @param x The x scroll offset.
     * @param y The y scroll offset.
     *
     * @apiNote Custom method.
     */
    public static void addScrollEvent(double x, double y) {
        // Update delta values
        dWheelX += (int) x;
        dWheelY += (int) y;

        // Reset other event values
        prevX = latestX;
        prevY = latestY;

        // Enqueue event
        MOUSE_MOVE_EVENTS[EVENT_QUEUE.getWritePosition()] = null;
        MOUSE_BUTTON_EVENTS[EVENT_QUEUE.getWritePosition()] = null;
        MOUSE_SCROLL_EVENTS[EVENT_QUEUE.getWritePosition()] = new MouseScrollEvent(x, y);

        MOUSE_EVENT_TIMINGS[EVENT_QUEUE.getWritePosition()] = Sys.getTime();
        EVENT_QUEUE.push();
    }

    /**
     * Dummy method.
     */
    public static void create() {
    }

    /**
     * Dummy method.
     */
    public static void destroy() {
    }

    /**
     * Returns the button count.
     *
     * @return The button count.
     *
     * @implNote This method is not implemented. It will always return 8.
     */
    // TODO: Implement this method
    public static int getButtonCount() {
        LWJGLUtil.log("Mouse.getButtonCount() is not implemented.");
        return 8;
    }

    /**
     * Returns the button index from the button name.
     *
     * @param buttonName The button name.
     * @return The button index.
     */
    public static int getButtonIndex(String buttonName) {
        if (buttonName.matches("BUTTON[0-9]+")) {
            return Integer.parseInt(StringUtils.removeStart(buttonName, "BUTTON"));
        } else {
            return -1;
        }
    }

    /**
     * Returns the button name from the button index.
     *
     * @param button The button index.
     * @return The button name.
     */
    public static String getButtonName(int button) {
        return "BUTTON" + button;
    }

    /**
     * Returns the delta y scroll offset of the mouse.
     *
     * @return The delta y scroll offset of the mouse.
     */
    public static int getDWheel() {
        return getDWheelY();
    }

    /**
     * Returns the delta y scroll offset of the mouse.
     *
     * @return The delta y scroll offset of the mouse.
     *
     * @apiNote Custom method.
     */
    public static int getDWheelX() {
        int v = dWheelX;
        dWheelX = 0;
        return v;
    }

    /**
     * Returns the delta y scroll offset of the mouse.
     *
     * @return The delta y scroll offset of the mouse.
     *
     * @apiNote Custom method.
     */
    public static int getDWheelY() {
        int v = dWheelY;
        dWheelY = 0;
        return v;
    }

    /**
     * Returns the delta x position of the mouse.
     *
     * @return The delta x position of the mouse.
     */
    public static int getDX() {
        int v = dx;
        dx = 0;
        return v;
    }

    /**
     * Returns the delta y position of the mouse.
     *
     * @return The delta y position of the mouse.
     */
    public static int getDY() {
        int v = dy;
        dy = 0;
        return v;
    }

    /**
     * Returns the current event button.
     *
     * @return The current event button.
     */
    public static int getEventButton() {
        MouseButtonEvent event = MOUSE_BUTTON_EVENTS[EVENT_QUEUE.getReadPosition()];
        return event == null ? -1 : event.button();
    }

    /**
     * Returns the current event button state.
     *
     * @return The current event button state.
     */
    public static boolean getEventButtonState() {
        MouseButtonEvent event = MOUSE_BUTTON_EVENTS[EVENT_QUEUE.getReadPosition()];
        return event != null && event.state();
    }

    /**
     * Returns the current event delta wheel (y) position.
     *
     * @return The current event delta wheel (y) position.
     */
    public static int getEventDWheel() {
        return getEventDWheelY();
    }

    /**
     * Returns the current event delta wheel (y) position.
     *
     * @return The current event delta wheel (y) position.
     *
     * @apiNote Custom method.
     */
    public static int getEventDWheelX() {
        MouseScrollEvent event = MOUSE_SCROLL_EVENTS[EVENT_QUEUE.getReadPosition()];
        return event != null ? (int) event.x() : 0;
    }

    /**
     * Returns the current event delta wheel (y) position.
     *
     * @return The current event delta wheel (y) position.
     *
     * @apiNote Custom method.
     */
    public static int getEventDWheelY() {
        MouseScrollEvent event = MOUSE_SCROLL_EVENTS[EVENT_QUEUE.getReadPosition()];
        return event != null ? (int) event.y() : 0;
    }

    /**
     * Returns the current event delta x position of the last two events.
     *
     * @return The current event delta x position of the last two events.
     */
    public static int getEventDX() {
        MouseMoveEvent event = MOUSE_MOVE_EVENTS[EVENT_QUEUE.getReadPosition()],
                previousEvent = MOUSE_MOVE_EVENTS[EVENT_QUEUE.getLastReadPosition()];

        if (previousEvent == null) {
            return event == null ? 0 : (int) event.x();
        }

        return event == null ? 0 : (int) (event.x() - previousEvent.x());
    }

    /**
     * Returns the current event delta y position of the last two events.
     *
     * @return The current event delta y position of the last two events.
     */
    public static int getEventDY() {
        MouseMoveEvent event = MOUSE_MOVE_EVENTS[EVENT_QUEUE.getReadPosition()],
                previousEvent = MOUSE_MOVE_EVENTS[EVENT_QUEUE.getLastReadPosition()];

        if (previousEvent == null) {
            return event == null ? 0 : (int) event.y();
        }

        return event == null ? 0 : (int) (event.y() - previousEvent.y());
    }

    /**
     * Returns the time in nanoseconds of the latest event.
     *
     * @return The time in nanoseconds of the latest event.
     */
    public static long getEventNanoseconds() {
        return MOUSE_EVENT_TIMINGS[EVENT_QUEUE.getReadPosition()];
    }

    /**
     * Returns the latest x position of the mouse.
     *
     * @return The latest x position of the mouse.
     */
    public static int getEventX() {
        return MOUSE_MOVE_EVENTS[EVENT_QUEUE.getReadPosition()] == null ? 0 : (int) MOUSE_MOVE_EVENTS[EVENT_QUEUE.getReadPosition()].x();
    }

    /**
     * Returns the latest y position of the mouse.
     *
     * @return The latest y position of the mouse.
     */
    public static int getEventY() {
        return MOUSE_MOVE_EVENTS[EVENT_QUEUE.getReadPosition()] == null ? 0 : (int) MOUSE_MOVE_EVENTS[EVENT_QUEUE.getReadPosition()].y();
    }

    /**
     * Returns the current natively bound cursor.
     *
     * @return The current natively bound cursor.
     */
    public static Cursor getNativeCursor() {
        LWJGLUtil.log("Mouse.getNativeCursor() is not implemented.");
        return null;
    }

    /**
     * Returns whether the mouse has wheel support.
     *
     * @return Whether the mouse has wheel support.
     *
     * @implNote This method is not implemented. It will always return true.
     */
    // TODO: Implement this method
    public static boolean hasWheel() {
        LWJGLUtil.log("Mouse.hasWheel() is not implemented.");
        return true;
    }

    /**
     * Returns whether a mouse button is down.
     *
     * @param button The button to check.
     * @return Whether the button is down.
     */
    public static boolean isButtonDown(int button) {
        return glfwGetMouseButton(Display.getWindow(), button) == GLFW.GLFW_PRESS;
    }

    /**
     * Returns whether the mouse is clipped to the window.
     *
     * @return Whether the mouse is clipped to the window.
     */
    public static boolean isClipMouseCoordinatesToWindow() {
        return clipPosition;
    }

    /**
     * Dummy method.
     * <p>
     * Returns whether the <b>display</b> is created.
     *
     * @return Whether the display is created.
     *
     * @implNote This method is not implemented. It will return whether the window is visible or not.
     */
    public static boolean isCreated() {
        LWJGLUtil.log("Mouse.isCreated() is not implemented.");
        return Display.isCreated();
    }

    /**
     * Returns whether the mouse is inside the window.
     *
     * @return Whether the mouse is inside the window.
     *
     * @implNote This method is not implemented. It will return whether the window is visible or not.
     */
    // TODO: Implement this method
    public static boolean isInsideWindow() {
        LWJGLUtil.log("Mouse.isInsideWindow() is not implemented.");
        return Display.isVisible();
    }

    /**
     * Moves on to the next event in the queue.
     *
     * @return Whether there are more events in the queue.
     */
    public static boolean next() {
        return EVENT_QUEUE.next();
    }

    /**
     * Dummy method.
     * <p>
     * Forcefully updates the mouse position to match the latest event.
     */
    public static void poll() {
        if (!grabbed && clipPosition) {
            latestX = Math.min(Math.max(latestX, 0), Display.getWidth() - 1);
            latestY = Math.min(Math.max(latestY, 0), Display.getHeight() - 1);
        }

        x = latestX;
        y = latestY;
    }

    /**
     * Sets the clip position state.
     *
     * @param clip Whether to clip the mouse position to the window.
     */
    public static void setClipMouseCoordinatesToWindow(boolean clip) {
        clipPosition = clip;
    }

    /**
     * Sets the position of the cursor.
     *
     * @param x The x position of the cursor.
     * @param y The y position of the cursor.
     */
    public static void setCursorPosition(int x, int y) {
        glfwSetCursorPos(Display.getWindow(), x, y);
        addMoveEvent(x, y);
    }

    /**
     * Sets whether the mouse is grabbed.
     *
     * @param grabbed Whether the mouse is grabbed.
     */
    public static void setGrabbed(boolean grabbed) {
        if (grabbed == Mouse.grabbed) {
            return;
        }

        GLFW.glfwSetInputMode(Display.getWindow(), GLFW.GLFW_CURSOR, grabbed ? GLFW.GLFW_CURSOR_DISABLED : GLFW.GLFW_CURSOR_NORMAL);
        Mouse.grabbed = grabbed;
        if (!grabbed) {
            ignoreNextMove += 2;
            setCursorPosition(Display.getWidth() / 2, Display.getHeight() / 2);
            latestX = Display.getWidth() / 2;
            latestY = Display.getHeight() / 2;
            prevX = latestX;
            prevY = latestY;
            x = latestX;
            y = latestY;
            addButtonEvent(-1, false); // This is a hacky solution to cause the game to get the correct mouse position if no new events are fired
        } else {
            ignoreNextDelta++; // Prevent camera rapidly rotating when closing GUIs.
            dx = 0;
            dy = 0;
        }
    }

    /**
     * Sets the native cursor.
     *
     * @param cursor The cursor to set.
     *
     * @implNote This method is not implemented.
     */
    public static void setNativeCursor(Cursor cursor) {
        LWJGLUtil.log("Mouse.setNativeCursor(Cursor) is not implemented.");
    }

    /**
     * Updates the native cursor.
     *
     * @implNote This method is not implemented.
     */
    public static void updateCursor() {
        LWJGLUtil.log("Mouse.setNativeCursor() is not implemented.");
    }
}
