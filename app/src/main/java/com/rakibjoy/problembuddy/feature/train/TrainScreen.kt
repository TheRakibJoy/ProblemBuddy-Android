package com.rakibjoy.problembuddy.feature.train

import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rakibjoy.problembuddy.core.ui.components.AnimatedProgressBar
import com.rakibjoy.problembuddy.core.ui.components.AppTopBar
import com.rakibjoy.problembuddy.core.ui.components.GradientSurface
import com.rakibjoy.problembuddy.core.ui.components.TierBadge
import com.rakibjoy.problembuddy.core.ui.components.pressScale
import com.rakibjoy.problembuddy.core.ui.theme.AppShapes
import com.rakibjoy.problembuddy.core.ui.theme.ProblemBuddyTheme
import com.rakibjoy.problembuddy.domain.model.ThemeMode
import com.rakibjoy.problembuddy.core.ui.theme.Spacing
import com.rakibjoy.problembuddy.core.ui.theme.gradient
import com.rakibjoy.problembuddy.core.ui.theme.palette
import com.rakibjoy.problembuddy.domain.model.Tier
import com.rakibjoy.problembuddy.domain.model.TrainingJob

@Composable
fun TrainScreen(viewModel: TrainViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is TrainEffect.ShowToast -> Toast.makeText(
                    context,
                    effect.message,
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }
    }
    TrainScreen(state = state, onIntent = viewModel::onIntent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainScreen(
    state: TrainState,
    onIntent: (TrainIntent) -> Unit,
) {
    val job = state.activeJob
    val running = job?.status == TrainingJob.Status.RUNNING ||
        job?.status == TrainingJob.Status.QUEUED
    val succeeded = job?.status == TrainingJob.Status.SUCCESS

    GradientSurface {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = { AppTopBar(title = "Training") },
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(Spacing.lg)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Spacing.lg),
            ) {
                HeroCard()
                HandleField(
                    value = state.handleInput,
                    validation = state.handleValidation,
                    onValueChange = { onIntent(TrainIntent.HandleChanged(it)) },
                )
                if (running) {
                    OutlinedButton(
                        onClick = { onIntent(TrainIntent.CancelClicked) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .pressScale(0.97f),
                        shape = AppShapes.medium,
                    ) { Text("Cancel training") }
                } else {
                    Button(
                        onClick = { onIntent(TrainIntent.StartClicked) },
                        enabled = state.startEnabled,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .pressScale(0.97f),
                        shape = AppShapes.medium,
                    ) {
                        Text(
                            text = "Start training",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        )
                    }
                }
                if (job != null) {
                    ActiveJobCard(
                        job = job,
                        onCancel = { onIntent(TrainIntent.CancelClicked) },
                    )
                }
                AnimatedVisibility(
                    visible = succeeded,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    SuccessBanner()
                }
            }
        }
    }
}

@Composable
private fun HeroCard() {
    val brush = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.secondary,
        ),
    )
    val onColor = MaterialTheme.colorScheme.onPrimary
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AppShapes.large)
            .background(brush)
            .padding(Spacing.lg),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            // decorative
            Icon(
                imageVector = Icons.Default.Bolt,
                contentDescription = null,
                tint = onColor,
                modifier = Modifier.size(40.dp),
            )
            Text(
                "Build your corpus",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = onColor,
            )
            Text(
                "Ingest strong handles' submissions to power recommendations.",
                style = MaterialTheme.typography.bodyMedium,
                color = onColor.copy(alpha = 0.85f),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HandleField(
    value: String,
    validation: HandleValidation,
    onValueChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Codeforces handle") },
        singleLine = true,
        isError = validation is HandleValidation.Invalid,
        leadingIcon = {
            // decorative
            Icon(Icons.Default.Person, contentDescription = null)
        },
        shape = AppShapes.medium,
        supportingText = {
            AnimatedContent(
                targetState = validation,
                label = "handle-validation",
            ) { v ->
                when (v) {
                    HandleValidation.Idle -> Text(" ")
                    HandleValidation.Validating -> Text("Checking…")
                    HandleValidation.Valid -> Text(
                        "✓ Handle OK",
                        color = MaterialTheme.colorScheme.primary,
                    )
                    is HandleValidation.Invalid -> Text(
                        v.reason,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun ActiveJobCard(job: TrainingJob, onCancel: () -> Unit) {
    val tier = job.currentTier
    val accent = tier?.palette()?.strong ?: MaterialTheme.colorScheme.primary
    val running = job.status == TrainingJob.Status.RUNNING ||
        job.status == TrainingJob.Status.QUEUED
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Tier-tinted left bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(accent),
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusDot(status = job.status)
                    Spacer(Modifier.width(Spacing.sm))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Training ${job.handle}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        )
                        Text(
                            text = job.status.label(),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                if (tier != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TierBadge(tier = tier, compact = true)
                        Spacer(Modifier.width(Spacing.sm))
                        Text(
                            text = "${job.done}/${job.total}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                val progress = if (job.total > 0) {
                    job.done.toFloat() / job.total.toFloat()
                } else if (running) {
                    -1f
                } else {
                    0f
                }
                AnimatedProgressBar(
                    progress = progress,
                    accentBrush = tier?.gradient() ?: Brush.horizontalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary,
                        ),
                    ),
                )
                if (job.status == TrainingJob.Status.FAILED && !job.error.isNullOrBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(AppShapes.small)
                            .background(MaterialTheme.colorScheme.errorContainer)
                            .padding(Spacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // decorative
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(Modifier.width(Spacing.sm))
                        Text(
                            job.error,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
                if (running) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TextButton(onClick = onCancel) { Text("Cancel") }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusDot(status: TrainingJob.Status) {
    val color = when (status) {
        TrainingJob.Status.QUEUED -> MaterialTheme.colorScheme.primary
        TrainingJob.Status.RUNNING -> MaterialTheme.colorScheme.primary
        TrainingJob.Status.SUCCESS -> Color(0xFF22C55E)
        TrainingJob.Status.FAILED -> MaterialTheme.colorScheme.error
    }
    val isRunning = status == TrainingJob.Status.RUNNING
    val scale = if (isRunning) {
        val transition = rememberInfiniteTransition(label = "pulse")
        val v by transition.animateFloat(
            initialValue = 0.8f,
            targetValue = 1.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(700),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "pulse-scale",
        )
        v
    } else {
        1f
    }
    Box(
        modifier = Modifier
            .size(12.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(color),
    )
}

@Composable
private fun SuccessBanner() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        ),
    ) {
        Row(
            modifier = Modifier.padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // decorative
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
            )
            Spacer(Modifier.width(Spacing.sm))
            Text(
                "Corpus ready!",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            )
        }
    }
}

private fun TrainingJob.Status.label(): String = when (this) {
    TrainingJob.Status.QUEUED -> "Queued"
    TrainingJob.Status.RUNNING -> "Running"
    TrainingJob.Status.SUCCESS -> "Completed"
    TrainingJob.Status.FAILED -> "Failed"
}

@Preview(name = "Train - Idle", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TrainScreenIdlePreview() {
    ProblemBuddyTheme(themeMode = ThemeMode.DARK) {
        TrainScreen(state = TrainState(), onIntent = {})
    }
}

@Preview(name = "Train - Validating", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TrainScreenValidatingPreview() {
    ProblemBuddyTheme(themeMode = ThemeMode.DARK) {
        TrainScreen(
            state = TrainState(
                handleInput = "tou",
                handleValidation = HandleValidation.Validating,
            ),
            onIntent = {},
        )
    }
}

@Preview(name = "Train - Running 40%", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TrainScreenRunningPreview() {
    ProblemBuddyTheme(themeMode = ThemeMode.DARK) {
        TrainScreen(
            state = TrainState(
                handleInput = "tourist",
                handleValidation = HandleValidation.Valid,
                activeJob = TrainingJob(
                    id = 1,
                    handle = "tourist",
                    status = TrainingJob.Status.RUNNING,
                    currentTier = Tier.EXPERT,
                    done = 40,
                    total = 100,
                    error = null,
                    updatedAt = 0,
                ),
                startEnabled = false,
            ),
            onIntent = {},
        )
    }
}

@Preview(name = "Train - Success", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TrainScreenSuccessPreview() {
    ProblemBuddyTheme(themeMode = ThemeMode.DARK) {
        TrainScreen(
            state = TrainState(
                handleInput = "tourist",
                handleValidation = HandleValidation.Valid,
                activeJob = TrainingJob(
                    id = 1,
                    handle = "tourist",
                    status = TrainingJob.Status.SUCCESS,
                    currentTier = null,
                    done = 120,
                    total = 120,
                    error = null,
                    updatedAt = 0,
                ),
                startEnabled = true,
            ),
            onIntent = {},
        )
    }
}

@Preview(name = "Train - Failed", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TrainScreenFailedPreview() {
    ProblemBuddyTheme(themeMode = ThemeMode.DARK) {
        TrainScreen(
            state = TrainState(
                handleInput = "tourist",
                handleValidation = HandleValidation.Valid,
                activeJob = TrainingJob(
                    id = 1,
                    handle = "tourist",
                    status = TrainingJob.Status.FAILED,
                    currentTier = Tier.MASTER,
                    done = 17,
                    total = 120,
                    error = "Codeforces is unreachable. Try again soon.",
                    updatedAt = 0,
                ),
                startEnabled = true,
            ),
            onIntent = {},
        )
    }
}
