package com.example.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.AppLanguage
import com.example.domain.model.AppTheme
import com.example.presentation.R

val LOCATION_INTERVALS = listOf(30, 60, 120, 300)

@Composable
fun SettingsScreen(
    isActivated: Boolean,
    modifier: Modifier = Modifier
) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val prefs by viewModel.preferences.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SettingsSectionCard(title = stringResource(R.string.settings_language_title)) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ElevatedFilterChip(
                    selected = prefs.language == AppLanguage.TURKISH,
                    onClick = { viewModel.setLanguage(AppLanguage.TURKISH) },
                    label = { Text(stringResource(R.string.settings_language_turkish)) },
                    colors = selectedChipColors()
                )
                ElevatedFilterChip(
                    selected = prefs.language == AppLanguage.ENGLISH,
                    onClick = { viewModel.setLanguage(AppLanguage.ENGLISH) },
                    label = { Text(stringResource(R.string.settings_language_english)) },
                    colors = selectedChipColors()
                )
            }
        }

        SettingsSectionCard(
            title = stringResource(R.string.settings_location_interval_title),
            locked = !isActivated,
            lockedHint = stringResource(R.string.settings_location_interval_locked_hint)
        ) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LOCATION_INTERVALS.forEach { seconds ->
                    ElevatedFilterChip(
                        selected = prefs.locationIntervalSeconds == seconds,
                        onClick = { if (isActivated) viewModel.setLocationInterval(seconds) },
                        enabled = isActivated,
                        label = { Text(formatInterval(seconds)) },
                        colors = selectedChipColors()
                    )
                }
            }
        }

        SettingsSectionCard(title = stringResource(R.string.settings_theme_title)) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ElevatedFilterChip(
                    selected = prefs.theme == AppTheme.LIGHT,
                    onClick = { viewModel.setTheme(AppTheme.LIGHT) },
                    label = { Text(stringResource(R.string.settings_theme_light)) },
                    colors = selectedChipColors()
                )
                ElevatedFilterChip(
                    selected = prefs.theme == AppTheme.DARK,
                    onClick = { viewModel.setTheme(AppTheme.DARK) },
                    label = { Text(stringResource(R.string.settings_theme_dark)) },
                    colors = selectedChipColors()
                )
                ElevatedFilterChip(
                    selected = prefs.theme == AppTheme.SYSTEM,
                    onClick = { viewModel.setTheme(AppTheme.SYSTEM) },
                    label = { Text(stringResource(R.string.settings_theme_system)) },
                    colors = selectedChipColors()
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    locked: Boolean = false,
    lockedHint: String? = null,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (locked) 0.5f else 1f),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                if (locked) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            if (locked && lockedHint != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = lockedHint,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun selectedChipColors() = FilterChipDefaults.elevatedFilterChipColors(
    selectedContainerColor = MaterialTheme.colorScheme.primary,
    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
)

private fun formatInterval(seconds: Int): String = when {
    seconds < 60 -> "${seconds}s"
    else -> "${seconds / 60}dk"
}
