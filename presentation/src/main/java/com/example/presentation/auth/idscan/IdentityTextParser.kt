package com.example.presentation.auth.idscan

import com.example.domain.model.IdentityInfo
import com.example.utils.extensions.containsLabelKey
import com.example.utils.extensions.isIdentityLabel
import com.example.utils.extensions.sanitizeIdentityCandidate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IdentityTextParser @Inject constructor() {

    private val allLabelKeys = listOf(
        "ADI", "ADI SOYADI", "GIVEN NAME", "GIVEN NAMES", "GIVEN NAME(S)", "NAME",
        "SOYADI", "SURNAME", "LAST NAME"
    )

    fun parse(rawText: String): IdentityInfo {
        val lines = rawText
            .lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }

        val name = findValue(lines, listOf("ADI", "GIVEN NAME", "GIVEN NAMES", "GIVEN NAME(S)"))
        val surname = findValue(lines, listOf("SOYADI", "SURNAME", "LAST NAME"))

        return IdentityInfo(
            name = name.orEmpty(),
            surname = surname.orEmpty()
        )
    }

    private fun findValue(lines: List<String>, keys: List<String>): String? {
        val index = lines.indexOfFirst { line ->
            keys.any { key -> line.containsLabelKey(key) }
        }
        if (index == -1) return null

        val nextLine = lines.getOrNull(index + 1)?.trim().orEmpty().sanitizeIdentityCandidate()
        if (nextLine.isNotBlank() && !looksLikeLabel(nextLine)) {
            return nextLine
        }
        return null
    }

    private fun looksLikeLabel(value: String): Boolean {
        return value.isIdentityLabel(allLabelKeys)
    }
}
