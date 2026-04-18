package com.rakibjoy.problembuddy.feature.settings

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rakibjoy.problembuddy.BuildConfig
import com.rakibjoy.problembuddy.core.ui.components.AppTopBar
import com.rakibjoy.problembuddy.core.ui.components.GradientSurface
import com.rakibjoy.problembuddy.core.ui.theme.AppShapes
import com.rakibjoy.problembuddy.core.ui.theme.ProblemBuddyTheme
import com.rakibjoy.problembuddy.core.ui.theme.Spacing
import com.rakibjoy.problembuddy.core.ui.theme.appExtras
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
            topBar = { AppTopBar() },
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
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
                // decorative
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
                // decorative
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
    val extras = MaterialTheme.appExtras
    Column(Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            letterSpacing = 1.0.sp,
            color = extras.textTertiary,
            modifier = Modifier.padding(bottom = 6.dp),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(extras.surfaceElevated)
                .border(0.5.dp, extras.borderSubtle, RoundedCornerShape(10.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = content,
        )
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    iconTint: Color = MaterialTheme.appExtras.textSecondary,
    trailing: @Composable (() -> Unit)? = null,
    belowContent: @Composable (() -> Unit)? = null,
) {
    val extras = MaterialTheme.appExtras
    Column(
        Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {},
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 40.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // decorative
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.size(Spacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = extras.textTertiary,
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
            title = "theme",
            subtitle = "match your system or pick a side",
            belowContent = {
                val modes = listOf(ThemeMode.SYSTEM, ThemeMode.LIGHT, ThemeMode.DARK)
                val labels = listOf("system", "light", "dark")
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
            title = "problems per load",
            trailing = {
                Text(
                    text = "$recsPerLoad",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.appExtras.accentVioletSoft,
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
            title = "difficulty offset",
            trailing = {
                val signed = when {
                    difficultyOffset > 0 -> "+$difficultyOffset"
                    difficultyOffset < 0 -> "$difficultyOffset"
                    else -> "\u00B10"
                }
                Text(
                    text = signed,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.appExtras.accentVioletSoft,
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
            title = "reset corpus",
            subtitle = "keep your handle, clear trained problems.",
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
                        Text("reset")
                    }
                }
            },
        )
        SettingsRow(
            icon = Icons.Default.DeleteForever,
            iconTint = MaterialTheme.colorScheme.error,
            title = "delete all data",
            subtitle = "wipes everything and returns to onboarding.",
            trailing = {
                Button(
                    onClick = { onIntent(SettingsIntent.RequestDeleteAll) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    ),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text("delete")
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
            icon = Icons.Outlined.Favorite,
            title = "made by RakibJoy",
            subtitle = "idea, algorithms, and original web app",
            trailing = {
                TextButton(onClick = {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://codeforces.com/profile/RakibJoy"),
                    )
                    runCatching { context.startActivity(intent) }
                }) {
                    Text("CF")
                }
            },
        )
        SettingsRow(
            icon = Icons.Outlined.Info,
            title = "version",
            trailing = {
                Text(
                    text = runCatching { BuildConfig.VERSION_NAME }.getOrDefault("1.0"),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.appExtras.textTertiary,
                )
            },
        )
        SettingsRow(
            icon = Icons.Outlined.Code,
            title = "view source on GitHub",
            subtitle = "TheRakibJoy/ProblemBuddy-Android",
            trailing = {
                TextButton(onClick = {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/TheRakibJoy/ProblemBuddy-Android"),
                    )
                    runCatching { context.startActivity(intent) }
                }) {
                    Text("open")
                }
            },
        )
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
