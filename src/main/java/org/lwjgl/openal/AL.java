package org.lwjgl.openal;

import lombok.Getter;
import org.jspecify.annotations.Nullable;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLUtil;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.function.IntFunction;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.ALC.*;
import static org.lwjgl.openal.ALC10.*;
import static org.lwjgl.openal.EXTThreadLocalContext.alcGetThreadContext;
import static org.lwjgl.system.APIUtil.*;
import static org.lwjgl.system.JNI.*;
import static org.lwjgl.system.MemoryUtil.*;

/**
 * This is a wrapper implementation of the LWJGL2 AL class.
 *
 * @author darraghd493, lwjgl3 (original)
 * @since 1.0.0
 *
 * @apiNote This class does not contain neither getDevice() nor getContext() methods.
 * @implNote This class partially uses code from AL.java in LWJGL3.
 */
public class AL {
    /**
     * -- GETTER --
     * Returns whether the OpenAL context has been created.
     */
    @Getter
    private static boolean created;

    private static final ThreadLocal<ALCapabilities> capabilitiesTLS = new ThreadLocal<>();

    @Nullable
    private static ALCapabilities processCaps;

    private static ICD icd = new ICDStatic();

    private static long device, context;

    public static void create() {
        create(null, 44100, 60, false, true);
    }

    public static void create(@Nullable String deviceArguments, int contextFrequency, int contextRefresh, boolean contextSynchronized) {
        create(deviceArguments, contextFrequency, contextRefresh, contextSynchronized, true);
    }

    public static void create(@Nullable String deviceArguments, int contextFrequency, int contextRefresh, boolean contextSynchronized, boolean openDevice) {
        if (created) {
            throw new IllegalStateException("Only one OpenAL context may be instantiated at any one time.");
        }

        init(deviceArguments, contextFrequency, contextRefresh, contextSynchronized, openDevice);
        created = true;
    }

    private static void init(@Nullable String deviceArguments, int contextFrequency, int contextRefresh, boolean contextSynchronized, boolean openDevice) {
        try {
            if (openDevice) {
                device = alcOpenDevice(deviceArguments != null ? ByteBuffer.wrap(deviceArguments.getBytes()) : null);
                if (device == NULL) {
                    throw new IllegalStateException("Failed to open the default OpenAL device.");
                }

                MemoryStack stack = MemoryStack.stackPush();
                ALCCapabilities deviceCaps = ALC.createCapabilities(device);
                context = alcCreateContext(device, contextFrequency == -1 ? null : createAttributeList(contextFrequency, contextRefresh, contextSynchronized ? ALC_TRUE : ALC_FALSE, stack));
                if (context == NULL) {
                    throw new IllegalStateException("Failed to create OpenAL context.");
                }
                stack.close();

                alcMakeContextCurrent(context);
                createCapabilities(deviceCaps);
            }
        } catch (Exception e) { // Prevent further issues
            destroy();
            throw e;
        }
    }

    /**
     * Dummy method.
     */
    @Nullable
    public static ALCcontext getContext() {
        LWJGLUtil.log("AL.getContext() is not implemented.");
        return null;
    }

    /**
     * Dummy method.
     */
    @Nullable
    public static ALCdevice getDevice() {
        LWJGLUtil.log("AL.getDevice() is not implemented.");
        return null;
    }

    /**
     * Destroys the OpenAL context.
     */
    public static void destroy() {
        if (context != -1L) {
            alcMakeContextCurrent(0L);
            alcDestroyContext(context);
            context = -1L;
        }

        if (device != -1L) {
            alcCloseDevice(device);
            device = -1L;
        }

        al_destroy();
        created = false;
    }

    /**
     * Dummy method.
     */
    static void init() {
    }

    /**
     * Destroys the AL context.
     */
    static void al_destroy() {
        setCurrentProcess(null);
    }

    /**
     * Sets the capabilities fo the current process.
     *
     * @param capabilities The capabilities to set.
     *
     * @apiNote Modern method.
     */
    public static void setCurrentProcess(@Nullable ALCapabilities capabilities) {
        processCaps = capabilities;
        capabilitiesTLS.set(null);
        icd.set(capabilities);
    }

    /**
     * Sets the capabilities for the current thread.
     *
     * @param capabilities The capabilities to set.
     *
     * @apiNote Modern method.
     */
    public static void setCurrentThread(@Nullable ALCapabilities capabilities) {
        capabilitiesTLS.set(capabilities);
        icd.set(capabilities);
    }

    /**
     * Returns the capabilities fo the current process.
     *
     * @return The capabilities of the current process.
     *
     * @apiNote Modern method.
     */
    @Nullable
    public static ALCapabilities getCapabilities() {
        ALCapabilities capabilities = capabilitiesTLS.get();
        if (capabilities == null) {
            capabilities = processCaps;
        }
        return capabilities;
    }

    private static ALCapabilities checkCapabilities(@Nullable ALCapabilities caps) {
        if (caps == null) {
            throw new IllegalStateException(
                    """
                            No ALCapabilities instance set for the current thread or process. Possible solutions:
                            \ta) Call AL.createCapabilities() after making a context current.
                            \tb) Call AL.setCurrentProcess() or AL.setCurrentThread() if an ALCapabilities instance already exists."""
            );
        }
        return caps;
    }

    /**
     * Creates a new {@link ALCapabilities} instance for the OpenAL context that is current in the current thread or process.
     *
     * <p>This method calls {@link #setCurrentProcess} (or {@link #setCurrentThread} if applicable) with the new instance before returning.</p>
     *
     * @param alcCaps the {@link ALCCapabilities} of the device associated with the current context
     *
     * @return the ALCapabilities instance
     *
     * @apiNote Directly from LWJGL3.
     */
    public static ALCapabilities createCapabilities(ALCCapabilities alcCaps) {
        return createCapabilities(alcCaps, null);
    }

    /**
     * Creates a new {@link ALCapabilities} instance for the OpenAL context that is current in the current thread or process.
     *
     * @param alcCaps       the {@link ALCCapabilities} of the device associated with the current context
     * @param bufferFactory a function that allocates a {@link PointerBuffer} given a size. The buffer must be filled with zeroes. If {@code null}, LWJGL will
     *                      allocate a GC-managed buffer internally.
     *
     * @return the ALCapabilities instance
     *
     * @apiNote Directly from LWJGL3.
     */
    public static ALCapabilities createCapabilities(ALCCapabilities alcCaps, @Nullable IntFunction<PointerBuffer> bufferFactory) {
        // We'll use alGetProcAddress for both core and extension entry points.
        // To do that, we need to first grab the alGetProcAddress function from
        // the OpenAL native library.
        long alGetProcAddress = getFunctionProvider().getFunctionAddress(NULL, "alGetProcAddress");
        if (alGetProcAddress == NULL) {
            throw new RuntimeException("A core AL function is missing. Make sure that the OpenAL library has been loaded correctly.");
        }

        FunctionProvider functionProvider = functionName -> {
            long address = invokePP(memAddress(functionName), alGetProcAddress);
            if (address == NULL && Checks.DEBUG_FUNCTIONS) {
                apiLogMissing("AL", functionName);
            }
            return address;
        };

        long GetString          = functionProvider.getFunctionAddress("alGetString");
        long GetError           = functionProvider.getFunctionAddress("alGetError");
        long IsExtensionPresent = functionProvider.getFunctionAddress("alIsExtensionPresent");
        if (GetString == NULL || GetError == NULL || IsExtensionPresent == NULL) {
            throw new IllegalStateException("Core OpenAL functions could not be found. Make sure that the OpenAL library has been loaded correctly.");
        }

        String versionString = memASCIISafe(invokeP(AL_VERSION, GetString));
        if (versionString == null || invokeI(GetError) != AL_NO_ERROR) {
            throw new IllegalStateException("There is no OpenAL context current in the current thread or process.");
        }

        APIUtil.APIVersion apiVersion = apiParseVersion(versionString);

        int majorVersion = apiVersion.major;
        int minorVersion = apiVersion.minor;

        int[][] AL_VERSIONS = {
                {0, 1}  // OpenAL 1
        };

        Set<String> supportedExtensions = new HashSet<>(32);

        for (int major = 1; major <= AL_VERSIONS.length; major++) {
            int[] minors = AL_VERSIONS[major - 1];
            for (int minor : minors) {
                if (major < majorVersion || (major == majorVersion && minor <= minorVersion)) {
                    supportedExtensions.add("OpenAL" + major + minor);
                }
            }
        }

        // Parse EXTENSIONS string
        String extensionsString = memASCIISafe(invokeP(AL_EXTENSIONS, GetString));
        if (extensionsString != null) {
            MemoryStack stack = MemoryStack.stackGet();

            StringTokenizer tokenizer = new StringTokenizer(extensionsString);
            while (tokenizer.hasMoreTokens()) {
                String extName = tokenizer.nextToken();
                try (MemoryStack frame = stack.push()) {
                    if (invokePZ(memAddress(frame.ASCII(extName, true)), IsExtensionPresent)) {
                        supportedExtensions.add(extName);
                    }
                }
            }
        }

        if (alcCaps.ALC_EXT_EFX) {
            supportedExtensions.add("ALC_EXT_EFX");
        }
        apiFilterExtensions(supportedExtensions, Configuration.OPENAL_EXTENSION_FILTER);

        ALCapabilities caps = new ALCapabilities(functionProvider, supportedExtensions, bufferFactory == null ? BufferUtils::createPointerBuffer : bufferFactory);

        if (alcCaps.ALC_EXT_thread_local_context && alcGetThreadContext() != NULL) {
            setCurrentThread(caps);
        } else {
            setCurrentProcess(caps);
        }

        return caps;
    }

    /**
     * Returns the capabilities of the ICD.
     *
     * @return The capabilities of the ICD.
     *
     * @apiNote Modern method.
     */
    static ALCapabilities getICD() {
        return check(icd.get());
    }

    /**
     * Pushes the attributes of the context to the stack.
     *
     * @param contextFrequency The frequency of the context.
     * @param contextRefresh The refresh rate of the context.
     * @param contextSynchronized Whether the context is synchronised.
     * @param stack The stack to push the attributes to.
     * @return The attribute list.
     */
    private static IntBuffer createAttributeList(int contextFrequency, int contextRefresh, int contextSynchronized, MemoryStack stack) {
        IntBuffer buffer = stack.callocInt(7);
        buffer.put(0, ALC_FREQUENCY);
        buffer.put(1, contextFrequency);
        buffer.put(2, ALC_REFRESH);
        buffer.put(3, contextRefresh);
        buffer.put(4, ALC_SYNC);
        buffer.put(5, contextSynchronized);
        buffer.put(6, 0);
        return buffer;
    }

    /**
     * Function pointer provider.
     *
     * @apiNote Directly from LWJGL3.
     */
    interface ICD {
        default void set(@Nullable ALCapabilities caps) {}
        @Nullable ALCapabilities get();
    }

    /**
     * Write-once {@link ICD}.
     *
     * <p>This is the default implementation that skips the thread/process lookup. When a new ALCapabilities is set, we compare it to the write-once
     * capabilities. If different function pointers are found, we fall back to the expensive lookup. This will never happen with the OpenAL-Soft
     * implementation.</p>
     *
     * @apiNote Directly from LWJGL3.
     */
    static class ICDStatic implements ICD {
        @Nullable
        private static ALCapabilities tempCaps;

        @SuppressWarnings("AssignmentToStaticFieldFromInstanceMethod")
        @Override
        public void set(@Nullable ALCapabilities caps) {
            if (tempCaps == null) {
                tempCaps = caps;
            } else if (caps != null && caps != tempCaps && ThreadLocalUtil.areCapabilitiesDifferent(tempCaps.addresses, caps.addresses)) {
                apiLog("[WARNING] Incompatible context detected. Falling back to thread/process lookup for AL contexts.");
                icd = AL::getCapabilities; // fall back to thread/process lookup
            }
        }

        @Override
        public ALCapabilities get() {
            return ICDStatic.WriteOnce.caps;
        }

        private static final class WriteOnce {
            // This will be initialized the first time get() above is called
            static final ALCapabilities caps;

            static {
                ALCapabilities tempCaps = ICDStatic.tempCaps;
                if (tempCaps == null) {
                    throw new IllegalStateException("No ALCapabilities instance has been set");
                }
                caps = tempCaps;
            }
        }
    }
}
