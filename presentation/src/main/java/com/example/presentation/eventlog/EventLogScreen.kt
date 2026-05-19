package com.example.presentation.eventlog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.EventRecord
import com.example.presentation.R
import com.example.presentation.eventlog.components.EventNoteDialog
import com.example.presentation.eventlog.components.EventRecordCard
import com.example.presentation.eventlog.model.EventLogUiState

@Composable
fun EventLogScreen(
    viewModel: EventLogViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    EventLogContent(
        uiState = uiState,
        onEventClick = viewModel::onEventSelected,
        onNoteChanged = viewModel::onNoteChanged,
        onNoteSave = viewModel::onNoteSave,
        onNoteDismiss = viewModel::onNoteDismiss,
    )
}

@Composable
internal fun EventLogContent(
    uiState: EventLogUiState,
    onEventClick: (EventRecord) -> Unit,
    onNoteChanged: (String) -> Unit,
    onNoteSave: () -> Unit,
    onNoteDismiss: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (uiState.events.isEmpty()) {
            EventLogEmptyState()
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }
                items(uiState.events, key = { it.id }) { event ->
                    EventRecordCard(
                        event = event,
                        onClick = { onEventClick(event) },
                    )
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }

        uiState.selectedEvent?.let { event ->
            EventNoteDialog(
                event = event,
                noteInput = uiState.noteInput,
                onNoteChanged = onNoteChanged,
                onSave = onNoteSave,
                onDismiss = onNoteDismiss,
            )
        }
    }
}

@Composable
private fun EventLogEmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.event_log_empty),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
