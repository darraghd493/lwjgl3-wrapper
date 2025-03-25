plugins {
    id("java")
    id("maven-publish")
}

// Project properties:
val baseGroup: String by project
val lwjglVersion: String by project
val lwjglNatives: String by project

// Toolchains:
java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

// Dependencies:
repositories {
    mavenCentral()
}

dependencies {
    // Lombok
    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")
    testCompileOnly("org.projectlombok:lombok:1.18.34")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.34")

    // JSpecify
    implementation("org.jspecify:jspecify:1.0.0")

    // LWJGL
    implementation(platform("org.lwjgl:lwjgl-bom:$lwjglVersion"))
    implementation("org.lwjgl", "lwjgl")
    implementation("org.lwjgl", "lwjgl-glfw")
    implementation("org.lwjgl", "lwjgl-openal")
    implementation("org.lwjgl", "lwjgl-opengl")
    runtimeOnly("org.lwjgl", "lwjgl", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-glfw", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-openal", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-opengl", classifier = lwjglNatives)

    // JInput
    implementation("net.java.jinput:jinput:2.0.10")

    // Commons
    implementation("org.apache.commons:commons-lang3:3.17.0")
}

// Tasks:
tasks.compileJava {
    options.encoding = "UTF-8"
}

tasks.javadoc {
    options.encoding = "UTF-8"
    (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
    isFailOnError = false
}

tasks.register<Jar>("sourcesJar") {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

tasks.register<Jar>("javadocJar") {
    archiveClassifier.set("javadoc")
    from(tasks.javadoc)
}

// Publishing:
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = "me.darragh"
            artifactId = "lwjgl3-wrapper"
            version = project.version.toString()

            pom {
                name.set("lwjgl3-wrapper")
                description.set("An experimental lwjgl3 wrapper")
                url.set("https://github.com/darraghd493/lwjgl3-wrapper")
                properties.set(mapOf(
                    "java.version" to "17",
                    "project.build.sourceEncoding" to "UTF-8",
                    "project.reporting.outputEncoding" to "UTF-8"
                ))
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://github.com/darraghd493/lwjgl3-wrapper/blob/main/LICENSE")
                    }
                }
                developers {
                    developer {
                        id.set("darraghd493")
                        name.set("Darragh")
                    }
                }
                organization {
                    name.set("Fentanyl")
                    url.set("https://fentanyl.dev")
                }
                scm {
                    connection.set("scm:git:git://github.com/darraghd493/lwjgl3-wrapper.git")
                    developerConnection.set("scm:git:ssh://github.com/darraghd493/lwjgl3-wrapper.git")
                    url.set("https://github.com/darraghd493/lwjgl3-wrapper")
                }
            }

            java {
                withSourcesJar()
                withJavadocJar()
            }
        }
    }

    repositories {
        mavenLocal()
        maven {
            name = "darraghsRepo"
            url = uri("https://repo.darragh.website/releases")
            credentials {
                username = System.getenv("REPO_TOKEN")
                password = System.getenv("REPO_SECRET")
            }
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
}
