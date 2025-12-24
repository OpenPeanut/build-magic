package com.google.samples.apps.nowinandroid

import org.gradle.api.Project
import java.io.File

class CrossCompiler(private val workDir: File) {
    fun configure(configure: MutableList<String>) {
        runCmd("./configure", *configure.toTypedArray(), workDir = workDir).getOrThrow()
    }

    fun make(j: Int) {
        runCmd("make", "-j$j", workDir = workDir).getOrThrow()
    }

    fun makeInstall() {
        runCmd("make", "install", workDir = workDir).getOrThrow()
    }
}

class SimpleCrossCompiler(private val project: Project, private val repository: GitRepository) {
    init {
        cloneIfNotExist()
    }

    val repoPath: File get() = project.file(repository.name)
    val prefixPath: File get() = File(File(project.file("build"), repository.name), repository.prefix)

    private fun cloneIfNotExist() {
        if (repoPath.exists()) return
        runCmd(
            "git",
            "clone",
            "--depth",
            "1",
            "--branch",
            repository.branch,
            repository.url,
            repository.name,
            workDir = project.projectDir
        )
        File(repoPath.path, ".git").deleteRecursively()
    }

    fun compile(task: CMakeTask) {
        runCmd(*task.onConfigure, workDir = repoPath) {
            set(task.env)
        }.getOrThrow()
        runCmd("make", "-j${task.j}", workDir = repoPath) {
            set(task.env)
        }.getOrThrow()
        runCmd("make", "install", workDir = repoPath) {
            set(task.env)
        }.getOrThrow()
    }

    fun MutableMap<String, String>.set(env: Map<String, String>) {
        for ((k, v) in env) {
            if (k == "PATH") {
                put(k, v + ":" + get(k))
            } else put(k, v)
        }
        this.remove("ANDROID_SDK_ROOT")
    }
}

data class GitRepository(
    val url: String,
    val name: String,
    val branch: String,
    val prefix: String,
)