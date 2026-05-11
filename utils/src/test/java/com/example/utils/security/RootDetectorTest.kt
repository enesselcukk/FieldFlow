package com.example.utils.security

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RootDetectorTest {

    @Test
    fun detectsTestKeysInBuildTags() {
        val detector = RootDetector(
            fileExists = { false },
            buildTags = "release-keys,test-keys"
        )
        assertTrue(detector.isDeviceCompromised())
    }

    @Test
    fun detectsSuBinaryPathPresent() {
        val detector = RootDetector(
            fileExists = { it == "/system/bin/su" },
            buildTags = "release-keys"
        )
        assertTrue(detector.isDeviceCompromised())
    }

    @Test
    fun reportsSafeWhenClean() {
        val detector = RootDetector(
            fileExists = { false },
            buildTags = "release-keys"
        )
        assertFalse(detector.isDeviceCompromised())
    }

    @Test
    fun compromiseIndicatorPathsNonEmpty() {
        assertTrue(RootDetector.CompromiseIndicatorPaths.isNotEmpty())
    }
}
