package com.google.samples.apps.nowinandroid

sealed class PlatformResult<T> {
    class OK<T>(val value: T): PlatformResult<T>()
    class FAIL<T>: PlatformResult<T>()

    fun expect(msg: String): T {
        when(this) {
            is OK -> return value
            is FAIL -> throw RuntimeException(msg)
        }
    }

    fun isOK(): Boolean = this is OK
}
