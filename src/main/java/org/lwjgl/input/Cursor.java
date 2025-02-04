package org.lwjgl.input;

import java.nio.IntBuffer;

import org.lwjgl.LWJGLException;

/**
 * This is a stub implementation of the LWJGL Cursor class.
 * This class is not implemented.
 *
 * @author darraghd493
 * @since 1.0.0
 */
public class Cursor {
    private static final String UNSUPPORTED_OPERATION_EXCEPTION_MESSAGE = "This class is not implemented. It serves as a stub for the LWJGL API.";

    public Cursor(int width, int height, int xHotspot, int yHotspot, int numImages, IntBuffer images, IntBuffer delays) throws LWJGLException {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_EXCEPTION_MESSAGE);
    }

    /**
     * Dummy method.
     *
     * @throws UnsupportedOperationException Always thrown.
     */
    public static int getMinCursorSize() {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_EXCEPTION_MESSAGE);
    }

    /**
     * Dummy method.
     *
     * @throws UnsupportedOperationException Always thrown.
     */
    public static int getMaxCursorSize() {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_EXCEPTION_MESSAGE);
    }

    /**
     * Dummy method.
     *
     * @throws UnsupportedOperationException Always thrown.
     */
    public static int getCapabilities() {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_EXCEPTION_MESSAGE);
    }

    /**
     * Dummy method.
     *
     * @throws UnsupportedOperationException Always thrown.
     */
    private static CursorElement[] createCursors(int width, int height, int xHotspot, int yHotspot, int numImages, IntBuffer images, IntBuffer delays) throws LWJGLException {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_EXCEPTION_MESSAGE);
    }

    /**
     * Dummy method.
     *
     * @throws UnsupportedOperationException Always thrown.
     */
    private static void convertARGBtoABGR(IntBuffer imageBuffer) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_EXCEPTION_MESSAGE);
    }

    /**
     * Dummy method.
     *
     * @throws UnsupportedOperationException Always thrown.
     */
    private static void flipImages(int width, int height, int numImages, IntBuffer images, IntBuffer images_copy) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_EXCEPTION_MESSAGE);
    }

    /**
     * Dummy method.
     *
     * @throws UnsupportedOperationException Always thrown.
     */
    private static void flipImage(int width, int height, int start_index, IntBuffer images, IntBuffer images_copy) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_EXCEPTION_MESSAGE);
    }

    /**
     * Dummy method.
     *
     * @throws UnsupportedOperationException Always thrown.
     */
    Object getHandle() {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_EXCEPTION_MESSAGE);
    }

    /**
     * Dummy method.
     *
     * @throws UnsupportedOperationException Always thrown.
     */
    private void checkValid() {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_EXCEPTION_MESSAGE);
    }

    /**
     * Dummy method.
     *
     * @throws UnsupportedOperationException Always thrown.
     */
    public void destroy() {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_EXCEPTION_MESSAGE);
    }

    /**
     * Dummy method.
     *
     * @throws UnsupportedOperationException Always thrown.
     */
    protected void setTimeout() {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_EXCEPTION_MESSAGE);
    }

    /**
     * Dummy method.
     *
     * @throws UnsupportedOperationException Always thrown.
     */
    protected boolean hasTimedOut() {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_EXCEPTION_MESSAGE);
    }

    /**
     * Dummy method.
     *
     * @throws UnsupportedOperationException Always thrown.
     */
    protected void nextCursor() {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_EXCEPTION_MESSAGE);
    }

    /**
     * Dummy class for CursorElement.
     */
    private static class CursorElement {
        final Object cursorHandle;
        final long delay;
        long timeout;

        CursorElement(Object cursorHandle, long delay) {
            throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_EXCEPTION_MESSAGE);
        }
    }
}