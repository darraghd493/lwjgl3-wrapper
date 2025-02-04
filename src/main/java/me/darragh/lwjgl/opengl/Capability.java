package me.darragh.lwjgl.opengl;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates a field that represents an OpenGL capability.
 * <p>
 * Even though you could achieve the same result by using standard reflection and selecting fields matching a pattern,
 * this is more explicit and does not require an exception within renaming (obfuscation).
 *
 * @author darraghd493
 * @since 1.0.0
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Capability {
    String name();
}
