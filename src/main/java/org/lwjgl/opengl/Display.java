package org.lwjgl.opengl;

import lombok.Getter;
import me.darragh.lwjgl.Config;
import me.darragh.lwjgl.opengl.input.keyboard.KeyCodeUtil;
import me.darragh.lwjgl.opengl.input.keyboard.KeyEvent;
import me.darragh.lwjgl.opengl.input.keyboard.KeyState;
import org.jspecify.annotations.Nullable;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLUtil;
import org.lwjgl.PointerBuffer;
import org.lwjgl.Sys;
import org.lwjgl.glfw.*;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.nio.ByteBuffer;
import java.util.Objects;

import static me.darragh.lwjgl.Config.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL43.*;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * This is a wrapper implementation of the LWJGL2 Display class.
 *
 * @author darraghd493
 * @since 1.0.0
 */
public class Display {
    /**
     * -- GETTER --
     *  Returns the window title.
     */
    @Getter
    private static String title = "LWJGL Display";

    private static int width = 854,
            height = 480;

    /**
     * -- GETTER --
     * Returns whether the display window is in fullscreen mode.
     *
     * @apiNote Custom method.
     */
    @Getter
    private static boolean resizable = true;

    /**
     * -- GETTER --
     * Returns whether the display window is in fullscreen mode.
     */
    @Getter
    private static boolean fullscreen = false;

    /**
     * -- GETTER --
     * Returns the drawable instance.
     */
    @Getter
    @Nullable
    private static Drawable drawable = null;

    @SuppressWarnings("DataFlowIssue")
    @Nullable
    private static ByteBuffer[] icons = null;

    // Window data
    /**
     * -- GETTER --
     *  Returns the initial desktop display mode.
     */
    @Getter
    private static DisplayMode desktopDisplayMode;

    /**
     * -- GETTER --
     *  Returns the current display mode.
     */
    @Getter
    private static DisplayMode displayMode = new DisplayMode(width, height);

    private static boolean displayCreated, displayFocused = true,
            displayVisible, displayResized, displayDirty;

    private static int displayX, displayY,
            displayWidth, displayHeight;

    private static int savedDisplayX, savedDisplayY,
            savedDisplayWidth, savedDisplayHeight;

    // TODO: Remove? Only set; internal tracking
    private static int displayFramebufferWidth,
            displayFramebufferHeight;

    private static boolean latestResized;
    private static int latestWidth, latestHeight;

    // Callback data
    private static boolean cancelNextChar;

    @Nullable
    private static KeyEvent ingredientKeyEvent;

    public Display() {
        throw new UnsupportedOperationException("This class cannot be instantiated. Please use Display.create().");
    }

    static {
        Sys.initialize(); // init using dummy sys method

        long primaryMonitor = glfwGetPrimaryMonitor();
        GLFWVidMode vidMode = Objects.requireNonNull(glfwGetVideoMode(primaryMonitor), "Primary monitor video mode is null.");

        int monitorWidth = vidMode.width(),
                monitorHeight = vidMode.height(),
                monitorBitPerPixel = vidMode.redBits() + vidMode.greenBits() + vidMode.blueBits(),
                monitorRefreshRate = vidMode.refreshRate();

        desktopDisplayMode = new DisplayMode(monitorWidth, monitorHeight, monitorBitPerPixel, monitorRefreshRate);
    }

    /**
     * Creates the display window, initialises the OpenGL context and prepares all resources.
     */
    // TODO: Fix late resize issue; for some reason it doesn't update on Hyprland?
    public static void create() {
        if (Window.handle != NULL || displayCreated) {
            throw new IllegalStateException("Display already created.");
        }

        // Update display mode
        long primaryMonitor = glfwGetPrimaryMonitor();
        GLFWVidMode vidMode = Objects.requireNonNull(glfwGetVideoMode(primaryMonitor), "Primary monitor video mode is null.");

        int monitorWidth = vidMode.width(),
                monitorHeight = vidMode.height(),
                monitorBitPerPixel = vidMode.redBits() + vidMode.greenBits() + vidMode.blueBits(),
                monitorRefreshRate = vidMode.refreshRate();

        desktopDisplayMode = new DisplayMode(monitorWidth, monitorHeight, monitorBitPerPixel, monitorRefreshRate);

        // Prepare window hints
        glfwDefaultWindowHints();

        if (GL_CONTEXT_BACKWARD_COMPATIBLE) {
            glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_COMPAT_PROFILE);
        }

        glfwWindowHint(GLFW_VISIBLE, GLFW_TRUE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, GL_VERSION_MAJOR);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, GL_VERSION_MINOR);

        // -> OpenGL context
        glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_bool(GL_CONTEXT_DEBUG));
        glfwWindowHint(GLFW_CONTEXT_NO_ERROR, GLFW_bool(GL_CONTEXT_NO_ERROR));
        glfwWindowHint(GLFW_SRGB_CAPABLE, GLFW_bool(GL_CONTEXT_SRGB));
        glfwWindowHint(GLFW_DOUBLEBUFFER, GLFW_bool(GL_DOUBLE_BUFFER));

        // -> Cocoa compositor
        glfwWindowHintString(GLFW_COCOA_FRAME_NAME, Config.COCOA_FRAME_NAME);
        glfwWindowHint(GLFW_COCOA_RETINA_FRAMEBUFFER, GLFW_bool(COCOA_RETINA_FRAME_BUFFER)); // Request a non-hidpi framebuffer (Retina display)

        // -> X11 compositor
        glfwWindowHintString(GLFW_X11_CLASS_NAME, Config.X11_CLASS_NAME);

        // -> Wayland compositor
        glfwWindowHintString(GLFW_WAYLAND_APP_ID, Config.WAYLAND_APP_ID);

        // Create window
        Window.handle = glfwCreateWindow(displayMode.getWidth(), displayMode.getHeight(), title, NULL, NULL);
        if (Window.handle == NULL) {
            throw new IllegalStateException("Failed to create Display window");
        }

        // Patch for raw mouse input
        if (glfwRawMouseMotionSupported()) {
            glfwSetInputMode(Window.handle, GLFW_RAW_MOUSE_MOTION, GLFW_TRUE);
        }

        // Set callbacks
        Window.keyCallback = new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                cancelNextChar = false;
                if (key > GLFW_KEY_SPACE && key <= GLFW_KEY_GRAVE_ACCENT) { // Handle keys have a char - we exclude space to avoid extra input when switching IME
                    if ((GLFW_MOD_CONTROL & mods) != 0 && (GLFW_MOD_ALT & mods) == 0) { // Handle Ctrl + X/C/V
                        Keyboard.addKeyEvent(window, key, scancode, action, mods, (char) (key & 0x1F));
                        cancelNextChar = true; // Cancel char event from Ctrl key event since it is handled here
                    } else if (action > 0) { // Delay press and repeat key event to actual char input; there is always a following char
                        ingredientKeyEvent = new KeyEvent(KeyCodeUtil.toLwjgl(key), '\0', action > 1 ? KeyState.REPEAT : KeyState.PRESS, Sys.getNanoTime());
                    } else { // Release event
                        if (ingredientKeyEvent != null && ingredientKeyEvent.key() == KeyCodeUtil.toLwjgl(key)) {
                            ingredientKeyEvent.outOfOrder(true);
                        }
                        Keyboard.addKeyEvent(window, key, scancode, action, mods, '\0');
                    }
                } else { // Other key with no char event associated
                    char mappedChar = switch (key) {
                        case GLFW_KEY_ENTER -> 0x0D;
                        case GLFW_KEY_ESCAPE -> 0x1B;
                        case GLFW_KEY_TAB -> 0x09;
                        case GLFW_KEY_BACKSPACE -> 0x08;
                        default -> '\0';
                    };
                    Keyboard.addKeyEvent(window, key, scancode, action, mods, mappedChar);
                }
            }
        };

        Window.charCallback = new GLFWCharCallback() {
            @Override
            public void invoke(long window, int codepoint) {
                if (cancelNextChar) { // Char event being cancelled
                    cancelNextChar = false;
                } else if (ingredientKeyEvent != null) {
                    ingredientKeyEvent.keyChar((char) codepoint); // Send char with ASCII key event here
                    Keyboard.addKeyEvent(ingredientKeyEvent);
                    if (ingredientKeyEvent.outOfOrder()) {
                        ingredientKeyEvent = ingredientKeyEvent.copy();
                        ingredientKeyEvent.state(KeyState.RELEASE);
                        Keyboard.addKeyEvent(ingredientKeyEvent);
                    }
                    ingredientKeyEvent = null;
                } else {
                    Keyboard.addCharEvent(0, (char) codepoint); // Non-ASCII chars
                }
            }
        };

        Window.cursorPosCallback = new GLFWCursorPosCallback() {
            @Override
            public void invoke(long window, double x, double y) {
                Mouse.addMoveEvent(x, y);
            }
        };

        Window.mouseButtonCallback = new GLFWMouseButtonCallback() {
            @Override
            public void invoke(long window, int id, int action, int mods) {
                Mouse.addButtonEvent(id, action > 0);
            }
        };

        Window.scrollCallback = new GLFWScrollCallback() {
            @Override
            public void invoke(long window, double xOffset, double yOffset) {
                Mouse.addScrollEvent(xOffset, yOffset);
            }
        };

        Window.windowFocusCallback = new GLFWWindowFocusCallback() {
            @Override
            public void invoke(long window, boolean focused) {
                displayFocused = focused;
            }
        };

        Window.windowIconifyCallback = new GLFWWindowIconifyCallback() {
            @Override
            public void invoke(long window, boolean iconified) {
                displayVisible = !iconified;
            }
        };

        Window.windowSizeCallback = new GLFWWindowSizeCallback() {
            @Override
            public void invoke(long window, int width, int height) {
                latestResized = true;
                latestWidth = width;
                latestHeight = height;
            }
        };

        Window.windowPosCallback = new GLFWWindowPosCallback() {
            @Override
            public void invoke(long window, int x, int y) {
                displayX = x;
                displayY = y;
            }
        };

        Window.windowRefreshCallback = new GLFWWindowRefreshCallback() {
            @Override
            public void invoke(long window) {
                displayDirty = true;
            }
        };

        Window.framebufferSizeCallback = new GLFWFramebufferSizeCallback() {
            @Override
            public void invoke(long window, int width, int height) {
                displayFramebufferWidth = width;
                displayFramebufferHeight = height;
            }
        };

        Window.cursorPosCallback = new GLFWCursorPosCallback() {
            @Override
            public void invoke(long window, double xpos, double ypos) {
                Mouse.addMoveEvent(xpos, ypos);
            }
        };

        Window.mouseButtonCallback = new GLFWMouseButtonCallback() {
            @Override
            public void invoke(long window, int button, int action, int mods) {
                Mouse.addButtonEvent(button, action == GLFW_PRESS);
            }
        };

        Window.setCallbacks();

        // Update display state
        displayWidth = displayMode.getWidth();
        displayHeight = displayMode.getHeight();

        int[] fbw = new int[1],
                fbh = new int[1];

        glfwGetFramebufferSize(Window.handle, fbw, fbh);
        displayFramebufferWidth = fbw[0];
        displayFramebufferHeight = fbh[0];

        displayX = (monitorWidth - displayMode.getWidth()) / 2;
        displayY = (monitorHeight - displayMode.getHeight()) / 2;

        glfwMakeContextCurrent(Window.handle);
        drawable = new DrawableGL();
        GL.createCapabilities();

        glfwSwapInterval(1);
        displayCreated = true;

        // Update window
        setIcon(icons);

        if (fullscreen) {
            setFullscreen(true);
        }

        // Trigger early callbacks to get a more accurate initial state
        int[] ww = new int[1],
                wh = new int[1];

        glfwGetWindowSize(Window.handle, ww, wh);
        glfwGetFramebufferSize(Window.handle, ww, wh);
        Window.windowSizeCallback.invoke(Window.handle, ww[0], wh[0]);
        Window.framebufferSizeCallback.invoke(Window.handle, ww[0], wh[0]);
    }

    /**
     * Dummy method.
     *
     * @implNote Redirected to {@link #create()}.
     */
    public static void create(PixelFormat pixelFormat) {
        LWJGLUtil.log("Display.create(PixelFormat) is not implemented. Please use Display.create().");
        create();
    }

    /**
     * Dummy method.
     *
     * @implNote Redirected to {@link #create()}.
     */
    public static void create(PixelFormat pixelFormat, ContextAttribs attribs) {
        LWJGLUtil.log("Display.create(PixelFormat, ContextAttribs) is not implemented. Please use Display.create().");
        create();
    }

    /**
     * Dummy method.
     *
     * @implNote Redirected to {@link #create()}.
     */
    public static void create(PixelFormat pixelFormat, Drawable sharedDrawable) {
        LWJGLUtil.log("Display.create(PixelFormat, Drawable) is not implemented. Please use Display.create().");
        create();
    }

    /**
     * Dummy method.
     *
     * @implNote Redirected to {@link #create()}.
     */
    public static void create(PixelFormat pixelFormat, Drawable sharedDrawable, ContextAttribs attribs) {
        LWJGLUtil.log("Display.create(PixelFormat, Drawable, ContextAttribs) is not implemented. Please use Display.create().");
        create();
    }

    /**
     * Dummy method.
     *
     * @implNote Redirected to {@link #create()}.
     */
    public static void create(PixelFormatLWJGL pixelFormat) {
        LWJGLUtil.log("Display.create(PixelFormatLWJGL) is not implemented. Please use Display.create().");
        create();
    }

    /**
     * Dummy method.
     *
     * @implNote Redirected to {@link #create()}.
     */
    public static void create(PixelFormatLWJGL pixelFormat, ContextAttribs attribs) {
        LWJGLUtil.log("Display.create(PixelFormatLWJGL, ContextAttribs) is not implemented. Please use Display.create().");
        create();
    }

    /**
     * Dummy method.
     *
     * @implNote Redirected to {@link #create()}.
     */
    public static void create(PixelFormatLWJGL pixelFormat, Drawable sharedDrawable) {
        LWJGLUtil.log("Display.create(PixelFormatLWJGL, Drawable) is not implemented. Please use Display.create().");
        create();
    }

    /**
     * Dummy method.
     *
     * @implNote Redirected to {@link #create()}.
     */
    public static void create(PixelFormatLWJGL pixelFormat, Drawable sharedDrawable, ContextAttribs attribs) {
        LWJGLUtil.log("Display.create(PixelFormatLWJGL, Drawable, ContextAttribs) is not implemented. Please use Display.create().");
        create();
    }

    /**
     * Destroys the display window and releases all resources.
     */
    public static void destroy() {
        destroy(true);
    }

    /**
     * Destroys the display window and releases all resources.
     *
     * @apiNote Custom method.
     */
    // TODO: Debug; something is wrong on Linux? I've cleaned this up to at least close instantly but it still sigsevs
    public static void destroy(boolean terminate) {
        if (!isCreated()) {
            throw new IllegalStateException("Display not created.");
        }

        if (Window.handle == NULL) {
            LWJGLUtil.log("Display.destroy() called but the display window is already destroyed.");
            return;
        }

        Window.releaseCallbacks();

        if (Window.handle != NULL) {
            glfwDestroyWindow(Window.handle);
            Window.handle = NULL;
        }

        if (terminate) {
            terminate();
        }

        displayCreated = false;
    }

    /**
     * Returns the driver adapter.
     *
     * @return The driver adapter.
     */
    public static String getAdapter() {
        return isCreated() ? Objects.requireNonNull(glGetString(GL11.GL_VENDOR), "GL_VENDOR is null.") : "Unknown";
    }

    /**
     * Returns all available display adapters.
     *
     * @return All available display adapters.
     */
    public static DisplayMode[] getAvailableDisplayModes() { // TODO: Consider other monitors?
        GLFWVidMode.Buffer modes = glfwGetVideoModes(glfwGetPrimaryMonitor());
        if (modes == null) {
            LWJGLUtil.log("No video modes found.");
            return new DisplayMode[] {
                    desktopDisplayMode
            };
        }

        DisplayMode[] displayModes = new DisplayMode[modes.remaining()];
        for (int i = 0; i < modes.limit(); i++) {
            GLFWVidMode mode = modes.get(i);
            int width = mode.width(),
                    height = mode.height(),
                    bpp = mode.redBits() + mode.greenBits() + mode.blueBits(),
                    refreshRate = mode.refreshRate();

            displayModes[i] = new DisplayMode(width, height, bpp, refreshRate);
        }

        return displayModes;
    }

    /**
     * Dummy method.
     *
     * @implNote This method is not implemented. The display window is not a child of a canvas.
     */
    @Nullable
    public static Canvas getParent() {
        LWJGLUtil.log("Display.getParent() is not implemented. The display window is not a child of a canvas.");
        return null;
    }

    /**
     * Returns the pixel scale factor of the display window.
     *
     * @return The pixel scale factor of the display window.
     */
    public static float getPixelScaleFactor() {
        if (!isCreated()) {
            return 1.0F;
        }

        float[] xScale = new float[1],
                yScale = new float[1];

        glfwGetWindowContentScale(getWindow(), xScale, yScale);
        return Math.max(xScale[0], yScale[0]);
    }

    /**
     * Returns the driver version of OpenGL.
     *
     * @return The driver version of OpenGL.
     */
    public static String getVersion() {
        return isCreated() ? Objects.requireNonNull(glGetString(GL11.GL_VERSION), "GL_VERSION is null.") : "Unknown";
    }

    /**
     * Returns the current width of the display window.
     *
     * @return The current width of the display window.
     */
    public static int getWidth() {
        return displayWidth;
    }

    /**
     * Returns the current height of the display window.
     *
     * @return The current height of the display window.
     *
     * @apiNote Out of method order provided on <a href="https://legacy.lwjgl.org/javadoc.html">Javadoc</a>.
     */
    public static int getHeight() {
        return displayHeight;
    }

    /**
     * Returns the current x position of the display window.
     *
     * @return The current x position of the display window.
     */
    public static int getX() {
        return displayX;
    }

    /**
     * Returns the current y position of the display window.
     *
     * @return The current y position of the display window.
     */
    public static int getY() {
        return displayY;
    }

    public static boolean isActive() {
        return displayFocused;
    }

    public static boolean isCloseRequested() {
        return glfwWindowShouldClose(Window.handle);
    }

    /**
     * Returns whether the display window is created.
     *
     * @return Whether the display window is created.
     */
    public static boolean isCreated() {
        return Window.handle != NULL && displayCreated;
    }

    /**
     * Returns whether the display is in current context.
     *
     * @return Whether the display is in current context.
     */
    public static boolean isCurrent() {
        return glfwGetCurrentContext() == Window.handle;
    }

    /**
     * Returns whether the display window is dirty.
     *
     * @return Whether the display window is dirty.
     */
    public static boolean isDirty() {
        return displayDirty;
    }

    /**
     * Returns whether the display window is focused.
     *
     * @return Whether the display window is focused.
     */
    public static boolean isVisible() {
        return displayVisible;
    }

    /**
     * Makes the display window current.
     */
    public static void makeCurrent() {
        glfwMakeContextCurrent(Window.handle);
    }

    /**
     * Polls the display window for system messages.
     */
    public static void processMessages() {
        glfwPollEvents();
        Keyboard.poll();
        Mouse.poll();

        if (latestResized) {
            latestResized = false;
            displayResized = true;
            displayWidth = latestWidth;
            displayHeight = latestHeight;

            int[] fbw = new int[1], fbh = new int[1];
            glfwGetFramebufferSize(Window.handle, fbw, fbh);
            displayFramebufferWidth = fbw[0];
            displayFramebufferHeight = fbh[0];
        } else {
            displayResized = false;
        }
    }

    /**
     * Releases the display window context.
     */
    public static void releaseContext() {
        glfwMakeContextCurrent(NULL);
    }

    /**
     * Dummy method.
     *
     * @implNote This method is not implemented. GLFW does not provide brightness or contrast settings.
     */
    public static void setDisplayConfiguration(float gamma, float brightness, float contrast) {
        LWJGLUtil.log("Display.setDisplayConfiguration() is not implemented. GLFW only provides gamma correction.");
    }

    /**
     * Sets the display mode.
     *
     * @param mode The new display mode.
     */
    public static void setDisplayMode(DisplayMode mode) {
        DisplayMode currentMode = getDisplayMode();
        if (currentMode.equals(mode)) {
            return;
        }

        // TODO: Implement display mode setting
        // glfwSetWindowSize(window, mode.getWidth(), mode.getHeight());
        // glfwSetWindowMonitor(window, NULL, 0, 0, mode.getWidth(), mode.getHeight(), GLFW_DONT_CARE);
        // glfwSetWindowPos(window, (desktopDisplayMode.getWidth() - mode.getWidth()) / 2, (desktopDisplayMode.getHeight() - mode.getHeight()) / 2);

        displayMode = mode;
    }

    /**
     * Sets the display mode and toggles fullscreen.
     *
     * @param mode The new display mode.
     *
     * @implNote This method is not implemented. Please use {@link #setDisplayMode(DisplayMode)} and {@link #setFullscreen(boolean)}.
     * @implNote Redirected to {@link #setDisplayMode(DisplayMode)}.
     */
    public static void setDisplayModeAndFullscreen(DisplayMode mode) {
        LWJGLUtil.log("Display.setDisplayModeAndFullscreen() is not implemented. Please use Display.setDisplayMode() with a full width and height.");
        setDisplayMode(mode);
    }

    /**
     * Sets the fullscreen state of the display window.
     *
     * @param fullscreen The new fullscreen state of the display window.
     */
    public static void setFullscreen(boolean fullscreen) {
        if (!isCreated()) {
            Display.fullscreen = fullscreen;
            return;
        }

        if (fullscreen) {
            // Store the current display state before switching to fullscreen
            if (!Display.fullscreen) {
                savedDisplayX = displayX;
                savedDisplayY = displayY;
                savedDisplayWidth = displayWidth;
                savedDisplayHeight = displayHeight;
            }

            if (GL_FULLSCREEN_BORDERLESS) {
                _setFullscreenBorderless();
            } else {
                _setFullscreenExclusive();
            }
        } else {
            glfwSetWindowMonitor(Window.handle, NULL, savedDisplayX, savedDisplayY, savedDisplayWidth, savedDisplayHeight, 0);
            //noinspection DataFlowIssue
            Window.windowSizeCallback.invoke(Window.handle, savedDisplayWidth, savedDisplayHeight);
            glfwSetWindowAttrib(Window.handle, GLFW_DECORATED, GLFW_TRUE); // Ensure the window is decorated
        }

        Display.fullscreen = fullscreen;
    }

    /**
     * Sets the display icon.
     *
     * @param icons The new display icon.
     */
    // TODO: Validate platform icon support
    public static void setIcon(@Nullable ByteBuffer[] icons) {
        //noinspection ConstantValue
        if (icons == null || icons.length == 0) {
            LWJGLUtil.log("Display.setIcon() called with null or empty icon array. No icon will be set.");
            return;
        }

        if (!isCreated()) {
            Display.icons = icons;
            return;
        }

        GLFWImage.Buffer glfwImages = GLFWImage.calloc(icons.length);
        ByteBuffer[] nativeBuffers = new ByteBuffer[icons.length];
        for (int icon = 0; icon < icons.length; icon++) {
            ByteBuffer image = icons[icon];
            if (image == null) {
                throw new IllegalArgumentException("Icon at index " + icon + " is null.");
            }
            nativeBuffers[icon] = BufferUtils.createByteBuffer(image.capacity());
            nativeBuffers[icon].put(image);
            nativeBuffers[icon].flip();
            int dimension = (int) Math.sqrt(nativeBuffers[icon].limit() / 4D);
            if (dimension * dimension * 4 != nativeBuffers[icon].limit()) {
                throw new IllegalStateException();
            }
            //noinspection resource
            glfwImages.put(icon, GLFWImage.create().set(dimension, dimension, nativeBuffers[icon]));
        }
        glfwSetWindowIcon(getWindow(), glfwImages);
        glfwImages.free();
    }

    /**
     * Dummy method.
     *
     * @implNote This method is not implemented. Please use {@link GL11C#glClearColor(float, float, float, float)}.
     */
    public static void setInitialBackground(float red, float green, float blue) {
        LWJGLUtil.log("Display.setInitialBackground() is not implemented. Please use GL11.glClearColor().");
    }

    /**
     * Sets the position of the display window.
     *
     * @param x The new x position of the display window.
     * @param y The new y position of the display window.
     *
     * @implNote Does not take effect prior to display creation.
     */
    public static void setLocation(int x, int y) {
        if (fullscreen || !isCreated()) {
            return;
        }
        glfwSetWindowPos(Window.handle, x, y);
    }

    /**
     * Dummy method.
     *
     * @implNote This method is not implemented. The display window is not a child of a canvas.
     */
    public static void setParent(Canvas canvas) {
        LWJGLUtil.log("Display.setParent() is not implemented. The display window is not a child of a canvas.");
    }

    /**
     * Sets the resizable state of the display window.
     *
     * @param resizable The new resizable state of the display window.
     */
    public static void setResizable(boolean resizable) {
        Display.resizable = resizable;
        if (!isCreated()) {
            return;
        }
        glfwWindowHint(GLFW_RESIZABLE, resizable ? GLFW_TRUE : GLFW_FALSE);
    }

    /**
     * Sets the swap interval of the display window.
     *
     * @param fps The new swap interval of the display window.
     */
    public static void setSwapInterval(int fps) {
        glfwSwapInterval(fps);
    }

    /**
     * Sets the title of the display window.
     *
     * @param title The new title of the display window.
     */
    public static void setTitle(String title) {
        Display.title = title;
        if (!isCreated()) {
            return;
        }
        glfwSetWindowTitle(Window.handle, title);
    }

    /**
     * Sets the VSync state of the display window.
     *
     * @param vsync The new VSync state of the display window.
     */
    public static void setVSyncEnabled(boolean vsync) {
        if (!isCreated()) {
            return;
        }
        setSwapInterval(vsync ? 1 : 0);
    }

    /**
     * Performs a buffer swap on the display window.
     */
    public static void swapBuffers() {
        if (!isCreated()) {
            return;
        }
        glfwSwapBuffers(Window.handle);
    }

    /**
     * Synchronises the display window to a specific frame rate.
     *
     * @param fps The frame rate to synchronise to.
     */
    public static void sync(int fps) {
        if (!isCreated()) {
            return;
        }
        Sync.sync(fps);
    }

    /**
     * Updates the display window.
     */
    public static void update() {
        if (!isCreated()) {
            return;
        }
        update(true);
    }

    /**
     * Updates the display window.
     *
     * @param processMessages Whether to process system messages.
     */
    public static void update(boolean processMessages) {
        if (!isCreated()) {
            return;
        }
        swapBuffers();
        displayDirty = false;
        if (processMessages) {
            processMessages();
        }
    }

    /**
     * Returns whether the display window was resized.
     *
     * @return Whether the display window was resized.
     */
    public static boolean wasResized() {
        return displayResized;
    }

    /**
     * Dummy method.
     *
     * @implNote This method is not implemented. The wrapper handles a singular GLFW display instance.
     */
    @Nullable
    static DisplayImplementation getImplementation() {
        LWJGLUtil.log("Display.getImplementation() is not implemented. The wrapper handles a singular GLFW display instance.");
        return null;
    }

    /*
      Custom methods below:
        these provide additional functionality to the Display class for client use.
     */

    /**
     * Sets the size of the display window.
     *
     * @param width The new width of the display window.
     * @param height The new height of the display window.
     *
     * @apiNote Custom method.
     * @implNote Does not take effect prior to display creation.
     */
    public static void setSize(int width, int height) {
        if (!isCreated() || fullscreen) {
            return;
        }

        Display.width = width;
        Display.height = height;
        glfwSetWindowSize(Window.handle, width, height);
    }

    /**
     * Sets the visibility of the display window.
     *
     * @param visible The new visibility of the display window.
     *
     * @apiNote Custom method.
     */
    public static void setVisible(boolean visible) {
        if (!isCreated() || !displayCreated) {
            return;
        }

        if (visible) {
            glfwShowWindow(Window.handle);
        } else {
            glfwHideWindow(Window.handle);
        }
    }

    /**
     * Prints debug information about the display window.
     *
     * @apiNote Custom method.
     */
    public static void printDebugInfo() {
        System.out.printf("Renderer: %s\n%n", glGetString(GL_RENDERER));
        System.out.printf("Version: %s\n%n", glGetString(GL_VERSION));
        System.out.printf("GLSL Version: %s\n%n", glGetString(GL_SHADING_LANGUAGE_VERSION));
    }

    /**
     * Sets the debug output of the display window.
     *
     * @param enabled The new debug output state of the display window.
     */
    public static void setDebugOutput(boolean enabled) {
        if (enabled) {
            glEnable(GL_DEBUG_OUTPUT);
            glDebugMessageCallback((source, type, id, severity, length, message, userParam) -> {
                System.err.printf("OpenGL Debug Message (%d): %s\n", id, GLDebugMessageCallback.getMessage(length, message));
            }, NULL);
        } else {
            glDisable(GL_DEBUG_OUTPUT);
        }
    }

    /**
     * Calls glfwTerminate
     *
     * @apiNote Custom method.
     */
    public static void terminate() {
        glfwTerminate();
    }

    private static class Window {
        static long handle;

        @Nullable static GLFWKeyCallback keyCallback = null;
        @Nullable static GLFWCharCallback charCallback = null;
        @Nullable static GLFWCursorPosCallback cursorPosCallback = null;
        @Nullable static GLFWMouseButtonCallback mouseButtonCallback = null;
        @Nullable static GLFWScrollCallback scrollCallback = null;
        @Nullable static GLFWWindowFocusCallback windowFocusCallback = null;
        @Nullable static GLFWWindowIconifyCallback windowIconifyCallback = null;
        @Nullable static GLFWWindowSizeCallback windowSizeCallback = null;
        @Nullable static GLFWWindowPosCallback windowPosCallback = null;
        @Nullable static GLFWWindowRefreshCallback windowRefreshCallback = null;
        @Nullable static GLFWFramebufferSizeCallback framebufferSizeCallback = null;
        @Nullable static GLFWErrorCallback errorCallback = GLFWErrorCallback.createPrint(System.err); // Kept open; we want to see the errors

        public static void setCallbacks() {
            glfwSetKeyCallback(handle, keyCallback);
            glfwSetCharCallback(handle, charCallback);
            glfwSetCursorPosCallback(handle, cursorPosCallback);
            glfwSetMouseButtonCallback(handle, mouseButtonCallback);
            glfwSetScrollCallback(handle, scrollCallback);
            glfwSetWindowFocusCallback(handle, windowFocusCallback);
            glfwSetWindowIconifyCallback(handle, windowIconifyCallback);
            glfwSetWindowSizeCallback(handle, windowSizeCallback);
            glfwSetWindowPosCallback(handle, windowPosCallback);
            glfwSetWindowRefreshCallback(handle, windowRefreshCallback);
            glfwSetFramebufferSizeCallback(handle, framebufferSizeCallback);
            glfwSetErrorCallback(errorCallback);
        }

        @SuppressWarnings("resource")
        public static void releaseCallbacks() { // TODO: Cleanup - repeated codeGLFW.glfwSetKeyCallback(handle, keyCallback);
            glfwSetCharCallback(handle, null);
            glfwSetCursorPosCallback(handle, null);
            glfwSetMouseButtonCallback(handle, null);
            glfwSetScrollCallback(handle, null);
            glfwSetWindowFocusCallback(handle, null);
            glfwSetWindowIconifyCallback(handle, null);
            glfwSetWindowSizeCallback(handle, null);
            glfwSetWindowPosCallback(handle, null);
            glfwSetWindowRefreshCallback(handle, null);
            glfwSetFramebufferSizeCallback(handle, null);
            if (keyCallback != null) keyCallback.free();
            if (charCallback != null) charCallback.free();
            if (cursorPosCallback != null) cursorPosCallback.free();
            if (mouseButtonCallback != null) mouseButtonCallback.free();
            if (scrollCallback != null) scrollCallback.free();
            if (windowFocusCallback != null) windowFocusCallback.free();
            if (windowIconifyCallback != null) windowIconifyCallback.free();
            if (windowSizeCallback != null) windowSizeCallback.free();
            if (windowPosCallback != null) windowPosCallback.free();
            if (windowRefreshCallback != null) windowRefreshCallback.free();
            if (framebufferSizeCallback != null) framebufferSizeCallback.free();
        }
    }

    /**
     * Returns the window handle.
     *
     * @return The window handle.
     */
    public static long getWindow() {
        return Window.handle;
    }

    /**
     * Sets the display to fullscreen exclusive mode.
     */
    private static void _setFullscreenExclusive() {
        long currentMonitor = _getCurrentMonitor();
        GLFWVidMode vidMode = Objects.requireNonNull(glfwGetVideoMode(currentMonitor), "Primary monitor video mode is null.");
        glfwSetWindowMonitor(Window.handle, currentMonitor, 0, 0, vidMode.width(), vidMode.height(), vidMode.refreshRate());
    }

    /**
     * Sets the display to fullscreen borderless mode.
     */
    private static void _setFullscreenBorderless() {
        long currentMonitor = _getCurrentMonitor();
        GLFWVidMode vidMode = Objects.requireNonNull(glfwGetVideoMode(currentMonitor), "Primary monitor video mode is null.");
        glfwSetWindowAttrib(Window.handle, GLFW_DECORATED, GLFW_FALSE);
        glfwSetWindowMonitor(Window.handle, currentMonitor, 0, 0, vidMode.width(), GL_FULLSCREEN_BORDERLESS_WINDOWS_FIX ? vidMode.height() + 1 : vidMode.height(), vidMode.refreshRate());
    }

    /**
     * Finds the current monitor based on position.
     */
    private static long _getCurrentMonitor() {
        PointerBuffer monitors = Objects.requireNonNull(glfwGetMonitors(), "No monitors found.");
        if (monitors.limit() == 0) {
            LWJGLUtil.log("No monitors found.");
            return glfwGetPrimaryMonitor();
        }

        for (int i = 0; i < monitors.limit(); i++) {
            long monitor = monitors.get(i);

            int[] monX = new int[1],
                    monY = new int[1];
            glfwGetMonitorPos(monitor, monX, monY);

            GLFWVidMode mode = Objects.requireNonNull(glfwGetVideoMode(monitor), "Monitor video mode is null.");
            int width = mode.width(),
                    height = mode.height();

            if (displayX >= monX[0] && displayX < monX[0] + width &&
                    displayY >= monY[0] && displayY < monY[0] + height) {
                return monitor;
            }
        }

        LWJGLUtil.log("No monitors found.");
        return glfwGetPrimaryMonitor();
    }
}