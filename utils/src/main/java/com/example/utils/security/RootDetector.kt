package com.example.utils.security

import android.os.Build
import java.io.File

class RootDetector internal constructor(
    private val fileExists: (String) -> Boolean = { File(it).exists() },
    private val buildTags: String = Build.TAGS ?: "",
    private val buildType: String = Build.TYPE ?: "",
    private val debuggableProperty: () -> String? = { readRoDebuggable() },
) {

    fun isDeviceCompromised(): Boolean =
        hasTestKeys() ||
            isNonRetailBuildType() ||
            isRoDebuggable() ||
            hasSuOrMagiskArtifacts()

    private fun hasTestKeys(): Boolean = buildTags.contains("test-keys")

    private fun isNonRetailBuildType(): Boolean =
        buildType == "eng" || buildType == "userdebug"

    private fun isRoDebuggable(): Boolean = debuggableProperty() == "1"

    private fun hasSuOrMagiskArtifacts(): Boolean =
        CompromiseIndicatorPaths.any { fileExists(it) }

    companion object {
        internal val CompromiseIndicatorPaths = listOf(
            "/system/app/Superuser.apk",
            "/system/bin/su",
            "/system/xbin/su",
            "/sbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/data/local/su",
            "/system/bin/failsafe/su",
            "/su/bin/su",
            "/sbin/magisk",
            "/data/adb/magisk",
            "/data/adb/magisk.db"
        )

        fun isDeviceCompromised(): Boolean = RootDetector().isDeviceCompromised()
    }
}

private fun readRoDebuggable(): String? =
    try {
        val clazz = Class.forName("android.os.SystemProperties")
        val raw = try {
            clazz.getMethod("get", String::class.java, String::class.java)
                .invoke(null, "ro.debuggable", "") as? String
        } catch (_: ReflectiveOperationException) {
            clazz.getMethod("get", String::class.java)
                .invoke(null, "ro.debuggable") as? String
        }
        raw?.trim()?.takeIf { it.isNotEmpty() }
    } catch (_: Exception) {
        null
    }
