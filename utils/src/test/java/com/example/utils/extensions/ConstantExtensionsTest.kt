package com.example.utils.extensions

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.Locale
import java.util.TimeZone

class ConstantExtensionsTest {

    private lateinit var savedLocale: Locale
    private lateinit var savedTimeZone: TimeZone

    @Before
    fun pinLocaleAndTimeZone() {
        savedLocale = Locale.getDefault()
        savedTimeZone = TimeZone.getDefault()
        Locale.setDefault(Locale.US)
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    }

    @After
    fun restoreLocaleAndTimeZone() {
        Locale.setDefault(savedLocale)
        TimeZone.setDefault(savedTimeZone)
    }

    @Test
    fun epochToFormattedDate() {
        assertEquals("01 Jan 1970, 00:00:00", 0L.toFormattedDate())
    }

    @Test
    fun epochToDateTimeShort() {
        assertEquals("01 Jan, 00:00", 0L.toDateTimeShort())
    }

    @Test
    fun nonZeroInstantToFormattedDate() {
        assertEquals(
            "15 Jun 2024, 14:30:45",
            1718461845000L.toFormattedDate()
        )
    }
}
