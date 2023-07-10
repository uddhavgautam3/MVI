import org.gradle.api.tasks.Delete

buildscript {
    dependencies {
        classpath("org.jacoco:org.jacoco.core:0.8.9")
    }
    repositories {
        //I am using settings.gradle
    }
}

plugins {
    id("com.android.application") version "8.0.2" apply false
    id("com.android.library") version "8.0.2" apply false
    id("com.google.dagger.hilt.android") version "2.44" apply false
    id("org.jetbrains.kotlin.android") version "1.8.22" apply false
    id("maven-publish")
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}