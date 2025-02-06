package org.lwjgl.input;

import me.darragh.lwjgl.opengl.input.keyboard.KeyCodeUtil;
import me.darragh.lwjgl.opengl.input.keyboard.KeyEvent;
import me.darragh.lwjgl.opengl.input.keyboard.KeyCode;
import me.darragh.lwjgl.opengl.input.keyboard.KeyState;
import org.lwjgl.LWJGLException;
import org.lwjgl.LWJGLUtil;
import org.lwjgl.Sys;
import org.lwjgl.opengl.Display;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * This is a wrapper implementation of the LWJGL2 Keyboard class.
 *
 * @author darraghd493
 * @since 1.0.0
 */
public class Keyboard {
    /**
     * Unused <b>internal</b> variable.
     * <p>
     * Retained for compatibility.
     */
    public static final int EVENT_SIZE = 4 + 1 + 4 + 8 + 1;

    /**
     * The special character meaning that no character was translated for the event.
     */
    public static final int CHAR_NONE = '\0';

    /**
     * The special keycode meaning that only the translated character is valid.
     */
    @KeyCode(name = "NONE") public static final int KEY_NONE = 0x00;
    @KeyCode(name = "ESCAPE") public static final int KEY_ESCAPE = 0x01;
    @KeyCode(name = "1") public static final int KEY_1 = 0x02;
    @KeyCode(name = "2") public static final int KEY_2 = 0x03;
    @KeyCode(name = "3") public static final int KEY_3 = 0x04;
    @KeyCode(name = "4") public static final int KEY_4 = 0x05;
    @KeyCode(name = "5") public static final int KEY_5 = 0x06;
    @KeyCode(name = "6") public static final int KEY_6 = 0x07;
    @KeyCode(name = "7") public static final int KEY_7 = 0x08;
    @KeyCode(name = "8") public static final int KEY_8 = 0x09;
    @KeyCode(name = "9") public static final int KEY_9 = 0x0A;
    @KeyCode(name = "0") public static final int KEY_0 = 0x0B;
    @KeyCode(name = "MINUS") public static final int KEY_MINUS = 0x0C; /* - */
    @KeyCode(name = "EQUALS") public static final int KEY_EQUALS = 0x0D;
    @KeyCode(name = "BACK") public static final int KEY_BACK = 0x0E; /* Backspace */
    @KeyCode(name = "TAB") public static final int KEY_TAB = 0x0F;
    @KeyCode(name = "Q") public static final int KEY_Q = 0x10;
    @KeyCode(name = "W") public static final int KEY_W = 0x11;
    @KeyCode(name = "E") public static final int KEY_E = 0x12;
    @KeyCode(name = "R") public static final int KEY_R = 0x13;
    @KeyCode(name = "T") public static final int KEY_T = 0x14;
    @KeyCode(name = "Y") public static final int KEY_Y = 0x15;
    @KeyCode(name = "U") public static final int KEY_U = 0x16;
    @KeyCode(name = "I") public static final int KEY_I = 0x17;
    @KeyCode(name = "O") public static final int KEY_O = 0x18;
    @KeyCode(name = "P") public static final int KEY_P = 0x19;
    @KeyCode(name = "LBRACKET") public static final int KEY_LBRACKET = 0x1A;
    @KeyCode(name = "RBRACKET") public static final int KEY_RBRACKET = 0x1B;
    @KeyCode(name = "RETURN") public static final int KEY_RETURN = 0x1C; /* Enter */
    @KeyCode(name = "LCONTROL") public static final int KEY_LCONTROL = 0x1D;
    @KeyCode(name = "A") public static final int KEY_A = 0x1E;
    @KeyCode(name = "S") public static final int KEY_S = 0x1F;
    @KeyCode(name = "D") public static final int KEY_D = 0x20;
    @KeyCode(name = "F") public static final int KEY_F = 0x21;
    @KeyCode(name = "G") public static final int KEY_G = 0x22;
    @KeyCode(name = "H") public static final int KEY_H = 0x23;
    @KeyCode(name = "J") public static final int KEY_J = 0x24;
    @KeyCode(name = "K") public static final int KEY_K = 0x25;
    @KeyCode(name = "L") public static final int KEY_L = 0x26;
    @KeyCode(name = "SEMICOLON") public static final int KEY_SEMICOLON = 0x27;
    @KeyCode(name = "APOSTROPHE") public static final int KEY_APOSTROPHE = 0x28;
    @KeyCode(name = "GRAVE") public static final int KEY_GRAVE = 0x29; /* Accent Grave */
    @KeyCode(name = "LSHIFT") public static final int KEY_LSHIFT = 0x2A;
    @KeyCode(name = "BACKSLASH") public static final int KEY_BACKSLASH = 0x2B;
    @KeyCode(name = "Z") public static final int KEY_Z = 0x2C;
    @KeyCode(name = "X") public static final int KEY_X = 0x2D;
    @KeyCode(name = "C") public static final int KEY_C = 0x2E;
    @KeyCode(name = "V") public static final int KEY_V = 0x2F;
    @KeyCode(name = "B") public static final int KEY_B = 0x30;
    @KeyCode(name = "N") public static final int KEY_N = 0x31;
    @KeyCode(name = "M") public static final int KEY_M = 0x32;
    @KeyCode(name = "COMMA") public static final int KEY_COMMA = 0x33;
    @KeyCode(name = "PERIOD") public static final int KEY_PERIOD = 0x34; /* . */
    @KeyCode(name = "SLASH") public static final int KEY_SLASH = 0x35; /* / */
    @KeyCode(name = "RSHIFT") public static final int KEY_RSHIFT = 0x36;
    @KeyCode(name = "MULTIPLY") public static final int KEY_MULTIPLY = 0x37; /* * */
    @KeyCode(name = "LMENU") public static final int KEY_LMENU = 0x38; /* Left Alt */
    @KeyCode(name = "SPACE") public static final int KEY_SPACE = 0x39;
    @KeyCode(name = "CAPITAL") public static final int KEY_CAPITAL = 0x3A;
    @KeyCode(name = "F1") public static final int KEY_F1 = 0x3B;
    @KeyCode(name = "F2") public static final int KEY_F2 = 0x3C;
    @KeyCode(name = "F3") public static final int KEY_F3 = 0x3D;
    @KeyCode(name = "F4") public static final int KEY_F4 = 0x3E;
    @KeyCode(name = "F5") public static final int KEY_F5 = 0x3F;
    @KeyCode(name = "F6") public static final int KEY_F6 = 0x40;
    @KeyCode(name = "F7") public static final int KEY_F7 = 0x41;
    @KeyCode(name = "F8") public static final int KEY_F8 = 0x42;
    @KeyCode(name = "F9") public static final int KEY_F9 = 0x43;
    @KeyCode(name = "F10") public static final int KEY_F10 = 0x44;
    @KeyCode(name = "NUMLOCK") public static final int KEY_NUMLOCK = 0x45;
    @KeyCode(name = "SCROLL") public static final int KEY_SCROLL = 0x46; /* Scroll Lock */
    @KeyCode(name = "NUMPAD7") public static final int KEY_NUMPAD7 = 0x47;
    @KeyCode(name = "NUMPAD8") public static final int KEY_NUMPAD8 = 0x48;
    @KeyCode(name = "NUMPAD9") public static final int KEY_NUMPAD9 = 0x49;
    @KeyCode(name = "SUBTRACT") public static final int KEY_SUBTRACT = 0x4A; /* - (Numeric Keypad) */
    @KeyCode(name = "NUMPAD4") public static final int KEY_NUMPAD4 = 0x4B;
    @KeyCode(name = "NUMPAD5") public static final int KEY_NUMPAD5 = 0x4C;
    @KeyCode(name = "NUMPAD6") public static final int KEY_NUMPAD6 = 0x4D;
    @KeyCode(name = "ADD") public static final int KEY_ADD = 0x4E; /* + (Numeric Keypad) */
    @KeyCode(name = "NUMPAD1") public static final int KEY_NUMPAD1 = 0x4F;
    @KeyCode(name = "NUMPAD2") public static final int KEY_NUMPAD2 = 0x50;
    @KeyCode(name = "NUMPAD3") public static final int KEY_NUMPAD3 = 0x51;
    @KeyCode(name = "NUMPAD0") public static final int KEY_NUMPAD0 = 0x52;
    @KeyCode(name = "DECIMAL") public static final int KEY_DECIMAL = 0x53; /* . (Numeric Keypad) */
    @KeyCode(name = "F11") public static final int KEY_F11 = 0x57;
    @KeyCode(name = "F12") public static final int KEY_F12 = 0x58;
    @KeyCode(name = "F13") public static final int KEY_F13 = 0x64; /* (NEC PC98) */
    @KeyCode(name = "F14") public static final int KEY_F14 = 0x65; /* (NEC PC98) */
    @KeyCode(name = "F15") public static final int KEY_F15 = 0x66; /* (NEC PC98) */
    @KeyCode(name = "F16") public static final int KEY_F16 = 0x67; /* Extended Function keys - (Mac) */
    @KeyCode(name = "F17") public static final int KEY_F17 = 0x68;
    @KeyCode(name = "F18") public static final int KEY_F18 = 0x69;
    @KeyCode(name = "KANA") public static final int KEY_KANA = 0x70; /* (Japanese keyboard) */
    @KeyCode(name = "F19") public static final int KEY_F19 = 0x71; /* Extended Function keys - (Mac) */
    @KeyCode(name = "CONVERT") public static final int KEY_CONVERT = 0x79; /* (Japanese keyboard) */
    @KeyCode(name = "NOCONVERT") public static final int KEY_NOCONVERT = 0x7B; /* (Japanese keyboard) */
    @KeyCode(name = "YEN") public static final int KEY_YEN = 0x7D; /* (Japanese keyboard) */
    @KeyCode(name = "NUMPADEQUALS") public static final int KEY_NUMPADEQUALS = 0x8D; /* = (Numeric Keypad) (NEC PC98) */
    @KeyCode(name = "CIRCUMFLEX") public static final int KEY_CIRCUMFLEX = 0x90; /* (Japanese keyboard) */
    @KeyCode(name = "AT") public static final int KEY_AT = 0x91; /* (NEC PC98) */
    @KeyCode(name = "COLON") public static final int KEY_COLON = 0x92; /* (NEC PC98) */
    @KeyCode(name = "UNDERLINE") public static final int KEY_UNDERLINE = 0x93; /* (NEC PC98) */
    @KeyCode(name = "KANJI") public static final int KEY_KANJI = 0x94; /* (Japanese keyboard) */
    @KeyCode(name = "STOP") public static final int KEY_STOP = 0x95; /* (NEC PC98) */
    @KeyCode(name = "AX") public static final int KEY_AX = 0x96; /* (Japan AX) */
    @KeyCode(name = "UNLABELED") public static final int KEY_UNLABELED = 0x97; /* (J3100) */
    @KeyCode(name = "NUMPADENTER") public static final int KEY_NUMPADENTER = 0x9C; /* Enter (Numeric Keypad) */
    @KeyCode(name = "RCONTROL") public static final int KEY_RCONTROL = 0x9D;
    @KeyCode(name = "SECTION") public static final int KEY_SECTION = 0xA7; /* Section symbol (Mac) */
    @KeyCode(name = "NUMPADCOMMA") public static final int KEY_NUMPADCOMMA = 0xB3; /* , (Numeric Keypad) (NEC PC98) */
    @KeyCode(name = "DIVIDE") public static final int KEY_DIVIDE = 0xB5; /* / (Numeric Keypad) */
    @KeyCode(name = "SYSRQ") public static final int KEY_SYSRQ = 0xB7;
    @KeyCode(name = "RMENU") public static final int KEY_RMENU = 0xB8; /* right Alt */
    @KeyCode(name = "FUNCTION") public static final int KEY_FUNCTION = 0xC4; /* Function (Mac) */
    @KeyCode(name = "PAUSE") public static final int KEY_PAUSE = 0xC5; /* Pause */
    @KeyCode(name = "HOME") public static final int KEY_HOME = 0xC7; /* Home (Arrow Keypad) */
    @KeyCode(name = "UP") public static final int KEY_UP = 0xC8; /* UpArrow (Arrow Keypad) */
    @KeyCode(name = "PRIOR") public static final int KEY_PRIOR = 0xC9; /* PgUp (Arrow Keypad) */
    @KeyCode(name = "LEFT") public static final int KEY_LEFT = 0xCB; /* LeftArrow (Arrow Keypad) */
    @KeyCode(name = "RIGHT") public static final int KEY_RIGHT = 0xCD; /* RightArrow (Arrow Keypad) */
    @KeyCode(name = "END") public static final int KEY_END = 0xCF; /* End (Arrow Keypad) */
    @KeyCode(name = "DOWN") public static final int KEY_DOWN = 0xD0; /* DownArrow (Arrow Keypad) */
    @KeyCode(name = "NEXT") public static final int KEY_NEXT = 0xD1; /* PgDn (Arrow Keypad) */
    @KeyCode(name = "INSERT") public static final int KEY_INSERT = 0xD2; /* Insert (Arrow Keypad) */
    @KeyCode(name = "DELETE") public static final int KEY_DELETE = 0xD3; /* Delete (Arrow Keypad) */
    @KeyCode(name = "CLEAR") public static final int KEY_CLEAR = 0xDA; /* Clear (Mac) */
    @KeyCode(name = "LMETA") public static final int KEY_LMETA = 0xDB; /* Left Windows/Option */
    @KeyCode(name = "LWIN") public static final int KEY_LWIN = KEY_LMETA; /* Left Windows */
    @KeyCode(name = "RMETA") public static final int KEY_RMETA = 0xDC; /* Right Windows/Option */
    @KeyCode(name = "RWIN") public static final int KEY_RWIN = KEY_RMETA; /* Right Windows */
    @KeyCode(name = "APPS") public static final int KEY_APPS = 0xDD; /* AppMenu */
    @KeyCode(name = "POWER") public static final int KEY_POWER = 0xDE;
    @KeyCode(name = "SLEEP") public static final int KEY_SLEEP = 0xDF;

    private static final Queue<KeyEvent> EVENT_QUEUE = new ArrayBlockingQueue<>(128);
    private static final Map<Integer, String> KEY_NAMES = new HashMap<>();
    private static final int KEY_COUNT;
    private static boolean repeatEvents;

    /**
     * Unused variable.
     * <p>
     * Retained for compatibility.
     */
    public static final int KEYBOARD_SIZE = 256;

    static {
        int counter = 0;
        try {
            for (var field : Keyboard.class.getFields()) {
                if (Modifier.isStatic(field.getModifiers()) && Modifier.isPublic(field.getModifiers())
                        && Modifier.isFinal(field.getModifiers())
                        && field.getType().equals(int.class)
                        && field.isAnnotationPresent(KeyCode.class)) {
                    KeyCode keyCode = field.getAnnotation(KeyCode.class);
                    int key = field.getInt(null);
                    String name = keyCode.name();
                    KEY_NAMES.put(key, "Key " + name);
                    counter++;
                }
            }
        } catch (Exception ignored) {}

        KEY_COUNT = counter;
        EVENT_QUEUE.add(new KeyEvent(0, '\0', KeyState.RELEASE, Sys.getNanoTime()));
    }

    /**
     * Enqueues a key event.
     *
     * @param window The window the event occurred in.
     * @param key The key that was pressed.
     * @param scancode The scancode of the key.
     * @param action The action that occurred.
     * @param mods The modifiers that were pressed.
     * @param keyChar The character that was pressed.
     *
     * @implNote Not all keys are supported. This is a partial implementation.
     * @implNote Not all parameters are used. This consumes all presented in the callback.
     *
     * @apiNote Custom method.
     */
    public static void addKeyEvent(long window, int key, int scancode, int action, int mods, char keyChar) {
        KeyState state = KeyState.PRESS;
        if (action == GLFW_RELEASE) {
            state = KeyState.RELEASE;
        } else if (action == GLFW_REPEAT) {
            state = KeyState.REPEAT;
        }

        EVENT_QUEUE.add(new KeyEvent(KeyCodeUtil.toLwjgl(key), keyChar, state, Sys.getNanoTime()));
    }

    /**
     * Enqueues a specified key event.
     *
     * @param event The key event to enqueue.
     *
     * @apiNote Custom method.
     */
    public static void addKeyEvent(KeyEvent event) {
        if (event == null || (event.state() == KeyState.REPEAT && !repeatEvents)) {
            return;
        }

        EVENT_QUEUE.add(event);
    }

    /**
     * Enqueues a singular character press.
     *
     * @param key The key that was pressed.
     * @param keyChar The character that was pressed.
     *
     * @apiNote Custom method.
     */
    public static void addCharEvent(int key, char keyChar) {
        EVENT_QUEUE.add(new KeyEvent(KEY_NONE, keyChar, KeyState.PRESS, Sys.getNanoTime()));
    }

    /**
     * Dummy method.
     *
     * @throws LWJGLException Never thrown.
     */
    public static void create() throws LWJGLException {}

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
        LWJGLUtil.log("Keyboard.isCreated() is not implemented.");
        return Display.isCreated();
    }

    /**
     * Dummy method.
     */
    public static void destroy() {
        LWJGLUtil.log("Keyboard.destroy() is not implemented.");
    }

    /**
     * Dummy method.
     *
     * @implNote This is not implemented due to not actively storing the current state. We intend the game to poll manually using {@link #next()}.
     */
    public static void poll() {
        LWJGLUtil.log("Keyboard.poll() is not implemented.");
    }

    /**
     * Returns whether the key is currently down.
     *
     * @param key The key to check.
     * @return Whether the key is down.
     */
    public static boolean isKeyDown(int key) {
        return glfwGetKey(Display.getWindow(), KeyCodeUtil.toGlfw(key)) == GLFW_PRESS;
    }

    /**
     * Returns the name of the key.
     *
     * @param key The key to get the name of.
     */
    public static String getKeyName(int key) {
        return KEY_NAMES.getOrDefault(key, "Key " + key);
    }

    /**
     * Returns the index of a key by its name.
     *
     * @param keyName The name of the key.
     * @return The index of the key.
     */
    public static int getKeyIndex(String keyName) {
        for (Map.Entry<Integer, String> entry : KEY_NAMES.entrySet()) {
            if (entry.getValue().equals(keyName)) {
                return entry.getKey();
            }
        }
        return -1;
    }

    /**
     * Returns the number of queued keyboard events.
     *
     * @return The number of queued keyboard events.
     */
    public static int getNumKeyboardKeys() {
        return EVENT_QUEUE.size();
    }

    /**
     * Moves on to the next event in the queue.
     *
     * @return Whether there are more events in the queue.
     */
    public static boolean next() {
        boolean next = EVENT_QUEUE.size() > 1;
        if (next) {
            EVENT_QUEUE.remove();
        }
        return next;
    }

    /**
     * Sets the repeat events state.
     * @param enable Whether to enable repeat events.
     */
    public static void enableRepeatEvents(boolean enable) {
        repeatEvents = enable;
    }

    /**
     * Returns whether repeat events are enabled.
     * @return Whether repeat events are enabled.
     */
    public static boolean areRepeatEventsEnabled() {
        return repeatEvents;
    }

    /**
     * Returns the amount of supported keys.
     *
     * @return The amount of supported keys.
     */
    public static int getKeyCount() {
        return KEY_COUNT;
    }

    /**
     * Returns the current event key character.
     *
     * @return The current event key character.
     */
    public static char getEventCharacter() {
        if (EVENT_QUEUE.isEmpty()) {
            return CHAR_NONE;
        }
        return EVENT_QUEUE.peek().keyChar();
    }

    /**
     * Returns the current event key character.
     *
     * @return The current event key character.
     */
    public static int getEventKey() {
        if (EVENT_QUEUE.isEmpty()) {
            return CHAR_NONE;
        }
        return EVENT_QUEUE.peek().key();
    }

    /**
     * Returns the current event key.
     *
     * @return The current event key.
     */
    public static boolean getEventKeyState() {
        if (EVENT_QUEUE.isEmpty()) {
            return false;
        }
        return EVENT_QUEUE.peek().state().isPressed();
    }

    /**
     * Returns the exact time the event was pressed in nanoseconds.
     *
     * @return The exact time the event was pressed in nanoseconds.
     */
    public static long getEventNanoseconds() {
        if (EVENT_QUEUE.isEmpty()) {
            return NULL;
        }
        return EVENT_QUEUE.peek().timePressed();
    }

    /**
     * Returns whether the current event is a repeat event.
     *
     * @return Whether the current event is a repeat event.
     */
    public static boolean isRepeatEvent() {
        if (EVENT_QUEUE.isEmpty()) {
            return false;
        }
        return EVENT_QUEUE.peek().state() == KeyState.REPEAT;
    }
}
