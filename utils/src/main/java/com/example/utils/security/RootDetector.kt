package com.example.utils.security

import android.os.Build
import java.io.File

class RootDetector private constructor(
    private val fileExists: (String) -> Boolean = { File(it).exists() },
    private val buildTags: String = Build.TAGS,
) {

    fun isDeviceCompromised(): Boolean =
        hasTestKeys() || hasSuOrMagiskArtifacts()

    private fun hasTestKeys(): Boolean = buildTags.contains("test-keys")

    private fun hasSuOrMagiskArtifacts(): Boolean = CompromiseIndicatorPaths.any { fileExists(it) }

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
