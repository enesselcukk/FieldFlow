package com.example.utils.extensions

private val TurkishNormalizationMap = mapOf(
    'İ' to 'I',
    'I' to 'I',
    'Ş' to 'S',
    'Ç' to 'C',
    'Ğ' to 'G',
    'Ö' to 'O',
    'Ü' to 'U'
)

private fun String.normalizeForLabelMatching(): String {
    val upper = uppercase()
    val normalized = buildString(upper.length) {
        upper.forEach { ch ->
            append(TurkishNormalizationMap[ch] ?: ch)
        }
    }
    return normalized
        .replace(Regex("\\s+"), " ")
        .trim()
}

fun String.containsLabelKey(key: String): Boolean {
    val line = normalizeForLabelMatching()
    val normalizedKey = key.normalizeForLabelMatching()
    return line == normalizedKey ||
        line.startsWith("$normalizedKey ") ||
        line.startsWith("$normalizedKey:") ||
        line.startsWith("$normalizedKey(") ||
        line.startsWith("$normalizedKey/") ||
        line.contains(" $normalizedKey ") ||
        line.contains(" $normalizedKey:")
}

fun String.sanitizeIdentityCandidate(): String {
    return this
        .replace("(s)", "", ignoreCase = true)
        .replace(Regex("^[^\\p{L}]+"), "")
        .replace(Regex("\\s+"), " ")
        .trim()
}

fun String.isIdentityLabel(allLabelKeys: List<String>): Boolean {
    return allLabelKeys.any { key -> containsLabelKey(key) }
}
