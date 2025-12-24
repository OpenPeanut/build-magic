## 接入
1. add code to settings.gradle.kts(root)
```kts
pluginManagement {
    includeBuild("build-magic")
    //...
}
dependencyResolutionManagement {
    //...
    versionCatalogs {
        create("magicLibs") {
            from(files("build-magic/magic.versions.toml"))
        }
    }
}
```
2. definds kgp you are using
```toml
[versions]
agp = "8.13.2"
kgp = "2.0.0"
buildTools = "31.13.0"
[libraries]
android-gradle = { module = "com.android.tools.build:gradle", version.ref = "agp" }
android-tools-common = { group = "com.android.tools", name = "common", version.ref = "buildTools" }
kotlin-gradle-plugin = { module = "org.jetbrains.kotlin:kotlin-gradle-plugin", version.ref = "kgp" }
```
