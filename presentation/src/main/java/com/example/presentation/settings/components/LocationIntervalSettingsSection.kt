package com.example.presentation.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.presentation.R
import com.example.presentation.settings.model.LOCATION_INTERVALS

@Composable
private fun locationIntervalLabel(seconds: Int): String =
    if (seconds < 60) {
        stringResource(R.string.settings_interval_seconds, seconds)
    } else {
        stringResource(R.string.settings_interval_minutes, seconds / 60)
    }

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun LocationIntervalSettingsSection(
    selectedIntervalSeconds: Int,
    isActivated: Boolean,
    onIntervalSelected: (Int) -> Unit,
) {
    SettingsSectionCard(
        title = stringResource(R.string.settings_location_interval_title),
        locked = !isActivated,
        lockedHint = stringResource(R.string.settings_location_interval_locked_hint),
    ) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LOCATION_INTERVALS.forEach { seconds ->
                ElevatedFilterChip(
                    selected = selectedIntervalSeconds == seconds,
                    onClick = { if (isActivated) onIntervalSelected(seconds) },
                    enabled = isActivated,
                    label = { Text(locationIntervalLabel(seconds)) },
                    colors = settingsSelectedChipColors(),
                )
            }
        }
    }
}
