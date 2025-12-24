import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

group = "com.peanut.nas.buildlogic"

// Configure the build-logic plugins to target JDK 18
// This matches the JDK used to build the project, and is not related to what is running on device.
java {
    sourceCompatibility = JavaVersion.VERSION_18
    targetCompatibility = JavaVersion.VERSION_18
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_18
    }
}

dependencies {
    compileOnly(libs.android.gradle)
    compileOnly(libs.android.tools.common)
    compileOnly(libs.kotlin.gradle.plugin)
}

tasks {
    validatePlugins {
        enableStricterValidation = true
        failOnWarning = true
    }
}

gradlePlugin {
    plugins {
        register("androidLibrary") {
            id = "peanut.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidApplication") {
            id = "peanut.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("jvmLibrary") {
            id = "peanut.jvm.library"
            implementationClass = "JvmLibraryConventionPlugin"
        }
        register("HiltLibrary") {
            id = "peanut.android.hilt"
            implementationClass = "HiltConventionPlugin"
        }
        register("dagger") {
            id = "peanut.android.dagger"
            implementationClass = "DaggerLibraryPlugin"
        }
        register("GolangBuildPlugin") {
            id = "peanut.go.build"
            implementationClass = "GoConventionPlugin"
        }
        register("FFmpegBuildPlugin") {
            id = "peanut.ffmpeg.build"
            implementationClass = "FFmpegConventionPlugin"
        }
        register("composeLibrary") {
            id = "peanut.compose.library"
            implementationClass = "ComposeLibraryPlugin"
        }
    }
}
