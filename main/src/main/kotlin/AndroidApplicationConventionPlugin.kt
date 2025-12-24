import com.android.build.api.dsl.ApplicationExtension
import com.google.samples.apps.nowinandroid.libs
import com.google.samples.apps.nowinandroid.magicLibs
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import java.io.FileInputStream
import java.util.Properties

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.application")
            }
            val keyProps = Properties().apply { load(FileInputStream(rootProject.file("ui/keystore.properties"))) }
            extensions.configure<ApplicationExtension> {
                compileSdk = magicLibs.findVersion("api.target").get().toString().toInt()
                defaultConfig {
                    minSdk = magicLibs.findVersion("api.min").get().toString().toInt()
                    targetSdk = magicLibs.findVersion("api.target").get().toString().toInt()
                    versionCode = 1
                    versionName = keyProps.getProperty("VN") ?: "dev"
                    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                }
                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_18
                    targetCompatibility = JavaVersion.VERSION_18
                }
                signingConfigs {
                    create("open_source") {
                        keyAlias = keyProps.getProperty("keyAlias")
                        keyPassword = keyProps.getProperty("keyPassword")
                        storeFile = rootProject.file("ui/" + keyProps.getProperty("storeFile"))
                        storePassword = keyProps.getProperty("storePassword")
                        enableV1Signing = true
                        enableV2Signing = true
                        //check
                        //enableV3Signing = true
                        //enableV4Signing = true
                    }
                }
                buildTypes {
                    release {
                        isMinifyEnabled = true
                        isShrinkResources = true
                        signingConfig = signingConfigs.findByName("open_source")
                        proguardFiles(
                            getDefaultProguardFile("proguard-android-optimize.txt"),
                            "proguard-rules.pro"
                        )
                    }
                    debug {
                        signingConfig = signingConfigs.findByName("open_source")
                    }
                }
            }
            dependencies {
                add("implementation", magicLibs.findLibrary("androidx.core.ktx").get())
                add("implementation", magicLibs.findLibrary("androidx.lifecycle").get())
                add("testImplementation", magicLibs.findLibrary("java.junit").get())
                add("androidTestImplementation", magicLibs.findLibrary("androidx.junit").get())
                add("androidTestImplementation", magicLibs.findLibrary("androidx.espresso.core").get())
            }
        }
    }
}
