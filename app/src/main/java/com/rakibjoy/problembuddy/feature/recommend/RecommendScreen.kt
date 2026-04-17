package com.rakibjoy.problembuddy.feature.recommend

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FilterAltOff
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rakibjoy.problembuddy.core.ui.components.AnimatedProgressBar
import com.rakibjoy.problembuddy.core.ui.components.AppTopBar
import com.rakibjoy.problembuddy.core.ui.components.EmptyCorpusCard
import com.rakibjoy.problembuddy.core.ui.components.EmptyStateIllustration
import com.rakibjoy.problembuddy.core.ui.components.GradientSurface
import com.rakibjoy.problembuddy.core.ui.components.SkeletonCard
import com.rakibjoy.problembuddy.core.ui.components.StaleDataBanner
import com.rakibjoy.problembuddy.core.ui.components.TagChip
import com.rakibjoy.problembuddy.core.ui.components.TierBadge
import com.rakibjoy.problembuddy.core.ui.components.pressScale
import com.rakibjoy.problembuddy.core.ui.theme.AppShapes
import com.rakibjoy.problembuddy.core.ui.theme.ProblemBuddyTheme
import com.rakibjoy.problembuddy.domain.model.ThemeMode
import com.rakibjoy.problembuddy.core.ui.theme.Spacing
import com.rakibjoy.problembuddy.core.ui.theme.gradient
import com.rakibjoy.problembuddy.core.ui.theme.palette
import com.rakibjoy.problembuddy.domain.model.Filters
import com.rakibjoy.problembuddy.domain.model.Problem
import com.rakibjoy.problembuddy.domain.model.Tier

@Composable
fun RecommendScreen(
    onNavigateToTrain: (() -> Unit)? = null,
    viewModel: RecommendViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is RecommendEffect.OpenUrl -> {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(effect.url))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                }
                is RecommendEffect.Toast -> Toast.makeText(
                    context,
                    effect.message,
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }
    }
    RecommendScreen(
        state = state,
        onIntent = viewModel::onIntent,
        onNavigateToTrain = onNavigateToTrain,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendScreen(
    state: RecommendState,
    onIntent: (RecommendIntent) -> Unit,
    onNavigateToTrain: (() -> Unit)? = null,
) {
    GradientSurface {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                AppTopBar(
                    title = "For you",
                    actions = {
                        IconButton(onClick = { onIntent(RecommendIntent.OpenFilters) }) {
                            Icon(Icons.Filled.FilterList, contentDescription = "Filters")
                        }
                    },
                )
            },
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
            ) {
                if (state.stale) {
                    StaleDataBanner(
                        modifier = Modifier.padding(
                            horizontal = Spacing.lg,
                            vertical = Spacing.sm,
                        ),
                        fetchedAtMillis = state.fetchedAtMillis,
                    )
                }
                Crossfade(
                    targetState = when {
                        state.loading -> ContentMode.Loading
                        state.error != null -> ContentMode.Error
                        state.problems.isEmpty() && !state.hasCorpus -> ContentMode.NoCorpus
                        state.problems.isEmpty() -> ContentMode.Empty
                        else -> ContentMode.Loaded
                    },
                    modifier = Modifier.fillMaxSize(),
                    label = "recommend-content",
                ) { mode ->
                    when (mode) {
                        ContentMode.Loading -> SkeletonList()
                        ContentMode.Error -> Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            EmptyStateIllustration(
                                icon = Icons.Default.ErrorOutline,
                                title = state.error.orEmpty(),
                                subtitle = "We'll try again when you refresh.",
                                actionLabel = "Retry",
                                onAction = { onIntent(RecommendIntent.Refresh) },
                            )
                        }
                        ContentMode.NoCorpus -> Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(Spacing.lg),
                            contentAlignment = Alignment.Center,
                        ) {
                            EmptyCorpusCard(onTrainClicked = { onNavigateToTrain?.invoke() })
                        }
                        ContentMode.Empty -> Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            EmptyStateIllustration(
                                icon = Icons.Default.FilterAltOff,
                                title = "No matches",
                                subtitle = "Try widening the rating range or removing tag filters.",
                                actionLabel = "Edit filters",
                                onAction = { onIntent(RecommendIntent.OpenFilters) },
                            )
                        }
                        ContentMode.Loaded -> ProblemList(
                            problems = state.problems,
                            onIntent = onIntent,
                        )
                    }
                }
            }
            if (state.filterSheetOpen) {
                FilterSheet(
                    current = state.filters,
                    onCancel = { onIntent(RecommendIntent.CloseFilters) },
                    onApply = { onIntent(RecommendIntent.ApplyFilters(it)) },
                )
            }
        }
    }
}

private enum class ContentMode { Loading, Error, NoCorpus, Empty, Loaded }

@Composable
private fun SkeletonList() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        items(5) { SkeletonCard() }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ProblemList(
    problems: List<Problem>,
    onIntent: (RecommendIntent) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        itemsIndexed(
            items = problems,
            key = { _, p -> "${p.contestId}-${p.problemIndex}" },
        ) { index, problem ->
            var visible by remember { mutableStateOf(false) }
            LaunchedEffect(problem.contestId, problem.problemIndex) {
                val delayMs = (80L * index.coerceAtMost(6))
                kotlinx.coroutines.delay(delayMs)
                visible = true
            }
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn() + slideInVertically { it / 4 },
                exit = fadeOut(),
                modifier = Modifier.animateItemPlacement(),
            ) {
                ProblemCard(
                    problem = problem,
                    onSolve = { onIntent(RecommendIntent.OpenUrl(problem)) },
                    onMarkSolved = { onIntent(RecommendIntent.MarkSolved(problem)) },
                    onSkip = { onIntent(RecommendIntent.Skip(problem)) },
                )
            }
        }
    }
}

@Composable
private fun ProblemCard(
    problem: Problem,
    onSolve: () -> Unit,
    onMarkSolved: () -> Unit,
    onSkip: () -> Unit,
) {
    val tier = remember(problem.rating) { Tier.forMaxRating(problem.rating ?: 0) }
    val palette = tier.palette()
    val brush = remember(tier) { tier.gradient() }
    val onColor = palette.onColor
    val shape = AppShapes.large
    val displayName = problem.name.ifBlank { "Problem ${problem.contestId}${problem.problemIndex}" }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .pressScale(0.98f)
            .clip(shape)
            .background(brush),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 200.dp)
                .padding(Spacing.lg),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TierBadge(tier = tier, compact = true)
                Spacer(Modifier.width(Spacing.sm))
                Box(Modifier.weight(1f))
                Text(
                    text = "#${problem.contestId}${problem.problemIndex}",
                    style = MaterialTheme.typography.labelMedium,
                    color = onColor.copy(alpha = 0.8f),
                )
            }
            Spacer(Modifier.size(Spacing.md))
            Text(
                text = displayName,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = onColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.size(Spacing.sm))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = null,
                    tint = onColor.copy(alpha = 0.9f),
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(Spacing.xs))
                Text(
                    text = "${problem.rating ?: "—"}",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = onColor.copy(alpha = 0.9f),
                )
            }
            if (problem.tags.isNotEmpty()) {
                Spacer(Modifier.size(Spacing.sm))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                ) {
                    problem.tags.take(5).forEach { tag ->
                        OnGradientTagChip(tag = tag, onColor = onColor)
                    }
                }
            }
            Box(modifier = Modifier.weight(1f, fill = true))
            Spacer(Modifier.size(Spacing.md))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    onClick = onSolve,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black,
                    ),
                    modifier = Modifier.pressScale(0.96f),
                ) {
                    Text("Solve", fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.width(Spacing.xs))
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                }
                OutlinedButton(
                    onClick = onMarkSolved,
                    modifier = Modifier.pressScale(0.96f),
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = onColor,
                    )
                    Spacer(Modifier.width(Spacing.xs))
                    Text("Solved", color = onColor)
                }
                Box(Modifier.weight(1f))
                IconButton(onClick = onSkip) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Skip",
                        tint = onColor,
                    )
                }
            }
        }
    }
}

/**
 * A TagChip variant that reads well when rendered on top of a tier gradient.
 * Uses a semi-transparent surface tint instead of surfaceVariant for contrast.
 */
@Composable
private fun OnGradientTagChip(tag: String, onColor: Color) {
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f),
        contentColor = onColor,
        shape = AppShapes.small,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(tagAccent(tag)),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "#$tag",
                style = MaterialTheme.typography.labelSmall,
                color = onColor,
            )
        }
    }
}

private fun tagAccent(tag: String): Color {
    val goldenAngle = 137.508f
    val hue = (kotlin.math.abs(tag.hashCode()) * goldenAngle) % 360f
    return Color.hsl(hue, saturation = 0.6f, lightness = 0.7f)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterSheet(
    current: Filters,
    onCancel: () -> Unit,
    onApply: (Filters) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var count by remember { mutableStateOf(current.count.toFloat()) }
    var minRating by remember { mutableStateOf(current.minRating?.toString() ?: "") }
    var maxRating by remember { mutableStateOf(current.maxRating?.toString() ?: "") }
    var includeTags by remember { mutableStateOf(current.includeTags.joinToString(", ")) }
    var excludeTags by remember { mutableStateOf(current.excludeTags.joinToString(", ")) }
    var weakOnly by remember { mutableStateOf(current.weakOnly) }

    ModalBottomSheet(onDismissRequest = onCancel, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.lg, vertical = Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg),
        ) {
            Text(
                "Filters",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            )
            // Count section
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Problems per load",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.weight(1f),
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        shape = AppShapes.small,
                    ) {
                        Text(
                            text = count.toInt().toString(),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        )
                    }
                }
                Slider(
                    value = count,
                    onValueChange = { count = it },
                    valueRange = 1f..30f,
                    steps = 28,
                )
            }
            // Rating range
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Text("Rating range", style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    OutlinedTextField(
                        value = minRating,
                        onValueChange = { v -> minRating = v.filter { it.isDigit() } },
                        label = { Text("Min") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = AppShapes.medium,
                    )
                    OutlinedTextField(
                        value = maxRating,
                        onValueChange = { v -> maxRating = v.filter { it.isDigit() } },
                        label = { Text("Max") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = AppShapes.medium,
                    )
                }
            }
            OutlinedTextField(
                value = includeTags,
                onValueChange = { includeTags = it },
                label = { Text("Include tags (comma-separated)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = AppShapes.medium,
            )
            OutlinedTextField(
                value = excludeTags,
                onValueChange = { excludeTags = it },
                label = { Text("Exclude tags (comma-separated)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = AppShapes.medium,
            )
            SwitchCardRow(
                title = "Only weak tags",
                subtitle = "Focus on topics you struggle with.",
                checked = weakOnly,
                onCheckedChange = { weakOnly = it },
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = onCancel) { Text("Cancel") }
                Spacer(Modifier.width(Spacing.sm))
                Button(
                    modifier = Modifier.pressScale(0.96f),
                    onClick = {
                        onApply(
                            Filters(
                                count = count.toInt().coerceIn(1, 30),
                                minRating = minRating.toIntOrNull(),
                                maxRating = maxRating.toIntOrNull(),
                                includeTags = includeTags.splitTags(),
                                excludeTags = excludeTags.splitTags(),
                                weakOnly = weakOnly,
                            ),
                        )
                    },
                ) { Text("Apply") }
            }
        }
    }
}

@Composable
private fun SwitchCardRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.md, vertical = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

private fun String.splitTags(): Set<String> =
    split(',').map { it.trim() }.filter { it.isNotEmpty() }.toSet()

@Suppress("unused")
private val UnusedBrush: Brush = Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))

@Preview(name = "Recommend - Loading (Dark)", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun RecommendScreenLoadingPreview() {
    ProblemBuddyTheme(themeMode = ThemeMode.DARK) {
        RecommendScreen(state = RecommendState(loading = true), onIntent = {})
    }
}

@Preview(name = "Recommend - Cards (Dark)", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun RecommendScreenCardsPreview() {
    ProblemBuddyTheme(themeMode = ThemeMode.DARK) {
        RecommendScreen(
            state = RecommendState(
                loading = false,
                problems = listOf(
                    Problem(1520, "A", "Do Not Be Distracted", 800, listOf("implementation", "strings")),
                    Problem(1530, "B", "Putting Plates", 1500, listOf("constructive algorithms", "greedy")),
                    Problem(1540, "C", "Tree XOR", 2100, listOf("dp", "trees", "bitmasks")),
                ),
            ),
            onIntent = {},
        )
    }
}

@Preview(name = "Recommend - Empty (Dark)", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun RecommendScreenEmptyPreview() {
    ProblemBuddyTheme(themeMode = ThemeMode.DARK) {
        RecommendScreen(
            state = RecommendState(loading = false, problems = emptyList(), hasCorpus = true),
            onIntent = {},
        )
    }
}

@Preview(name = "Recommend - Error (Dark)", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun RecommendScreenErrorPreview() {
    ProblemBuddyTheme(themeMode = ThemeMode.DARK) {
        RecommendScreen(
            state = RecommendState(loading = false, error = "Couldn't reach Codeforces"),
            onIntent = {},
        )
    }
}
