package com.example.presentation.eventlog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.EventRecord
import com.example.presentation.R
import com.example.utils.extensions.toFormattedDate

@Composable
fun EventLogScreen(
    viewModel: EventLogViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        if (uiState.events.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.event_log_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }
                items(uiState.events, key = { it.id }) { event ->
                    EventRecordCard(
                        event = event,
                        onClick = { viewModel.onEventSelected(event) }
                    )
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }

        uiState.selectedEvent?.let {
            NoteDialog(
                event = it,
                noteInput = uiState.noteInput,
                onNoteChanged = viewModel::onNoteChanged,
                onSave = viewModel::onNoteSave,
                onDismiss = viewModel::onNoteDismiss
            )
        }
    }
}

@Composable
private fun EventRecordCard(
    event: EventRecord,
    onClick: () -> Unit
) {
    val (icon, iconTint, containerColor) = eventVisuals(event.type)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = iconTint.copy(alpha = 0.15f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.type.toLabel(),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (event.detail.isNotBlank()) {
                    Text(
                        text = event.detail,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = event.timestamp.toFormattedDate(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (event.note.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = event.note,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = if (event.note.isBlank())
                    stringResource(R.string.event_log_add_note)
                else
                    stringResource(R.string.event_log_edit_note),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun NoteDialog(
    event: EventRecord,
    noteInput: String,
    onNoteChanged: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.event_log_note_dialog_title),
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = event.type.toLabel(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (event.detail.isNotBlank()) {
                    Text(
                        text = event.detail,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = event.timestamp.toFormattedDate(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = noteInput,
                    onValueChange = onNoteChanged,
                    label = { Text(stringResource(R.string.event_log_note_label)) },
                    placeholder = { Text(stringResource(R.string.event_log_note_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 6,
                    shape = RoundedCornerShape(8.dp)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onSave) {
                Text(
                    text = stringResource(R.string.event_log_note_save),
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.event_log_note_cancel))
            }
        }
    )
}

private data class EventVisuals(
    val icon: ImageVector,
    val tint: Color,
    val containerColor: Color
)

@Composable
private fun eventVisuals(type: EventRecord.EventType): EventVisuals {
    val errorColor = MaterialTheme.colorScheme.error
    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val errorContainer = MaterialTheme.colorScheme.errorContainer
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val surface = MaterialTheme.colorScheme.surfaceVariant

    return when (type) {
        EventRecord.EventType.GEOFENCE_EXIT -> EventVisuals(
            icon = Icons.Default.Warning,
            tint = errorColor,
            containerColor = errorContainer
        )
        EventRecord.EventType.GEOFENCE_ENTER -> EventVisuals(
            icon = Icons.Default.LocationOn,
            tint = primaryColor,
            containerColor = primaryContainer
        )
        EventRecord.EventType.INTERNET_LOST -> EventVisuals(
            icon = Icons.Default.Warning,
            tint = errorColor,
            containerColor = errorContainer
        )
        EventRecord.EventType.INTERNET_RESTORED -> EventVisuals(
            icon = Icons.Default.LocationOn,
            tint = tertiaryColor,
            containerColor = surface
        )
        EventRecord.EventType.LOCATION_SERVICE_DISABLED -> EventVisuals(
            icon = Icons.Default.Warning,
            tint = errorColor,
            containerColor = errorContainer
        )
        EventRecord.EventType.LOCATION_SERVICE_ENABLED -> EventVisuals(
            icon = Icons.Default.LocationOn,
            tint = primaryColor,
            containerColor = primaryContainer
        )
    }
}

@Composable
private fun EventRecord.EventType.toLabel(): String = when (this) {
    EventRecord.EventType.GEOFENCE_EXIT -> stringResource(R.string.event_type_geofence_exit)
    EventRecord.EventType.GEOFENCE_ENTER -> stringResource(R.string.event_type_geofence_enter)
    EventRecord.EventType.INTERNET_LOST -> stringResource(R.string.event_type_internet_lost)
    EventRecord.EventType.INTERNET_RESTORED -> stringResource(R.string.event_type_internet_restored)
    EventRecord.EventType.LOCATION_SERVICE_DISABLED -> stringResource(R.string.event_type_location_disabled)
    EventRecord.EventType.LOCATION_SERVICE_ENABLED -> stringResource(R.string.event_type_location_enabled)
}
