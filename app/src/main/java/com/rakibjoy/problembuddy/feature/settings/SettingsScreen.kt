package com.rakibjoy.problembuddy.feature.settings

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rakibjoy.problembuddy.domain.model.ThemeMode

@Composable
fun SettingsScreen(
    onNavigateToOnboarding: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                SettingsEffect.NavigateToOnboarding -> onNavigateToOnboarding()
                is SettingsEffect.ShowToast -> Toast.makeText(
                    context,
                    effect.message,
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }
    }
    SettingsScreen(state = state, onIntent = viewModel::onIntent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: SettingsState,
    onIntent: (SettingsIntent) -> Unit,
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Settings") }) },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            AppearanceSection(theme = state.theme, onIntent = onIntent)
            RecommendationsSection(
                recsPerLoad = state.recsPerLoad,
                difficultyOffset = state.difficultyOffset,
                onIntent = onIntent,
            )
            DataSection(
                resetCorpusBusy = state.resetCorpusBusy,
                onIntent = onIntent,
            )
        }
    }

    if (state.showResetCorpusConfirm) {
        AlertDialog(
            onDismissRequest = { onIntent(SettingsIntent.DismissResetCorpusConfirm) },
            title = { Text("Reset corpus?") },
            text = { Text("This removes all trained problems but keeps your handle.") },
            confirmButton = {
                TextButton(onClick = { onIntent(SettingsIntent.ConfirmResetCorpus) }) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { onIntent(SettingsIntent.DismissResetCorpusConfirm) }) {
                    Text("Cancel")
                }
            },
        )
    }

    if (state.showDeleteAllConfirm) {
        AlertDialog(
            onDismissRequest = { onIntent(SettingsIntent.DismissDeleteAllConfirm) },
            title = { Text("Delete everything?") },
            text = { Text("This wipes your handle, corpus, and interactions. You'll go back to onboarding.") },
            confirmButton = {
                TextButton(onClick = { onIntent(SettingsIntent.ConfirmDeleteAll) }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { onIntent(SettingsIntent.DismissDeleteAllConfirm) }) {
                    Text("Cancel")
                }
            },
        )
    }
}

@Composable
private fun AppearanceSection(
    theme: ThemeMode,
    onIntent: (SettingsIntent) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = "Appearance", style = MaterialTheme.typography.titleMedium)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ThemeOption(
                label = "System",
                selected = theme == ThemeMode.SYSTEM,
                onClick = { onIntent(SettingsIntent.SetTheme(ThemeMode.SYSTEM)) },
            )
            ThemeOption(
                label = "Light",
                selected = theme == ThemeMode.LIGHT,
                onClick = { onIntent(SettingsIntent.SetTheme(ThemeMode.LIGHT)) },
            )
            ThemeOption(
                label = "Dark",
                selected = theme == ThemeMode.DARK,
                onClick = { onIntent(SettingsIntent.SetTheme(ThemeMode.DARK)) },
            )
        }
    }
}

@Composable
private fun ThemeOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        RadioButton(selected = selected, onClick = onClick)
        Text(text = label)
    }
}

@Composable
private fun RecommendationsSection(
    recsPerLoad: Int,
    difficultyOffset: Int,
    onIntent: (SettingsIntent) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = "Recommendations", style = MaterialTheme.typography.titleMedium)
        Text(text = "Problems per load: $recsPerLoad")
        Slider(
            value = recsPerLoad.toFloat(),
            onValueChange = { onIntent(SettingsIntent.SetRecsPerLoad(it.toInt())) },
            valueRange = 1f..30f,
            steps = 28,
        )
        Text(text = "Difficulty offset: $difficultyOffset")
        Slider(
            value = difficultyOffset.toFloat(),
            onValueChange = { onIntent(SettingsIntent.SetDifficultyOffset(it.toInt())) },
            valueRange = -400f..400f,
            steps = 7,
        )
    }
}

@Composable
private fun DataSection(
    resetCorpusBusy: Boolean,
    onIntent: (SettingsIntent) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = "Data", style = MaterialTheme.typography.titleMedium)
        OutlinedButton(
            onClick = { onIntent(SettingsIntent.RequestResetCorpus) },
            enabled = !resetCorpusBusy,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (resetCorpusBusy) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                )
            } else {
                Text("Reset corpus")
            }
        }
        Button(
            onClick = { onIntent(SettingsIntent.RequestDeleteAll) },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
            ),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Delete all data")
        }
    }
}

@Preview
@Composable
private fun SettingsScreenDefaultPreview() {
    SettingsScreen(
        state = SettingsState(),
        onIntent = {},
    )
}

@Preview
@Composable
private fun SettingsScreenResetConfirmPreview() {
    SettingsScreen(
        state = SettingsState(showResetCorpusConfirm = true),
        onIntent = {},
    )
}
