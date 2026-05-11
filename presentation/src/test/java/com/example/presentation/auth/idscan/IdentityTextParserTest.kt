package com.example.presentation.auth.idscan

import com.example.domain.model.IdentityInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IdentityTextParserTest {

    private val parser = IdentityTextParser()

    @Test
    fun parsesNameAndSurnameFromLatinLabels() {
        val text =
            """
            NAME ali
            SURNAME veli
            """.trimIndent()
        val parsed = parser.parse(text)
        assertEquals("ali", parsed.name.trim())
        assertEquals("veli", parsed.surname.trim())
    }

    @Test
    fun parsesInlineColons() {
        val text =
            """
            SURNAME: Selçuk
            ADI: Enes
            """.trimIndent()
        val parsed = parser.parse(text)
        assertTrue(parsed.name.isNotBlank())
        assertTrue(parsed.surname.isNotBlank())
    }

    @Test
    fun returnsEmptyWhenNoIdentityLines() {
        val parsed = parser.parse("hello world")
        assertEquals(IdentityInfo(name = "", surname = ""), parsed)
    }
}
