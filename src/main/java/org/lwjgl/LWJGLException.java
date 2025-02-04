package org.lwjgl;

import java.io.Serial;

/**
 * Decompiled from lwjgl-2.9.4-nightly-20150209.jar
 */
public class LWJGLException extends Exception {
    @Serial
    private static final long serialVersionUID = 1L;

    public LWJGLException() {
    }

    public LWJGLException(String msg) {
        super(msg);
    }

    public LWJGLException(String message, Throwable cause) {
        super(message, cause);
    }

    public LWJGLException(Throwable cause) {
        super(cause);
    }
}
