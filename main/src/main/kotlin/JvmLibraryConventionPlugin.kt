import com.google.samples.apps.nowinandroid.configureKotlinJvm
import com.google.samples.apps.nowinandroid.libs
import com.google.samples.apps.nowinandroid.magicLibs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.kotlin

class JvmLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jetbrains.kotlin.jvm")
            }
            configureKotlinJvm()
            dependencies {
                add("testImplementation", kotlin("test"))
                add("implementation", magicLibs.findLibrary("kotlinx.coroutines.core").get())
            }
        }
    }
}
