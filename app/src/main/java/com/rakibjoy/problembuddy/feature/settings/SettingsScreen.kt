package com.rakibjoy.problembuddy.feature.settings

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rakibjoy.problembuddy.BuildConfig
import com.rakibjoy.problembuddy.core.ui.components.AppTopBar
import com.rakibjoy.problembuddy.core.ui.components.GradientSurface
import com.rakibjoy.problembuddy.core.ui.theme.AppShapes
import com.rakibjoy.problembuddy.core.ui.theme.ProblemBuddyTheme
import com.rakibjoy.problembuddy.core.ui.theme.Spacing
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
    GradientSurface {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = { AppTopBar(title = "Settings") },
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.xl),
            ) {
                AppearanceGroup(theme = state.theme, onIntent = onIntent)
                RecommendationsGroup(
                    recsPerLoad = state.recsPerLoad,
                    difficultyOffset = state.difficultyOffset,
                    onIntent = onIntent,
                )
                DataGroup(
                    resetCorpusBusy = state.resetCorpusBusy,
                    onIntent = onIntent,
                )
                AboutGroup()
                Spacer(Modifier.height(Spacing.lg))
            }
        }
    }

    if (state.showResetCorpusConfirm) {
        AlertDialog(
            onDismissRequest = { onIntent(SettingsIntent.DismissResetCorpusConfirm) },
            shape = AppShapes.large,
            icon = {
                Icon(Icons.Default.RestartAlt, contentDescription = null)
            },
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
            shape = AppShapes.large,
            icon = {
                Icon(
                    Icons.Default.DeleteForever,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                )
            },
            title = { Text("Delete everything?") },
            text = { Text("This wipes your handle, corpus, and interactions. You'll go back to onboarding.") },
            confirmButton = {
                TextButton(onClick = { onIntent(SettingsIntent.ConfirmDeleteAll) }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
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
private fun SettingsGroup(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = Spacing.md, bottom = Spacing.sm),
        )
        Card(
            shape = AppShapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Column(
                modifier = Modifier.padding(Spacing.md),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                content = content,
            )
        }
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    iconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    trailing: @Composable (() -> Unit)? = null,
    belowContent: @Composable (() -> Unit)? = null,
) {
    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp),
            )
            Spacer(Modifier.size(Spacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (trailing != null) {
                Spacer(Modifier.size(Spacing.sm))
                trailing()
            }
        }
        if (belowContent != null) {
            Spacer(Modifier.height(Spacing.sm))
            belowContent()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppearanceGroup(
    theme: ThemeMode,
    onIntent: (SettingsIntent) -> Unit,
) {
    SettingsGroup(title = "APPEARANCE") {
        SettingsRow(
            icon = Icons.Outlined.Palette,
            title = "Theme",
            subtitle = "Match your system or pick a side",
            belowContent = {
                val modes = listOf(ThemeMode.SYSTEM, ThemeMode.LIGHT, ThemeMode.DARK)
                val labels = listOf("System", "Light", "Dark")
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    modes.forEachIndexed { index, mode ->
                        SegmentedButton(
                            selected = theme == mode,
                            onClick = { onIntent(SettingsIntent.SetTheme(mode)) },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = modes.size,
                            ),
                        ) {
                            Text(labels[index])
                        }
                    }
                }
            },
        )
    }
}

@Composable
private fun RecommendationsGroup(
    recsPerLoad: Int,
    difficultyOffset: Int,
    onIntent: (SettingsIntent) -> Unit,
) {
    SettingsGroup(title = "RECOMMENDATIONS") {
        SettingsRow(
            icon = Icons.Outlined.Tune,
            title = "Problems per load",
            trailing = {
                Text(
                    text = "$recsPerLoad",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                )
            },
            belowContent = {
                Slider(
                    value = recsPerLoad.toFloat(),
                    onValueChange = { onIntent(SettingsIntent.SetRecsPerLoad(it.toInt())) },
                    valueRange = 1f..30f,
                    steps = 28,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                    ),
                )
            },
        )
        SettingsRow(
            icon = Icons.AutoMirrored.Outlined.TrendingUp,
            title = "Difficulty offset",
            trailing = {
                val signed = when {
                    difficultyOffset > 0 -> "+$difficultyOffset"
                    difficultyOffset < 0 -> "$difficultyOffset"
                    else -> "±0"
                }
                Text(
                    text = signed,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                )
            },
            belowContent = {
                Slider(
                    value = difficultyOffset.toFloat(),
                    onValueChange = { onIntent(SettingsIntent.SetDifficultyOffset(it.toInt())) },
                    valueRange = -400f..400f,
                    steps = 7,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                    ),
                )
            },
        )
    }
}

@Composable
private fun DataGroup(
    resetCorpusBusy: Boolean,
    onIntent: (SettingsIntent) -> Unit,
) {
    SettingsGroup(title = "DATA") {
        SettingsRow(
            icon = Icons.Default.RestartAlt,
            title = "Reset corpus",
            subtitle = "Keep your handle, clear trained problems.",
            trailing = {
                TextButton(
                    onClick = { onIntent(SettingsIntent.RequestResetCorpus) },
                    enabled = !resetCorpusBusy,
                ) {
                    if (resetCorpusBusy) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text("Reset")
                    }
                }
            },
        )
        SettingsRow(
            icon = Icons.Default.DeleteForever,
            iconTint = MaterialTheme.colorScheme.error,
            title = "Delete all data",
            subtitle = "Wipes everything and returns to onboarding.",
            trailing = {
                TextButton(onClick = { onIntent(SettingsIntent.RequestDeleteAll) }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
        )
    }
}

@Composable
private fun AboutGroup() {
    val context = LocalContext.current
    SettingsGroup(title = "ABOUT") {
        SettingsRow(
            icon = Icons.Outlined.Info,
            title = "Version",
            trailing = {
                Text(
                    text = runCatching { BuildConfig.VERSION_NAME }.getOrDefault("1.0"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
        )
        Box(
            modifier = Modifier.fillMaxWidth(),
        ) {
            SettingsRow(
                icon = Icons.Outlined.Code,
                title = "View source on GitHub",
                subtitle = "TheRakibJoy/ProblemBuddy-Android",
                trailing = {
                    TextButton(onClick = {
                        Toast.makeText(
                            context,
                            "Opening soon",
                            Toast.LENGTH_SHORT,
                        ).show()
                    }) {
                        Text("Open")
                    }
                },
            )
        }
    }
}

@Preview(name = "Settings - Default")
@Composable
private fun SettingsScreenDefaultPreview() {
    ProblemBuddyTheme {
        SettingsScreen(
            state = SettingsState(),
            onIntent = {},
        )
    }
}

@Preview(name = "Settings - Reset dialog")
@Composable
private fun SettingsScreenResetConfirmPreview() {
    ProblemBuddyTheme {
        SettingsScreen(
            state = SettingsState(showResetCorpusConfirm = true),
            onIntent = {},
        )
    }
}
