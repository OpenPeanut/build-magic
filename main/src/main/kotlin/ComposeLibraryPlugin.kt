import com.android.build.gradle.LibraryExtension
import com.google.samples.apps.nowinandroid.libs
import com.google.samples.apps.nowinandroid.magicLibs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class ComposeLibraryPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jetbrains.kotlin.plugin.compose")
            }
            dependencies {
                // 使用 Compose BOM 管理版本
                add("implementation", platform(magicLibs.findLibrary("androidx.compose.bom").get()))

                // 添加 Compose 相关库
                add("implementation", magicLibs.findLibrary("androidx.ui").get())
                add("implementation", magicLibs.findLibrary("androidx.ui.graphics").get())
                add("implementation", magicLibs.findLibrary("androidx.ui.tooling.preview").get())
                add("implementation", magicLibs.findLibrary("androidx.compose.icons").get())
                add("implementation", magicLibs.findLibrary("androidx.material3").get())
                add("implementation", magicLibs.findLibrary("navigation.compose").get())

                // 添加 Compose 单元测试相关库
                add("androidTestImplementation", platform(magicLibs.findLibrary("androidx.compose.bom").get()))
                add("androidTestImplementation", magicLibs.findLibrary("androidx.ui.test.junit4").get())
                add("debugImplementation", magicLibs.findLibrary("androidx.ui.tooling").get())
                add("debugImplementation", magicLibs.findLibrary("androidx.ui.tooling.preview").get())
                add("debugImplementation", magicLibs.findLibrary("androidx.ui.test.manifest").get())
            }
        }
    }
}
