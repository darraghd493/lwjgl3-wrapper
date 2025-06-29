package me.darragh.lwjgl;

import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;

public class Config { // TODO: Serialisation
    public static boolean GLFW_FORCE_WAYLAND = true; // Only applies to Wayland sessions on Linux & FreeBSD

    public static boolean GL_CONTEXT_BACKWARD_COMPATIBLE = false;
    public static boolean GL_CONTEXT_DEBUG = false;
    public static boolean GL_CONTEXT_NO_ERROR = false;
    public static boolean GL_CONTEXT_SRGB = false;
    public static boolean GL_DOUBLE_BUFFER = true;
    public static boolean GL_FULLSCREEN_BORDERLESS = false;
    public static boolean GL_FULLSCREEN_BORDERLESS_WINDOWS_FIX = true; // Fixes fullscreen borderless on Windows by preventing it from being treated as a fullscreen window
    public static int GL_VERSION_MAJOR = 2;
    public static int GL_VERSION_MINOR = 1;

    public static boolean AL_CUSTOM_MAX_AUX_FX = false;
    public static int AL_CUSTOM_MAX_AUX_FX_VALUE = 8;
    public static boolean AL_HRTF = true;

    // Cocoa compositor
    public static String COCOA_FRAME_NAME = "Minecraft";
    public static boolean COCOA_RETINA_FRAME_BUFFER = false;

    // X11 compositor
    public static String X11_CLASS_NAME = "Minecraft";

    // Wayland compositor
    public static String WAYLAND_APP_ID = "Minecraft";

    /**
     * Converts a boolean value to an integer representation for GLFW.
     *
     * @apiNote The method name aligns with GLFW's naming convention as it is intended to be statically imported.
     *
     * @param value The boolean value to convert.
     *
     * @return GLFW_TRUE if the value is true, otherwise GLFW_FALSE.
     */
    public static int GLFW_bool(boolean value) {
        return value ? GLFW_TRUE : GLFW_FALSE;
    }
}
