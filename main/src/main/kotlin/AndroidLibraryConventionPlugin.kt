import com.android.build.gradle.LibraryExtension
import com.google.samples.apps.nowinandroid.configureKotlinAndroid
import com.google.samples.apps.nowinandroid.libs
import com.google.samples.apps.nowinandroid.magicLibs
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.kotlin

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.library")
                apply("org.jetbrains.kotlin.android")
            }
            extensions.configure<LibraryExtension> {
                configureKotlinAndroid(this)
                defaultConfig.minSdk = magicLibs.findVersion("api.min").get().toString().toInt()
                defaultConfig.testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                defaultConfig.consumerProguardFiles("consumer-rules.pro")
                compileSdk = magicLibs.findVersion("api.target").get().toString().toInt()
                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_1_8
                    targetCompatibility = JavaVersion.VERSION_1_8
                }
            }
            dependencies {
                add("testImplementation", magicLibs.findLibrary("java.junit").get())
                add("androidTestImplementation", magicLibs.findLibrary("androidx.junit").get())
                add("androidTestImplementation", magicLibs.findLibrary("androidx.espresso.core").get())
                add("implementation", magicLibs.findLibrary("kotlinx.coroutines.core").get())
                add("implementation", magicLibs.findLibrary("androidx.core.ktx").get())
                add("implementation", magicLibs.findLibrary("androidx.lifecycle").get())
            }
        }
    }
}
