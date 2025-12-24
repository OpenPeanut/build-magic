import com.google.samples.apps.nowinandroid.libs
import com.google.samples.apps.nowinandroid.magicLibs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class DaggerLibraryPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.google.devtools.ksp")
            }
            dependencies {
                add("implementation", magicLibs.findLibrary("dagger").get())
                add("ksp", magicLibs.findLibrary("dagger.compiler").get())
            }
        }
    }
}
