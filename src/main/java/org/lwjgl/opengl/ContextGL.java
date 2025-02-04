package org.lwjgl.opengl;

import org.lwjgl.LWJGLException;
import org.lwjgl.PointerBuffer;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Stores the GLFW window and shared context information.
 *
 * @author darraghd493
 * @since 1.0.0
 */
public class ContextGL implements Context {
    public long glfwWindow;
    public final boolean shared;

    public ContextGL(long glfwWindow, boolean shared) {
        this.glfwWindow = glfwWindow;
        this.shared = shared;
    }

    /**
     * Releases the current context.
     *
     * @throws LWJGLException Never thrown.
     */
    public void releaseCurrent() throws LWJGLException {
        glfwMakeContextCurrent(0);
        GL.setCapabilities(null);
    }

    /**
     * Dummy method.
     */
    public synchronized void releaseDrawable() throws LWJGLException {}

    /**
     * Dummy method.
     */
    public synchronized void update() {}

    /**
     * Swaps the buffers.
     *
     * @throws LWJGLException Never thrown.
     */
    public static void swapBuffers() throws LWJGLException {
        glfwSwapBuffers(Display.getWindow());
    }

    /**
     * Makes the context current.
     *
     * @throws LWJGLException Never thrown.
     */
    public synchronized void makeCurrent() throws LWJGLException {
        glfwMakeContextCurrent(glfwWindow);
        GL.createCapabilities();
    }

    /**
     * Checks if the context is current.
     *
     * @return Whether the context is current.
     *
     * @throws LWJGLException Never thrown.
     */
    public synchronized boolean isCurrent() throws LWJGLException {
        return glfwGetCurrentContext() == glfwWindow;
    }

    /**
     * Sets the swap interval.
     *
     * @param value The value to set the swap interval to.
     */
    public static void setSwapInterval(int value) {
        glfwSwapInterval(value);
    }

    /**
     * Forces the context to be destroyed.
     *
     * @throws LWJGLException Never thrown.
     */
    public synchronized void forceDestroy() throws LWJGLException {
        destroy();
    }

    /**
     * Destroys the context.
     *
     * @throws LWJGLException Never thrown.
     */
    public synchronized void destroy() throws LWJGLException {
        if (shared && glfwWindow > 0) {
            glfwDestroyWindow(glfwWindow);
            glfwWindow = -1;
        }
    }

    /**
     * Dummy method.
     */
    public synchronized void setCLSharingProperties(final PointerBuffer properties) throws LWJGLException {}
}
