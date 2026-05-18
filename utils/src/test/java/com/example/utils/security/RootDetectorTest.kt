package com.example.utils.security

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RootDetectorTest {

    @Test
    fun detectsTestKeysInBuildTags() {
        val detector = RootDetector(
            fileExists = { false },
            buildTags = "release-keys,test-keys",
            buildType = "user",
            debuggableProperty = { null }
        )
        assertTrue(detector.isDeviceCompromised())
    }

    @Test
    fun detectsSuBinaryPathPresent() {
        val detector = RootDetector(
            fileExists = { it == "/system/bin/su" },
            buildTags = "release-keys",
            buildType = "user",
            debuggableProperty = { null }
        )
        assertTrue(detector.isDeviceCompromised())
    }

    @Test
    fun detectsUserdebugBuildType() {
        val detector = RootDetector(
            fileExists = { false },
            buildTags = "release-keys",
            buildType = "userdebug",
            debuggableProperty = { null }
        )
        assertTrue(detector.isDeviceCompromised())
    }

    @Test
    fun detectsRoDebuggableWhenPropertyIsOne() {
        val detector = RootDetector(
            fileExists = { false },
            buildTags = "release-keys",
            buildType = "user",
            debuggableProperty = { "1" }
        )
        assertTrue(detector.isDeviceCompromised())
    }

    @Test
    fun reportsSafeWhenClean() {
        val detector = RootDetector(
            fileExists = { false },
            buildTags = "release-keys",
            buildType = "user",
            debuggableProperty = { "0" }
        )
        assertFalse(detector.isDeviceCompromised())
    }

    @Test
    fun compromiseIndicatorPathsNonEmpty() {
        assertTrue(RootDetector.CompromiseIndicatorPaths.isNotEmpty())
    }
}
