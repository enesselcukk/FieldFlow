package com.example.utils.security

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RootDetectorTest {

    @Test
    fun `compromised when test-keys in tags`() {
        val detector = RootDetector(fileExists = { false }, buildTags = "release-keys,test-keys")
        assertTrue(detector.isDeviceCompromised())
    }

    @Test
    fun `compromised when su binary path exists`() {
        val detector = RootDetector(
            fileExists = { it == "/system/bin/su" },
            buildTags = "release-keys"
        )
        assertTrue(detector.isDeviceCompromised())
    }

    @Test
    fun `not compromised when clean`() {
        val detector = RootDetector(
            fileExists = { false },
            buildTags = "release-keys"
        )
        assertFalse(detector.isDeviceCompromised())
    }

    @Test
    fun `paths list is non-empty`() {
        assertTrue(RootDetector.CompromiseIndicatorPaths.isNotEmpty())
    }
}
