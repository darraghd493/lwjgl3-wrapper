package org.lwjgl;

import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * Decompiled from lwjgl-2.9.4-nightly-20150209.jar
 */
public abstract class PointerWrapperAbstract implements PointerWrapper {
    protected final long pointer;

    protected PointerWrapperAbstract(long pointer) {
        this.pointer = pointer;
    }

    public boolean isValid() {
        return this.pointer != NULL;
    }

    public final void checkValid() {
        if (LWJGLUtil.DEBUG && !this.isValid()) {
            throw new IllegalStateException("This " + this.getClass().getSimpleName() + " pointer is not valid.");
        }
    }

    @Override
    public final long getPointer() {
        this.checkValid();
        return this.pointer;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PointerWrapperAbstract)) {
            return false;
        }
        PointerWrapperAbstract that = (PointerWrapperAbstract)o;
        return this.pointer == that.pointer;
    }

    public int hashCode() {
        return Long.hashCode(this.pointer);
    }

    public String toString() {
        return this.getClass().getSimpleName() + " pointer (0x" + Long.toHexString(this.pointer).toUpperCase() + ")";
    }
}
