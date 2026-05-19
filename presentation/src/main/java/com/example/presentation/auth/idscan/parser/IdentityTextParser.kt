package com.example.presentation.auth.idscan.parser

import com.example.domain.model.IdentityInfo
import com.example.utils.extensions.containsLabelKey
import com.example.utils.extensions.isIdentityLabel
import com.example.utils.extensions.sanitizeIdentityCandidate
import kotlin.text.RegexOption
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IdentityTextParser @Inject constructor() {

    private val allLabelKeys = buildList {
        addAll(NAME_KEYS)
        addAll(SURNAME_KEYS)
        add("FIRST NAME")
        add("FIRSTNAME")
        add("MIDDLE NAME")
    }

    private val nameKeysDesc = NAME_KEYS.distinct().sortedByDescending { it.length }

    private val surnameKeysDesc = SURNAME_KEYS.distinct().sortedByDescending { it.length }

    fun parse(rawText: String): IdentityInfo {
        val lines = rawText
            .lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }

        val name = findValue(lines, nameKeysDesc, isPrimaryNameSlot = true)
        val surname = findValue(lines, surnameKeysDesc, isPrimaryNameSlot = false)

        return IdentityInfo(
            name = name.orEmpty(),
            surname = surname.orEmpty(),
        )
    }

    private fun findValue(
        lines: List<String>,
        keysDescending: List<String>,
        isPrimaryNameSlot: Boolean,
    ): String? {
        for (index in lines.indices) {
            val line = lines[index]
            if (!keysDescending.any { line.containsLabelKey(it) }) continue

            val inline = extractInlineAcrossKeys(line, keysDescending, isPrimaryNameSlot)
            if (!inline.isNullOrBlank()) return inline

            for (lookahead in 1..4) {
                val candidateRaw = lines.getOrNull(index + lookahead)?.trim() ?: continue
                val candidate = candidateRaw.sanitizeIdentityCandidate()
                if (candidate.isBlank()) continue
                if (looksLikeLabel(candidate)) continue
                if (looksLikeMrzOrBarcodeNoise(candidate)) continue
                if (looksLikeAuxiliaryCertificateLine(candidate)) continue
                if (looksLikeStandaloneDate(candidate)) continue
                return candidate
            }
        }
        return null
    }

    private fun extractInlineAcrossKeys(
        line: String,
        keysDescending: List<String>,
        isPrimaryNameSlot: Boolean,
    ): String? {
        for (key in keysDescending) {
            if (!line.containsLabelKey(key)) continue
            val extracted =
                extractSameLineAfterKeyword(line.trim(), keyword = key)?.sanitizeIdentityCandidate()
                    ?.trim()
                    ?: continue
            if (extracted.isBlank()) continue
            if (looksLikeLabel(extracted)) continue
            if (looksLikeMrzOrBarcodeNoise(extracted)) continue
            if (!isPrimaryNameSlot && surnameKeysDesc.any { extracted.containsLabelKey(it) }) continue
            return extracted
        }
        return null
    }

    private fun looksLikeLabel(value: String): Boolean {
        return value.isIdentityLabel(allLabelKeys)
    }

    private fun looksLikeMrzOrBarcodeNoise(s: String): Boolean {
        val compact = s.replace(" ", "").replace("\n", "")
        if (compact.length < 20) return false
        val digitRatio = compact.count { it.isDigit() }.toFloat() / compact.length
        if (compact.any { it == '<' }) return true
        if (compact.length >= 38 && digitRatio > 0.35f && s.all { it.isLetterOrDigit() || it == '<' || it.isWhitespace() }) {
            return true
        }
        return false
    }

    private fun looksLikeAuxiliaryCertificateLine(s: String): Boolean {
        val u = s.uppercase()
        return u.contains("SERİ NO") ||
            Regex("SER[Iİ]\\s*N[OoUÜ]").containsMatchIn(u) ||
            u.contains("SERIES NO") ||
            u.contains("DOCUMENT NO") ||
            u.contains("SIRA NO")
    }

    private fun looksLikeStandaloneDate(s: String): Boolean {
        val t = s.replace(" ", "")
        return Regex("^\\d{2}[.]\\d{2}[.]\\d{4}$").matches(t) ||
            Regex("^\\d{2}/\\d{2}/\\d{4}$").matches(t)
    }

    private companion object {
        private val NAME_KEYS =
            listOf(
                "GIVEN NAME(S)",
                "GIVEN NAMES",
                "GIVEN NAME",
                "FIRST NAME",
                "ADI SOYADI",
                "AD SOYAD",
                "NAME",
                "ADI",
                "PRÉNOMS",
                "PRENOMS",
                "VORNAMEN",
            )

        private val SURNAME_KEYS =
            listOf(
                "LAST NAME",
                "SURNAME",
                "SOYADI",
                "SOYAD",
                "FAMILY NAME",
                "NOM",
                "NACHNAME",
            )

        private fun extractSameLineAfterKeyword(trimmedLine: String, keyword: String): String? {
            val esc = Regex.escape(keyword.trim())
            val separators = "[\\s:：|/.\\-\\u2013\\u2212]*"
            return Regex(
                pattern = "$esc(?:\\([^)]{0,32}\\))?$separators(.*)",
                options = setOf(RegexOption.IGNORE_CASE),
            )
                .find(trimmedLine.trim())
                ?.groupValues
                ?.getOrNull(1)
                ?.trim()
                ?.takeUnless { it.isEmpty() }
        }
    }
}
