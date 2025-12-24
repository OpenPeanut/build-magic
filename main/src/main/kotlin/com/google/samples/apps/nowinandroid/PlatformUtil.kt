package com.google.samples.apps.nowinandroid

object PlatformUtil {

    fun ndkArch() = windows { "windows-x86_64" }.mac { "darwin-x86_64" }.linux { "linux-x86_64" }.expect("Unknown os: ${System.getProperty("os.name")}")

    fun<T> windows(provider: () -> T): PlatformResult<T>{
        val osName = System.getProperty("os.name").lowercase()
        if (osName.contains("windows")) return PlatformResult.OK(provider())
        return PlatformResult.FAIL()
    }

    fun<T> PlatformResult<T>.mac(provider: () -> T): PlatformResult<T>{
        if (this.isOK()) return this
        val osName = System.getProperty("os.name").lowercase()
        if (osName.contains("mac")) return PlatformResult.OK(provider())
        return PlatformResult.FAIL()
    }

    fun<T> PlatformResult<T>.linux(provider: () -> T): PlatformResult<T>{
        if (this.isOK()) return this
        val osName = System.getProperty("os.name").lowercase()
        if (osName.contains("linux")) return PlatformResult.OK(provider())
        return PlatformResult.FAIL()
    }

    fun<T> PlatformResult<T>.default(provider: () -> T): T{
        if (this.isOK()) return this.expect("")
        return provider()
    }

    fun<T> PlatformResult<T>.unix(provider: () -> T): PlatformResult<T>{
        if (this.isOK()) return this
        val osName = System.getProperty("os.name").lowercase()
        if (osName.contains("linux")||osName.contains("mac")) return PlatformResult.OK(provider())
        return PlatformResult.FAIL()
    }
}