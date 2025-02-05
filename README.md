# lwjgl3-wrapper

This is a basic and heavily stripped drop-in replacement LWJGL 2.9.4 (nightly-20150209) -> LWJGL 3.3.4 wrapper for MCP919.

It does not maintain the same code practices as the original LWJGL 2.9.4, and is not intended for use outside MCP919.

## Installation

### Gradle

```kotlin
repositories {
    maven {
        name = "darraghsRepositoryReleases"
        url = uri("https://repo.darragh.website/releases")
    }
}

dependencies {
    implementation("me.darragh:lwjgl3-wrapper:{version}")
}
```

_This project is also available via. Jitpack. View more information [here](https://jitpack.io/#Fentanyl-Client/lwjgl3-wrapper)._

## Usage:

1. Add the matching LWJGL version to your project
2. Add the `lwjgl3-wrapper` dependency to your project
    - This must be overwriting classes from LWJGL
3. Update OpenGL/AL references, e.g., `GL11.glFog` -> `GL11.glFogfv`
    - In MCP919, this is pretty easy but takes about 5 - 10 minutes
    - Make sure to include paulscode sound library - I've posted updated classes for it [here](https://gist.github.com/darraghd493/a1b59d98ee790eea97d4a5fcbcca8332)
4. Done!
