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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rakibjoy.problembuddy.core.ui.components.AnimatedProgressBar
import com.rakibjoy.problembuddy.core.ui.components.AppTopBar
import com.rakibjoy.problembuddy.core.ui.components.GradientSurface
import com.rakibjoy.problembuddy.core.ui.components.pressScale
import com.rakibjoy.problembuddy.core.ui.theme.ProblemBuddyTheme
import com.rakibjoy.problembuddy.core.ui.theme.Spacing
import com.rakibjoy.problembuddy.core.ui.theme.appExtras
import com.rakibjoy.problembuddy.core.ui.theme.gradient
import com.rakibjoy.problembuddy.core.ui.theme.palette
import com.rakibjoy.problembuddy.domain.model.ThemeMode
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
            topBar = { AppTopBar() },
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Spacing.lg),
            ) {
                CorpusOverviewCard(overview = state.corpus)
                IntroBlock()
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
                            .pressScale(0.97f),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp),
                    ) { Text("cancel training") }
                } else {
                    Button(
                        onClick = { onIntent(TrainIntent.StartClicked) },
                        enabled = state.startEnabled,
                        modifier = Modifier
                            .fillMaxWidth()
                            .pressScale(0.97f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp),
                    ) {
                        Text(
                            text = "start training",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
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
                if (state.handleHistory.isNotEmpty()) {
                    HandleHistorySection(
                        history = state.handleHistory,
                        canTrigger = !running,
                        onReRun = { onIntent(TrainIntent.ReRunHandle(it)) },
                        onRemove = { onIntent(TrainIntent.RemoveHandle(it)) },
                    )
                }
            }
        }
    }
}

@Composable
private fun IntroBlock() {
    val extras = MaterialTheme.appExtras
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = "TRAIN",
            style = MaterialTheme.typography.labelSmall,
            letterSpacing = 1.3.sp,
            color = extras.textTertiary,
        )
        Text(
            text = "build your corpus",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = "ingest strong handles' submissions to power recommendations.",
            style = MaterialTheme.typography.bodyMedium,
            color = extras.textSecondary,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HandleField(
    value: String,
    validation: HandleValidation,
    onValueChange: (String) -> Unit,
) {
    val extras = MaterialTheme.appExtras
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("codeforces handle") },
        singleLine = true,
        isError = validation is HandleValidation.Invalid,
        leadingIcon = {
            // decorative
            Icon(Icons.Default.Person, contentDescription = null)
        },
        shape = RoundedCornerShape(10.dp),
        supportingText = {
            AnimatedContent(
                targetState = validation,
                label = "handle-validation",
            ) { v ->
                when (v) {
                    HandleValidation.Idle -> Text(
                        text = " ",
                        style = MaterialTheme.typography.labelSmall,
                    )
                    HandleValidation.Validating -> Text(
                        text = "checking\u2026",
                        style = MaterialTheme.typography.labelSmall,
                        color = extras.textSecondary,
                    )
                    HandleValidation.Valid -> Text(
                        text = "\u2713 handle ok",
                        style = MaterialTheme.typography.labelSmall,
                        color = extras.deltaPositive,
                    )
                    is HandleValidation.Invalid -> Text(
                        text = v.reason,
                        style = MaterialTheme.typography.labelSmall,
                        color = extras.deltaNegative,
                    )
                }
            }
        },
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun ActiveJobCard(job: TrainingJob, onCancel: () -> Unit) {
    val extras = MaterialTheme.appExtras
    val tier = job.currentTier
    val running = job.status == TrainingJob.Status.RUNNING ||
        job.status == TrainingJob.Status.QUEUED
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(extras.surfaceElevated)
            .border(0.5.dp, extras.borderSubtle, RoundedCornerShape(10.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            StatusDot(status = job.status)
            Spacer(Modifier.width(Spacing.sm))
            Text(
                text = "training ${job.handle}",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        val tierName = tier?.name?.lowercase()?.replace('_', ' ')
        val subLine = when {
            tierName != null && job.total > 0 -> "$tierName · ${job.done}/${job.total}"
            tierName != null -> tierName
            job.total > 0 -> "${job.done}/${job.total}"
            else -> job.status.label()
        }
        Text(
            text = subLine,
            style = MaterialTheme.typography.labelSmall,
            color = extras.textTertiary,
        )
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
                    .clip(RoundedCornerShape(8.dp))
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
                TextButton(onClick = onCancel) { Text("cancel") }
            }
        }
    }
}

@Composable
private fun StatusDot(status: TrainingJob.Status) {
    val extras = MaterialTheme.appExtras
    val color = when (status) {
        TrainingJob.Status.QUEUED -> MaterialTheme.colorScheme.primary
        TrainingJob.Status.RUNNING -> MaterialTheme.colorScheme.primary
        TrainingJob.Status.SUCCESS -> extras.deltaPositive
        TrainingJob.Status.FAILED -> extras.deltaNegative
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
            .size(10.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(color),
    )
}

@Composable
private fun SuccessBanner() {
    val extras = MaterialTheme.appExtras
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(extras.surfaceElevated)
            .border(
                BorderStroke(0.5.dp, extras.borderSubtle),
                RoundedCornerShape(10.dp),
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // decorative
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = extras.deltaPositive,
            modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.width(Spacing.sm))
        Text(
            text = "corpus ready.",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

private fun TrainingJob.Status.label(): String = when (this) {
    TrainingJob.Status.QUEUED -> "queued"
    TrainingJob.Status.RUNNING -> "running"
    TrainingJob.Status.SUCCESS -> "completed"
    TrainingJob.Status.FAILED -> "failed"
}

@Composable
private fun CorpusOverviewCard(overview: CorpusOverview) {
    val extras = MaterialTheme.appExtras
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(extras.surfaceElevated)
            .border(0.5.dp, extras.borderSubtle, RoundedCornerShape(10.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        Text(
            text = "CORPUS",
            style = MaterialTheme.typography.labelSmall,
            letterSpacing = 1.3.sp,
            color = extras.textTertiary,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            StatCell(label = "PROBLEMS", value = overview.totalProblems.formatK(), modifier = Modifier.weight(1f))
            StatCell(label = "TAGS", value = overview.distinctTags.toString(), modifier = Modifier.weight(1f))
            StatCell(label = "HANDLES", value = overview.handleCount.toString(), modifier = Modifier.weight(1f))
        }
        if (overview.perTierCounts.values.any { it > 0 }) {
            Text(
                text = "BY DIFFICULTY",
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 1.sp,
                color = extras.textTertiary,
            )
            DifficultyHistogram(overview.perTierCounts)
        } else {
            Text(
                text = "no problems yet — add a handle below to start.",
                style = MaterialTheme.typography.bodySmall,
                color = extras.textTertiary,
            )
        }
    }
}

@Composable
private fun StatCell(label: String, value: String, modifier: Modifier = Modifier) {
    val extras = MaterialTheme.appExtras
    Column(modifier = modifier) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            letterSpacing = 1.sp,
            color = extras.textTertiary,
        )
    }
}

/**
 * Rating-range histogram: rows are labelled by the problem-rating band
 * ("< 1200", "1200–1399", …) rather than the user-rank name. CP users
 * think in rating bands, so the labels reflect that.
 */
@Composable
private fun DifficultyHistogram(perTierCounts: Map<Tier, Int>) {
    val extras = MaterialTheme.appExtras
    val tiersInOrder = Tier.entries
    val max = (perTierCounts.values.maxOrNull() ?: 0).coerceAtLeast(1)
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        tiersInOrder.forEach { tier ->
            val count = perTierCounts[tier] ?: 0
            if (count == 0) return@forEach
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = tier.ratingRangeLabel(),
                    style = MaterialTheme.typography.labelSmall,
                    color = tier.palette().strong,
                    modifier = Modifier.width(76.dp),
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(extras.surfaceRaised),
                ) {
                    val fraction = count.toFloat() / max.toFloat()
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction.coerceIn(0f, 1f))
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(tier.palette().strong),
                    )
                }
                Spacer(Modifier.width(Spacing.sm))
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = extras.textTertiary,
                    modifier = Modifier.width(40.dp),
                )
            }
        }
    }
}

@Composable
private fun HandleHistorySection(
    history: List<TrainedHandle>,
    canTrigger: Boolean,
    onReRun: (String) -> Unit,
    onRemove: (String) -> Unit,
) {
    val extras = MaterialTheme.appExtras
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        Text(
            text = "TRAINED HANDLES",
            style = MaterialTheme.typography.labelSmall,
            letterSpacing = 1.3.sp,
            color = extras.textTertiary,
        )
        history.forEach { item ->
            TrainedHandleRow(
                item = item,
                canTrigger = canTrigger,
                onReRun = { onReRun(item.handle) },
                onRemove = { onRemove(item.handle) },
            )
        }
    }
}

@Composable
private fun TrainedHandleRow(
    item: TrainedHandle,
    canTrigger: Boolean,
    onReRun: () -> Unit,
    onRemove: () -> Unit,
) {
    val extras = MaterialTheme.appExtras
    val statusColor = when (item.lastStatus) {
        TrainingJob.Status.SUCCESS -> extras.deltaPositive
        TrainingJob.Status.FAILED -> extras.deltaNegative
        TrainingJob.Status.RUNNING, TrainingJob.Status.QUEUED -> MaterialTheme.colorScheme.primary
        null -> extras.textTertiary
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(extras.surfaceElevated)
            .border(0.5.dp, extras.borderSubtle, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(statusColor),
        )
        Spacer(Modifier.width(Spacing.sm))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.handle,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = buildString {
                    append(
                        when (item.lastStatus) {
                            TrainingJob.Status.SUCCESS -> "synced"
                            TrainingJob.Status.FAILED -> "failed"
                            TrainingJob.Status.RUNNING -> "running"
                            TrainingJob.Status.QUEUED -> "queued"
                            null -> "added"
                        },
                    )
                    val ts = item.lastRunAtMillis
                    if (ts != null && ts > 0) {
                        append(" · ")
                        append(relativeTime(ts))
                    }
                },
                style = MaterialTheme.typography.labelSmall,
                color = extras.textTertiary,
            )
        }
        IconButton(
            onClick = onReRun,
            enabled = canTrigger,
            modifier = Modifier.size(32.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "re-sync ${item.handle}",
                tint = if (canTrigger) extras.accentVioletSoft else extras.textTertiary,
                modifier = Modifier.size(16.dp),
            )
        }
        IconButton(
            onClick = onRemove,
            modifier = Modifier.size(32.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.DeleteOutline,
                contentDescription = "clear history for ${item.handle}",
                tint = extras.textTertiary,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

/**
 * Human-readable rating range for a difficulty bucket. NEWBIE collapses to
 * "< 1200"; LEGENDARY becomes "3000+"; the rest are "lo–hi" where hi = next
 * bucket's floor minus one.
 */
private fun Tier.ratingRangeLabel(): String {
    val lo = floor
    val hi = Tier.entries.firstOrNull { it.floor > lo }?.floor?.minus(1)
    return when {
        this == Tier.NEWBIE -> "< ${Tier.PUPIL.floor}"
        hi == null -> "${lo}+"
        else -> "$lo–$hi"
    }
}

private fun Int.formatK(): String = when {
    this >= 1_000_000 -> "%.1fM".format(this / 1_000_000f)
    this >= 10_000 -> "${this / 1000}k"
    this >= 1000 -> "%.1fk".format(this / 1000f)
    else -> toString()
}

private fun relativeTime(timeMillis: Long): String {
    val diffSec = (System.currentTimeMillis() - timeMillis) / 1000L
    if (diffSec < 60) return "just now"
    val diffMin = diffSec / 60
    if (diffMin < 60) return "${diffMin}m ago"
    val diffH = diffMin / 60
    if (diffH < 24) return "${diffH}h ago"
    val diffD = diffH / 24
    if (diffD < 30) return "${diffD}d ago"
    val diffMo = diffD / 30
    if (diffMo < 12) return "${diffMo}mo ago"
    return "${diffMo / 12}y ago"
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
                    error = "codeforces is unreachable. try again soon.",
                    updatedAt = 0,
                ),
                startEnabled = true,
            ),
            onIntent = {},
        )
    }
}
