/*
 * Copyright (c) 2022. Maxim Perelshtein.
 *  http://github.com/perelshtein
 */

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.10"
    id("com.github.johnrengelman.shadow") version "6.0.0"
    application
}

group = "com.github.perelshtein"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("MainKt")
}

tasks.withType<Jar> {
    // Otherwise you'll get a "No main manifest attribute" error
    manifest {
        attributes["Main-Class"] = "MainKt"
    }
}

project.setProperty("mainClassName", "MainKt")
