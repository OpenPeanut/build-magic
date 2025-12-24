package com.google.samples.apps.nowinandroid

import com.google.samples.apps.nowinandroid.PlatformUtil.ndkArch
import org.gradle.api.Project
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.Properties
import kotlin.concurrent.thread

fun Project.getClang(ndkVersion: String, api: String): String {
    val p = getToolchain(ndkVersion) + "/aarch64-linux-android$api-clang"
    if (!File(p).exists()) TODO("Unable to find: $p")
    return p
}

fun Project.getToolchain(ndkVersion: String): String {
    val p = getAndroidNDK(ndkVersion)+"/toolchains/llvm/prebuilt/${ndkArch()}/bin"
    if (!File(p).exists()) TODO("Unable to find: $p")
    return p
}

fun Project.getAndroidNDK(ndkVersion: String): String {
    val v = getSDKPath()
    val p = "$v/ndk/$ndkVersion"
    if (!File(p).exists()) TODO("Unable to find: $p, check your android sdk.")
    return p
}

fun Project.getSDKPath(): String {
    val localProperties = Properties().apply {
        val f = project.rootProject.file("local.properties")
        if (!f.exists()) {
            return@apply
        }
        load(f.inputStream())
    }.getProperty("sdk.dir")
    if (localProperties == null){
        val env = System.getenv("ANDROID_HOME")?: System.getenv("ANDROID_SDK_ROOT")
        if (env == null) TODO("You must set sdk.dir in your local.properties or set ANDROID_HOME or ANDROID_SDK_ROOT in your environment.")
        return env
    }
    return localProperties
}

fun runCmd(
    vararg cmd: String,
    workDir: File = File("./"),
    env: MutableMap<String, String>.() -> Unit = {}
): Result<String> {
    val pb = ProcessBuilder().command(*cmd).directory(workDir)
    pb.environment().apply(env)
    val p = pb.start()
    val br = BufferedReader(InputStreamReader(p.inputStream))
    val ebr = BufferedReader(InputStreamReader(p.errorStream))
    thread {
        br.forEachLine {
            println(it)
        }
    }
    thread {
        ebr.forEachLine {
            println("\u001B[1m\u001B[31m$it\u001B[0m")
        }
    }
    return if (p.waitFor() == 0) {
        Result.success("")
    } else {
        Result.failure(RuntimeException(pb.command().joinToString(" ")))
    }
}