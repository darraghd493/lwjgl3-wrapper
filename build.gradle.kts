import java.io.BufferedReader

plugins {
    id("java")
    id("maven-publish")
    id("com.github.johnrengelman.shadow") version "8.1.1"
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

val shadowImpl: Configuration by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
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

tasks.shadowJar {
    archiveClassifier.set("shadowed")
    configurations = listOf(shadowImpl)
    doLast {
        configurations.forEach {
            println("Copying dependencies into mod: ${it.files}")
        }
    }
}

// Publishing:
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = "me.darragh"
            artifactId = "event-bus"
            version = project.version.toString()

            pom {
                name.set("Event Bus")
                description.set("A simple event bus for Java")
                url.set("https://github.com/Fentanyl-Client/event-bus")
                properties.set(mapOf(
                    "java.version" to "17",
                    "project.build.sourceEncoding" to "UTF-8",
                    "project.reporting.outputEncoding" to "UTF-8"
                ))
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://github.com/Fentanyl-Client/event-bus/blob/main/LICENSE")
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
                    connection.set("scm:git:git://github.com/Fentanyl-Client/event-bus.git")
                    developerConnection.set("scm:git:ssh://github.com/Fentanyl-Client/event-bus.git")
                    url.set("https://github.com/Fentanyl-Client/event-bus")
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
            url = uri("https://repo.darragh.website/snapshots")
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
