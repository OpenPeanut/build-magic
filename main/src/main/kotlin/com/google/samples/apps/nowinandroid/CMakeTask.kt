package com.google.samples.apps.nowinandroid

data class CMakeTask(
    val onConfigure: Array<String> = arrayOf(),
    val onMake: Array<String> = arrayOf(),
    val onMakeInstall: Array<String> = arrayOf(),
    val env: Map<String, String> = mapOf(),
    val j: Int = 8,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CMakeTask

        if (!onConfigure.contentEquals(other.onConfigure)) return false
        if (!onMake.contentEquals(other.onMake)) return false
        if (!onMakeInstall.contentEquals(other.onMakeInstall)) return false
        if (env != other.env) return false

        return true
    }

    override fun hashCode(): Int {
        var result = onConfigure.contentHashCode()
        result = 31 * result + onMake.contentHashCode()
        result = 31 * result + onMakeInstall.contentHashCode()
        result = 31 * result + env.hashCode()
        return result
    }
}