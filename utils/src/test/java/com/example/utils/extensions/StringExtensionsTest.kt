package com.example.utils.extensions

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StringExtensionsTest {

    @Test
    fun containsLabelKeyExactTurkishMatch() {
        assertTrue("isim".containsLabelKey("İsim"))
    }

    @Test
    fun containsLabelKeyPrefixWithColon() {
        assertTrue("TC Kimlik No: 123".containsLabelKey("TC Kimlik No"))
    }

    @Test
    fun containsLabelKeyWordBoundary() {
        assertTrue("foo Seri No bar".containsLabelKey("Seri No"))
    }

    @Test
    fun containsLabelKeyCollapsesWhitespace() {
        assertTrue("İsim".containsLabelKey("  İsim  "))
    }

    @Test
    fun containsLabelKeyNegative() {
        assertFalse("Unrelated".containsLabelKey("Soyisim"))
    }

    @Test
    fun sanitizeIdentityCandidateStripsNoiseAndLeadingNonLetters() {
        assertEquals(
            "ALİ",
            "   (s) 123  ALİ  ".sanitizeIdentityCandidate()
        )
    }

    @Test
    fun sanitizeIdentityCandidateNormalizesSpaces() {
        assertEquals(
            "A B",
            "A    B".sanitizeIdentityCandidate()
        )
    }

    @Test
    fun isIdentityLabelAnyMatch() {
        assertTrue("Seri".isIdentityLabel(listOf("isim", "Seri")))
    }

    @Test
    fun isIdentityLabelNone() {
        assertFalse("xyz".isIdentityLabel(listOf("isim", "soyisim")))
    }
}
