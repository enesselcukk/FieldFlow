package com.example.presentation.eventlog.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.example.domain.model.EventRecord
import com.example.utils.extensions.toFormattedDate

internal enum class EventSummaryStyle {
    Card,
    Dialog,
}

@Composable
internal fun EventSummaryContent(
    event: EventRecord,
    style: EventSummaryStyle,
) {
    val mutedColor = MaterialTheme.colorScheme.onSurfaceVariant

    Text(
        text = event.type.toLabel(),
        style = when (style) {
            EventSummaryStyle.Card -> MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.SemiBold,
            )
            EventSummaryStyle.Dialog -> MaterialTheme.typography.bodySmall
        },
        color = when (style) {
            EventSummaryStyle.Card -> MaterialTheme.colorScheme.onSurface
            EventSummaryStyle.Dialog -> mutedColor
        },
    )
    if (event.detail.isNotBlank()) {
        Text(
            text = event.detail,
            style = MaterialTheme.typography.bodySmall,
            color = mutedColor,
            maxLines = if (style == EventSummaryStyle.Card) 1 else Int.MAX_VALUE,
            overflow = TextOverflow.Ellipsis,
        )
    }
    Text(
        text = event.timestamp.toFormattedDate(),
        style = MaterialTheme.typography.labelSmall,
        color = mutedColor,
    )
}
