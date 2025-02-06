package org.lwjgl;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.Display;
import org.lwjgl.system.Platform;

import javax.swing.*;
import java.awt.*;
import java.net.URI;

import static org.lwjgl.glfw.GLFW.glfwInit;

/**
 * This is a wrapper implementation of the LWJGL2 Sys class.
 * <p>
 * This class provides a set of utility methods for interacting with the system.
 *
 * @author darraghd493
 * @since 1.0.0
 */
public class Sys {
    static {
        if (!glfwInit()) throw new IllegalStateException("Unable to initialize glfw");
    }
    /**
     * Summons a simple alert dialog.
     *
     * @param title The title of the dialog.
     * @param message The message to display.
     */
    public static void alert(String title, String message) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            LWJGLUtil.log("Caught exception while setting LAF: " + e);
        }
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Gets the contents of the clipboard.
     *
     * @return The contents of the clipboard.
     */
    public static String getClipboard() {
        return GLFW.glfwGetClipboardString(Display.getWindow());
    }

    /**
     * Returns the current time in seconds.
     *
     * @return The current time in seconds.
     */
    public static long getTime() {
        return (long) GLFW.glfwGetTime();
    }

    /**
     * Returns the current time in nanoseconds.
     *
     * @return The current time in nanoseconds.
     *
     * @apiNote Custom method.
     */
    public static long getNanoTime() {
        return (long) GLFW.glfwGetTime() * 1000L * 1000L * 1000L;
    }

    /**
     * Returns the hi-resolution timer resolution.
     *
     * @return The hi-resolution timer resolution.
     */
    public static long getTimerResolution() {
        return GLFW.glfwGetTimerFrequency();
    }

    /**
     * Returns the current LWJGL version.
     *
     * @return The current LWJGL version.
     */
    public static String getVersion() {
        return Version.getVersion();
    }

    /**
     * Dummy method. Used to call the static initialiser.
     * <p>
     * GLFW is initialized in the static block above.
     */
    public static void initialize() {}

    /**
     * Identifies whether the platform is 64-bit.
     *
     * @return Whether the platform is 64-bit.
     */
    public static boolean is64Bit() {
        return Platform.getArchitecture().toString().endsWith("64");
    }

    /**
     * Opens a URL in the default browser.
     *
     * @param url The URL to open.
     */
    public static void openURL(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
