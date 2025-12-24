import com.google.samples.apps.nowinandroid.getClang
import com.google.samples.apps.nowinandroid.runCmd
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

class GoConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val goTask = tasks.register("buildGolang") {
                val goExtension = project.extensions.getByType(GoExtension::class.java)
                inputs.dir(goExtension.src)
                outputs.dir(project.file("build"))
                doLast {
                    checkGoEnv()
                    val ndk = project.getClang(goExtension.ndk, goExtension.api)
                    callBuild(path=File(projectDir, goExtension.src), ndk, goExtension, project)
                }
            }
            gradle.projectsEvaluated {
                project(":data:native:impl").tasks.findByName("buildNatives")?.apply {
                    this.dependsOn(goTask)
                }
            }
            tasks.register("cleanNative") {
                doLast {
                    project.file("build").deleteRecursively()
                }
            }
        }
    }

    private fun checkGoEnv() {
        runCmd("go", "version").getOrElse { TODO("You must set GoEnv for now.") }
    }

    private fun callBuild(path: File, cc: String, ext: GoExtension, project: Project) {
        println("path=${path}")
        println("cc=${cc}")
        runCmd(
            "go", "build",
            "-buildmode=c-shared",
            "-ldflags=-s -w -extldflags=-Wl,-soname,lib${ext.lib}.so",
            "-o", "${project.file("build")}/lib${ext.lib}.so",
            workDir = path
        ) {
            put("GOOS", "android")
            put("GOARCH", "arm64")
            put("CGO_ENABLED", "1")
            put("CC", cc)
        }.getOrThrow()
    }
}

fun Project.go(configure: GoExtension.() -> Unit) {
    extensions.create("go", GoExtension::class.java).apply(configure)
}

open class GoExtension {
    var src: String = ""
    var lib: String = ""
    var ndk: String = ""
    var api: String = "32"
}
