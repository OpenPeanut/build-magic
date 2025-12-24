/*
 * Copyright 2023 The Android Open Source Project
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

import com.google.samples.apps.nowinandroid.CMakeTask
import com.google.samples.apps.nowinandroid.CrossCompiler
import com.google.samples.apps.nowinandroid.GitRepository
import com.google.samples.apps.nowinandroid.PlatformUtil.default
import com.google.samples.apps.nowinandroid.PlatformUtil.windows
import com.google.samples.apps.nowinandroid.SimpleCrossCompiler
import com.google.samples.apps.nowinandroid.getAndroidNDK
import com.google.samples.apps.nowinandroid.getClang
import com.google.samples.apps.nowinandroid.getSDKPath
import com.google.samples.apps.nowinandroid.getToolchain
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

class FFmpegConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val ff = windows {
                tasks.register("downloadPrebuilds") {

                }
            }.default {
                tasks.register("buildFFmpeg") {
                    inputs.file(File(project.file("FFmpeg"), "RELEASE"))
                    outputs.dir(File(project.file("build"), "ffmpeg"))
                    doLast {
                        val ext = project.extensions.getByType(FFmpegExtension::class.java)
                        println(ext.getConfiguration())
                        val compiler = CrossCompiler(project.file(ext.src))
                        compiler.configure(ext.params)
                        compiler.make(8)
                        compiler.makeInstall()
                    }
                }
            }
            gradle.projectsEvaluated {
                project(":data:native:impl").tasks.findByName("buildNatives")?.apply {
                    this.dependsOn(ff)
                }
            }
            tasks.register("cleanNative") {
                doLast {
                    project.file("build").deleteRecursively()
                }
            }
        }
    }
}

fun Project.ffmpeg(configure: FFmpegExtension.() -> Unit) {
    extensions.create("ffmpeg", FFmpegExtension::class.java, this)
        .apply(configure)
        .apply { this.preConfig() }
        .apply { this.postConfig() }
        .apply { this.buildConfig() }
}

open class FFmpegExtension(private val project: Project) {
    val params: MutableList<String> = mutableListOf()
    var api: Int = 32
    var out: String = ""
    var ndk: String = ""
    var src: String = ""
    val cflags: MutableList<String> = mutableListOf()
    val ldflags: MutableList<String> = mutableListOf()

    fun preConfig() {
        params.apply {
            add("--prefix=${project.file("build").path}/$out")
            add("--enable-cross-compile")
        }
    }

    fun buildConfig() {
        val ndk = project.getClang(ndkVersion = this.ndk, api = this.api.toString())
        val toolchain = File(ndk).parent
        val sysroot = File(toolchain).parent
        params.apply {
            add("--cc=$ndk")
            add("--cxx=$ndk++")
            add("--nm=$toolchain/llvm-nm")
            add("--strip=$toolchain/llvm-strip")
            add("--sysroot=$sysroot/sysroot")
            add("--extra-cflags=${cflags.joinToString(" ")} -fPIE -fPIC -DANDROID -fdata-sections -ffunction-sections -fomit-frame-pointer -O3")
            add("--extra-ldflags=-pie -Wl,--gc-sections ${ldflags.joinToString(" ")}")
        }
    }

    fun postConfig() {
        params.apply {
            add("--disable-parsers")
            add("--disable-filters")
            add("--disable-bsfs")
            add("--disable-indevs")
            add("--disable-outdevs")
            add("--disable-ffprobe")
            add("--disable-ffmpeg")
            add("--disable-ffplay")
            add("--disable-debug")
            add("--disable-swresample")
            add("--disable-avfilter")
            add("--disable-avdevice")
            add("--enable-shared")
            add("--disable-static")
            add("--target-os=android")
            add("--arch=arm64")
            add("--cpu=armv8-a")
        }
    }

    fun FFmpegExtension.openssl() {
        params.apply {
            add("--enable-openssl")
            add("--disable-gpl")
            add("--enable-version3")
        }
        val r = GitRepository(
            url = "https://github.com/openssl/openssl.git",
            name = "openssl.git",
            branch = "openssl-3.1.4",
            prefix = "arm64-v8a",
        )
        val openssl = SimpleCrossCompiler(project, r)
        cflags.add("-I${openssl.prefixPath.path}/include")
        ldflags.add("-L${openssl.prefixPath.path}/lib")
        windows {}.default {
            val ssl = project.tasks.register("buildOpenssl") {
                inputs.file(File(openssl.repoPath, "VERSION.dat"))
                outputs.dir(openssl.prefixPath)
                doLast {
                    openssl.compile(
                        CMakeTask(
                            onConfigure = arrayOf(
                                "./Configure",
                                "android-arm64",
                                "-D__ANDROID_API__=${this@openssl.api}",
                                "--prefix=${openssl.prefixPath.path}"
                            ),
                            env = mapOf(
                                "ANDROID_NDK" to project.getAndroidNDK(ndkVersion = this@openssl.ndk),
                                "ANDROID_NDK_HOME" to project.getAndroidNDK(ndkVersion = this@openssl.ndk),
                                "ANDROID_NDK_ROOT" to project.getAndroidNDK(ndkVersion = this@openssl.ndk),
                                "ANDROID_SDK" to project.getSDKPath(),
                                "PATH" to project.getToolchain(ndkVersion = this@openssl.ndk)
                            )
                        )
                    )
                }
            }
            project.tasks.findByName("buildFFmpeg")!!.apply {
                dependsOn(ssl)
            }
        }

    }

    fun getConfiguration(): String = params.joinToString(
        separator = " \\${System.lineSeparator()}\t",
        prefix = "./configure \\${System.lineSeparator()}\t"
    )

    fun FFmpegExtension.decoders(func: Decoders.() -> Unit) {
        val b = mutableListOf<String>()
        Decoders(b).apply(func)
        params.addAll(b)
    }

    inner class Decoders(private val c: MutableList<String>) {
        init {
            c.add("--disable-decoders")
        }

        fun Decoders.Ass() {
            c.add("--enable-decoder=ass")
        }

        fun Decoders.H264() {
            c.add("--enable-decoder=h264")
        }

        fun Decoders.Hevc() {
            c.add("--enable-decoder=hevc")
        }
    }

    fun FFmpegExtension.encoders(func: Encoders.() -> Unit) {
        val b = mutableListOf<String>()
        Encoders(b).apply(func)
        params.addAll(b)
    }

    inner class Encoders(private val c: MutableList<String>) {
        init {
            c.add("--disable-encoders")
        }

        fun Encoders.Srt() {
            c.add("--enable-encoder=srt")
        }

        fun Encoders.Png() {
            c.add("--enable-encoder=png")
        }
    }

    fun FFmpegExtension.demuxers(func: Demuxers.() -> Unit) {
        val b = mutableListOf<String>()
        Demuxers(b).apply(func)
        params.addAll(b)
    }

    inner class Demuxers(private val c: MutableList<String>) {
        init {
            c.add("--disable-demuxers")
        }

        fun Demuxers.Ass() {
            c.add("--enable-demuxer=ass")
        }

        fun Demuxers.Mov() {
            c.add("--enable-demuxer=mov")
        }

        fun Demuxers.Matroska() {
            c.add("--enable-demuxer=matroska")
        }
    }

    fun FFmpegExtension.muxers(func: Muxers.() -> Unit) {
        val b = mutableListOf<String>()
        Muxers(b).apply(func)
        params.addAll(b)
    }

    inner class Muxers(private val c: MutableList<String>) {
        init {
            c.add("--disable-muxers")
        }

        fun Muxers.Srt() {
            c.add("--enable-muxer=srt")
        }
    }

    fun FFmpegExtension.protocols(func: Protocols.() -> Unit) {
        val b = mutableListOf<String>()
        Protocols(b).apply(func)
        params.addAll(b)
    }

    inner class Protocols(private val c: MutableList<String>) {
        init {
            c.add("--disable-protocols")
        }

        fun Protocols.LocalFile() {
            c.add("--enable-protocol=file")
        }

        fun Protocols.Http() {
            c.add("--enable-protocol=http")
        }

        fun Protocols.Https() {
            c.add("--enable-protocol=https")
        }
    }
}
