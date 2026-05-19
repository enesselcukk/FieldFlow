package com.example.presentation.auth.idscan

import com.example.domain.model.IdentityInfo
import com.example.presentation.auth.idscan.fixtures.IdScanOcrFixtures
import com.example.presentation.auth.idscan.parser.IdentityTextParser
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class IdentityTextParserTest {

    private lateinit var parser: IdentityTextParser

    @Before
    fun setUp() {
        parser = IdentityTextParser()
    }

    @Test
    fun parse_extractsLatinNameAndSurname() {
        val parsed = parser.parse(IdScanOcrFixtures.latinNameSurname)

        assertEquals(
            IdentityInfo(name = "Enes", surname = "Selçuk"),
            parsed,
        )
    }

    @Test
    fun parse_extractsInlineColonLabels() {
        val parsed = parser.parse(IdScanOcrFixtures.latinInlineColons)

        assertEquals("Enes", parsed.name.trim())
        assertEquals("Selçuk", parsed.surname.trim())
    }

    @Test
    fun parse_extractsTurkishLabelsFromFollowingLines() {
        val parsed = parser.parse(IdScanOcrFixtures.turkishMultiline)

        assertEquals("Ahmet", parsed.name.trim())
        assertEquals("Yılmaz", parsed.surname.trim())
    }

    @Test
    fun parse_ignoresMrzNoiseAfterIdentityFields() {
        val parsed = parser.parse(IdScanOcrFixtures.nameWithMrzNoise)

        assertEquals("Ali", parsed.name.trim())
        assertEquals("Veli", parsed.surname.trim())
    }

    @Test
    fun parse_ignoresStandaloneDateLines() {
        val parsed = parser.parse(IdScanOcrFixtures.nameWithDateNoise)

        assertEquals("Ayşe", parsed.name.trim())
        assertEquals("Demir", parsed.surname.trim())
    }

    @Test
    fun parse_ignoresSerialNumberLines() {
        val parsed = parser.parse(IdScanOcrFixtures.nameWithSerialLine)

        assertEquals("Can", parsed.name.trim())
        assertEquals("Öz", parsed.surname.trim())
    }

    @Test
    fun parse_returnsEmptyIdentityWhenNoLabelsMatch() {
        assertEquals(
            IdentityInfo(name = "", surname = ""),
            parser.parse(IdScanOcrFixtures.UNRELATED_TEXT),
        )
    }
}
