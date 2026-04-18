package com.rakibjoy.problembuddy.feature.recommend

import android.content.Intent
import android.net.Uri
import android.text.format.DateUtils
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FilterAltOff
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rakibjoy.problembuddy.core.ui.components.AppTopBar
import com.rakibjoy.problembuddy.core.ui.components.ChipState
import com.rakibjoy.problembuddy.core.ui.components.EmptyCorpusCard
import com.rakibjoy.problembuddy.core.ui.components.EmptyStateIllustration
import com.rakibjoy.problembuddy.core.ui.components.FilterBar
import com.rakibjoy.problembuddy.core.ui.components.FilterChipSpec
import com.rakibjoy.problembuddy.core.ui.components.GradientSurface
import com.rakibjoy.problembuddy.core.ui.components.RatingRail
import com.rakibjoy.problembuddy.core.ui.components.SkeletonCard
import com.rakibjoy.problembuddy.core.ui.components.StaleDataBanner
import com.rakibjoy.problembuddy.core.ui.components.TagChip
import com.rakibjoy.problembuddy.core.ui.components.pressScale
import com.rakibjoy.problembuddy.core.ui.theme.AppShapes
import com.rakibjoy.problembuddy.core.ui.theme.ProblemBuddyTheme
import com.rakibjoy.problembuddy.core.ui.theme.Spacing
import com.rakibjoy.problembuddy.core.ui.theme.appExtras
import com.rakibjoy.problembuddy.domain.model.Filters
import com.rakibjoy.problembuddy.domain.model.Problem
import com.rakibjoy.problembuddy.domain.model.ThemeMode
import com.rakibjoy.problembuddy.domain.model.Tier

private val KnownTags: List<String> = listOf(
    "2-sat", "binary search", "bitmasks", "brute force", "chinese remainder theorem",
    "combinatorics", "constructive algorithms", "data structures", "dfs and similar",
    "divide and conquer", "dp", "dsu", "expression parsing", "fft", "flows", "games",
    "geometry", "graph matchings", "graphs", "greedy", "hashing", "implementation",
    "interactive", "math", "matrices", "meet-in-the-middle", "number theory",
    "probabilities", "schedules", "shortest paths", "sortings", "string suffix structures",
    "strings", "ternary search", "trees", "two pointers",
)

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
                    actions = {
                        IconButton(onClick = { onIntent(RecommendIntent.OpenFilters) }) {
                            Icon(
                                imageVector = Icons.Default.Sort,
                                contentDescription = "Filters",
                                tint = MaterialTheme.appExtras.textTertiary,
                            )
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
                val ratingRange: IntRange? = remember(state.filters.minRating, state.filters.maxRating) {
                    val min = state.filters.minRating
                    val max = state.filters.maxRating
                    if (min != null && max != null && min <= max) min..max else null
                }
                val chipSpecs = remember(state.filters) {
                    buildFilterChips(state.filters)
                }
                FilterBar(
                    ratingRange = ratingRange,
                    chips = chipSpecs,
                    onChipToggled = { onIntent(RecommendIntent.OpenFilters) },
                    onRangeClicked = { onIntent(RecommendIntent.OpenFilters) },
                    modifier = Modifier.padding(bottom = 4.dp),
                )
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
                        ContentMode.Loaded -> LoadedList(
                            state = state,
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

private fun buildFilterChips(filters: Filters): List<FilterChipSpec> {
    val chips = mutableListOf<FilterChipSpec>()
    if (filters.weakOnly) chips += FilterChipSpec("weak only", ChipState.Include)
    filters.includeTags.forEach { chips += FilterChipSpec(it, ChipState.Include) }
    filters.excludeTags.forEach { chips += FilterChipSpec(it, ChipState.Exclude) }
    return chips
}

@Composable
private fun SkeletonList() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = Spacing.lg,
            end = Spacing.lg,
            bottom = Spacing.lg,
        ),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        items(5) { SkeletonCard() }
    }
}

private fun problemKey(p: Problem): String = "${p.contestId}-${p.problemIndex}"

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LoadedList(
    state: RecommendState,
    onIntent: (RecommendIntent) -> Unit,
) {
    val primaryPickKey = remember(state.problems, state.solvedKeys, state.skippedKeys) {
        state.problems.firstOrNull { p ->
            val k = problemKey(p)
            k !in state.solvedKeys && k !in state.skippedKeys
        }?.let(::problemKey)
    }
    val syncLabel = remember(state.fetchedAtMillis) { state.fetchedAtMillis?.let(::formatRelativeShort) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = Spacing.lg,
            end = Spacing.lg,
            bottom = Spacing.lg,
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (state.stale) {
            item(key = "stale-banner") {
                StaleDataBanner(
                    modifier = Modifier.padding(bottom = 4.dp),
                    fetchedAtMillis = state.fetchedAtMillis,
                )
            }
        }
        itemsIndexed(
            items = state.problems,
            key = { _, p -> problemKey(p) },
        ) { index, problem ->
            var visible by remember { mutableStateOf(false) }
            LaunchedEffect(problem.contestId, problem.problemIndex) {
                val delayMs = (60L * index.coerceAtMost(6))
                kotlinx.coroutines.delay(delayMs)
                visible = true
            }
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn() + slideInVertically { it / 4 },
                exit = fadeOut(),
                modifier = Modifier.animateItemPlacement(),
            ) {
                val key = problemKey(problem)
                val variant = when {
                    key in state.solvedKeys -> CardVariant.Solved
                    key in state.skippedKeys -> CardVariant.Skipped
                    else -> CardVariant.Default
                }
                RecommendProblemCard(
                    problem = problem,
                    weakTags = state.weakTags,
                    variant = variant,
                    isPrimaryPick = key == primaryPickKey,
                    onSolve = { onIntent(RecommendIntent.OpenUrl(problem)) },
                    onSave = { onIntent(RecommendIntent.Skip(problem)) },
                )
            }
        }
        item(key = "footer") {
            val total = state.totalMatching ?: state.problems.size
            val shown = state.problems.size
            val base = "showing $shown of $total"
            val text = if (syncLabel != null) "$base · corpus last synced $syncLabel" else base
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.appExtras.textTertiary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
            )
        }
    }
}

private enum class CardVariant { Default, Solved, Skipped }

@Composable
private fun RecommendProblemCard(
    problem: Problem,
    weakTags: Set<String>,
    variant: CardVariant,
    isPrimaryPick: Boolean,
    onSolve: () -> Unit,
    onSave: () -> Unit,
) {
    val extras = MaterialTheme.appExtras
    val tier = remember(problem.rating) { Tier.forMaxRating(problem.rating ?: 0) }
    val shape = AppShapes.large
    val displayName = problem.name.ifBlank { "Problem ${problem.contestId}${problem.problemIndex}" }
    val rowAlpha = when (variant) {
        CardVariant.Default -> 1f
        CardVariant.Solved -> 0.45f
        CardVariant.Skipped -> 0.30f
    }
    val metaText = buildMetaText(problem)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(rowAlpha)
            .clip(shape)
            .background(extras.surfaceElevated, shape)
            .border(0.5.dp, extras.borderSubtle, shape)
            .pressScale(0.99f)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RatingRail(rating = problem.rating ?: 0, tier = tier)
        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = displayName,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = (-0.12).sp,
                    textDecoration = if (variant == CardVariant.Solved) TextDecoration.LineThrough else null,
                ),
                color = if (variant == CardVariant.Solved) extras.textTertiary else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.size(5.dp))
            Text(
                text = metaText,
                style = MaterialTheme.typography.labelSmall,
                color = extras.textTertiary,
            )
            if (problem.tags.isNotEmpty()) {
                Spacer(Modifier.size(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    problem.tags.take(4).forEach { tag ->
                        TagChip(tag = tag, weak = tag in weakTags)
                    }
                }
            }
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            when (variant) {
                CardVariant.Solved -> SolvedCheckIcon()
                CardVariant.Skipped -> { /* no actions */ }
                CardVariant.Default -> {
                    ActionButton(
                        onClick = onSolve,
                        tint = if (isPrimaryPick) extras.accentVioletSoft else extras.textTertiary,
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Open problem",
                            modifier = Modifier.size(13.dp),
                        )
                    }
                    ActionButton(
                        onClick = onSave,
                        tint = extras.textTertiary,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Skip",
                            modifier = Modifier.size(13.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionButton(
    onClick: () -> Unit,
    tint: Color,
    content: @Composable () -> Unit,
) {
    val extras = MaterialTheme.appExtras
    val shape = RoundedCornerShape(7.dp)
    Box(
        modifier = Modifier
            .size(26.dp)
            .clip(shape)
            .border(0.5.dp, extras.borderSubtle, shape)
            .clickable(onClick = onClick)
            .pressScale(0.92f),
        contentAlignment = Alignment.Center,
    ) {
        androidx.compose.runtime.CompositionLocalProvider(
            androidx.compose.material3.LocalContentColor provides tint,
            content = content,
        )
    }
}

@Composable
private fun SolvedCheckIcon() {
    Box(
        modifier = Modifier.size(26.dp),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = "Solved",
            tint = MaterialTheme.appExtras.deltaPositive,
            modifier = Modifier.size(15.dp),
        )
    }
}

private fun buildMetaText(p: Problem): String {
    // Best-effort: "$contestId · $index". Division is unknown from the
    // domain model, so we omit it rather than guessing.
    return "${p.contestId} · ${p.problemIndex}"
}

private fun formatRelativeShort(fetchedAtMillis: Long): String {
    val now = System.currentTimeMillis()
    return DateUtils.getRelativeTimeSpanString(
        fetchedAtMillis,
        now,
        DateUtils.MINUTE_IN_MILLIS,
        DateUtils.FORMAT_ABBREV_RELATIVE,
    ).toString()
}

// ---------------------------------------------------------------------------
// Filter sheet (unchanged from prior revision, kept minimal)
// ---------------------------------------------------------------------------

private enum class TagSelectState { None, Include, Exclude }

@Composable
private fun ExcludableTagChip(
    tag: String,
    state: TagSelectState,
    onClick: () -> Unit,
) {
    TagChip(
        tag = tag,
        selected = state == TagSelectState.Include,
        excluded = state == TagSelectState.Exclude,
        onClick = onClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
    var includeTags by remember { mutableStateOf(current.includeTags.toSet()) }
    var excludeTags by remember { mutableStateOf(current.excludeTags.toSet()) }
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
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Text("Include any of", style = MaterialTheme.typography.titleSmall)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                ) {
                    KnownTags.forEach { tag ->
                        val selState = if (includeTags.contains(tag)) TagSelectState.Include else TagSelectState.None
                        ExcludableTagChip(
                            tag = tag,
                            state = selState,
                            onClick = {
                                includeTags = if (includeTags.contains(tag)) {
                                    includeTags - tag
                                } else {
                                    excludeTags = excludeTags - tag
                                    includeTags + tag
                                }
                            },
                        )
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Text("Exclude", style = MaterialTheme.typography.titleSmall)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                ) {
                    KnownTags.forEach { tag ->
                        val selState = if (excludeTags.contains(tag)) TagSelectState.Exclude else TagSelectState.None
                        ExcludableTagChip(
                            tag = tag,
                            state = selState,
                            onClick = {
                                excludeTags = if (excludeTags.contains(tag)) {
                                    excludeTags - tag
                                } else {
                                    includeTags = includeTags - tag
                                    excludeTags + tag
                                }
                            },
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Only weak tags", style = MaterialTheme.typography.titleSmall)
                    Text(
                        "Focus on topics you struggle with.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(checked = weakOnly, onCheckedChange = { weakOnly = it })
            }
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
                                includeTags = includeTags,
                                excludeTags = excludeTags,
                                weakOnly = weakOnly,
                            ),
                        )
                    },
                ) { Text("Apply") }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview(name = "Recommend - Loading (Dark)", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun RecommendScreenLoadingPreview() {
    ProblemBuddyTheme(themeMode = ThemeMode.DARK) {
        RecommendScreen(state = RecommendState(loading = true), onIntent = {})
    }
}

@Preview(name = "Recommend - Loaded (Dark)", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun RecommendScreenLoadedPreview() {
    val sample = listOf(
        Problem(1854, "G", "Segment Tree Beats", 2100, listOf("data structures", "segment tree", "lazy prop")),
        Problem(1891, "F", "K-D Tree Queries", 1900, listOf("data structures", "divide and conquer")),
        Problem(1849, "H", "Persistent Segment Tree", 2000, listOf("trees", "data structures")),
        Problem(1872, "E", "Euler Tour Trick", 1800, listOf("trees", "dfs and similar")),
    )
    ProblemBuddyTheme(themeMode = ThemeMode.DARK) {
        RecommendScreen(
            state = RecommendState(
                loading = false,
                problems = sample,
                weakTags = setOf("data structures", "dfs and similar"),
                totalMatching = 138,
                fetchedAtMillis = System.currentTimeMillis() - 2 * 3_600_000L,
                solvedKeys = setOf("1891-F"),
                skippedKeys = setOf("1849-H"),
                filters = Filters(
                    minRating = 1800,
                    maxRating = 2200,
                    includeTags = setOf("data structures"),
                    excludeTags = setOf("greedy"),
                    weakOnly = true,
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
