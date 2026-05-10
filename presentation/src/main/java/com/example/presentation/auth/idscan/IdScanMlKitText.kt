package com.example.presentation.auth.idscan

import com.google.mlkit.vision.text.Text

internal fun Text.extractTextForIdentityParsing(): String {
    data class ScrapLine(val top: Int, val left: Int, val text: String)

    val pieces = mutableListOf<ScrapLine>()
    for (block in textBlocks) {
        for (mlLine in block.lines) {
            val box = mlLine.boundingBox ?: continue
            val t = mlLine.text.trim()
            if (t.isBlank()) continue
            pieces += ScrapLine(box.top, box.left, t)
        }
    }
    return if (pieces.isEmpty()) {
        text.trim()
    } else {
        pieces.sortedWith(compareBy({ it.top }, { it.left })).joinToString("\n") { it.text }
    }
}
