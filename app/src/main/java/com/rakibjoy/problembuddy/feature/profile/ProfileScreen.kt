package com.rakibjoy.problembuddy.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rakibjoy.problembuddy.core.ui.components.AnimatedProgressBar
import com.rakibjoy.problembuddy.core.ui.components.EmptyCorpusCard
import com.rakibjoy.problembuddy.core.ui.components.EmptyStateIllustration
import com.rakibjoy.problembuddy.core.ui.components.AchievementRow
import com.rakibjoy.problembuddy.core.ui.components.ActivityHeatmap
import com.rakibjoy.problembuddy.core.ui.components.AppTopBar
import com.rakibjoy.problembuddy.core.ui.components.ComparisonCard
import com.rakibjoy.problembuddy.core.ui.components.ContestRow
import com.rakibjoy.problembuddy.core.ui.components.DayOfWeekChart
import com.rakibjoy.problembuddy.core.ui.components.DivisionDeltasCard
import com.rakibjoy.problembuddy.core.ui.components.FailedProblemRow
import com.rakibjoy.problembuddy.core.ui.components.FullRatingTimeline
import com.rakibjoy.problembuddy.core.ui.components.HourOfDayChart
import com.rakibjoy.problembuddy.core.ui.components.LanguageBar
import com.rakibjoy.problembuddy.core.ui.components.ProjectionCard
import com.rakibjoy.problembuddy.core.ui.components.SnapshotRow
import com.rakibjoy.problembuddy.core.ui.components.TagRadar
import com.rakibjoy.problembuddy.core.ui.components.TierStackedArea
import com.rakibjoy.problembuddy.core.ui.components.VerdictBar
import com.rakibjoy.problembuddy.core.ui.components.GradientSurface
import com.rakibjoy.problembuddy.core.ui.components.Sparkline
import com.rakibjoy.problembuddy.core.ui.theme.AppShapes
import com.rakibjoy.problembuddy.core.ui.components.HandleAvatar
import com.rakibjoy.problembuddy.core.ui.components.HandleText
import com.rakibjoy.problembuddy.core.ui.components.SkeletonCard
import com.rakibjoy.problembuddy.core.ui.components.SkeletonLine
import com.rakibjoy.problembuddy.core.ui.components.StaleDataBanner
import com.rakibjoy.problembuddy.core.ui.components.TabsRow
import com.rakibjoy.problembuddy.core.ui.components.TagChip
import com.rakibjoy.problembuddy.core.ui.components.VerticalTierLadder
import com.rakibjoy.problembuddy.core.ui.theme.ProblemBuddyTheme
import com.rakibjoy.problembuddy.core.ui.theme.Spacing
import com.rakibjoy.problembuddy.core.ui.theme.appExtras
import com.rakibjoy.problembuddy.core.ui.theme.gradient
import com.rakibjoy.problembuddy.core.ui.theme.palette
import com.rakibjoy.problembuddy.domain.model.Tier

@Composable
fun ProfileScreen(
    onNavigateToTrain: (() -> Unit)? = null,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ProfileScreen(
        state = state,
        onIntent = viewModel::onIntent,
        onNavigateToTrain = onNavigateToTrain,
    )
}

@Composable
fun ProfileScreen(
    state: ProfileState,
    onIntent: (ProfileIntent) -> Unit,
    onNavigateToTrain: (() -> Unit)? = null,
) {
    GradientSurface {
        Column(modifier = Modifier.fillMaxSize()) {
            AppTopBar()
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    state.loading -> LoadingContent()
                    state.error != null -> ErrorContent(
                        message = state.error,
                        onRetry = { onIntent(ProfileIntent.Refresh) },
                    )
                    else -> ProfileContent(
                        state = state,
                        onIntent = onIntent,
                        onNavigateToTrain = onNavigateToTrain,
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg),
    ) {
        SkeletonCard(modifier = Modifier.height(140.dp))
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            SkeletonLine(width = 220.dp, height = 14.dp)
            SkeletonLine(width = 180.dp, height = 14.dp)
            SkeletonLine(width = 260.dp, height = 14.dp)
            SkeletonLine(width = 160.dp, height = 14.dp)
            SkeletonLine(width = 200.dp, height = 14.dp)
            SkeletonLine(width = 140.dp, height = 14.dp)
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        EmptyStateIllustration(
            icon = Icons.Default.ErrorOutline,
            title = message,
            subtitle = "We couldn't load your profile. Check your connection and try again.",
            actionLabel = "Retry",
            onAction = onRetry,
        )
    }
}

@Composable
private fun ProfileContent(
    state: ProfileState,
    onIntent: (ProfileIntent) -> Unit,
    onNavigateToTrain: (() -> Unit)? = null,
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val tabs = remember { listOf("tier ladder", "weak tags", "activity", "compare") }

    Column(modifier = Modifier.fillMaxSize()) {
        ProfileHero(
            handle = state.handle,
            currentTier = state.currentTier,
            avatarUrl = state.avatarUrl,
            maxRating = state.maxRating,
            problemsSolved = state.problemsSolved,
            coveragePct = state.coveragePct,
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = Spacing.lg,
                end = Spacing.lg,
                top = 0.dp,
                bottom = Spacing.lg,
            ),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            if (state.stale) {
                item {
                    StaleDataBanner(fetchedAtMillis = state.fetchedAtMillis)
                }
            }
            item {
                TabsRow(
                    tabs = tabs,
                    selectedIndex = selectedTab,
                    onSelect = { selectedTab = it },
                )
            }

            when (selectedTab) {
                0 -> tierLadderTab(state)
                1 -> weakTagsTab(state, onNavigateToTrain)
                2 -> activityTab(state)
                3 -> compareTab(state, onSetCompareHandle = { onIntent(ProfileIntent.SetCompareHandle(it)) })
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.tierLadderTab(state: ProfileState) {
    val tier = state.currentTier
    val rating = state.rating ?: state.maxRating ?: 0
    if (tier != null) {
        item(key = "ladder") {
            VerticalTierLadder(currentTier = tier, currentRating = rating)
        }
        if (tier != Tier.NEWBIE && state.maxRating != null) {
            item(key = "ladder-caption") {
                LadderCaption(maxRating = state.maxRating)
            }
        }
    } else {
        item(key = "ladder-unknown") {
            CenterHint(text = "tier unknown")
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.weakTagsTab(
    state: ProfileState,
    onNavigateToTrain: (() -> Unit)?,
) {
    if (state.weakTags.isEmpty()) {
        item(key = "weak-empty") {
            EmptyCorpusCard(onTrainClicked = { onNavigateToTrain?.invoke() })
        }
    } else {
        itemsIndexed(state.weakTags, key = { _, it -> "weak-${it.tag}" }) { _, stat ->
            WeakTagCard(stat = stat, currentTier = state.currentTier)
        }
    }
}

private fun LazyListScope.activityTab(state: ProfileState) {
    val activity = state.activity
    if (activity == null) {
        item(key = "activity-empty") {
            CenterHint(text = "no activity yet — solve a problem to get started.")
        }
        return
    }

    // 1. Year-ago snapshot
    if (activity.oneYearAgo != null) {
        item(key = "activity-snapshot") {
            Column {
                SmallSectionHeader("A YEAR AGO")
                val today = ProfileSnapshot(
                    timeSeconds = System.currentTimeMillis() / 1000L,
                    rating = state.rating,
                    solvedCount = activity.solvedByDayEpoch.values.sum(),
                    tier = state.currentTier,
                )
                SnapshotRow(oneYearAgo = activity.oneYearAgo, today = today)
            }
        }
    }

    // 2. Projection
    if (activity.projection != null) {
        item(key = "activity-projection") {
            Column {
                SmallSectionHeader("NEXT TIER")
                ProjectionCard(projection = activity.projection)
            }
        }
    }

    // 3. Submissions heatmap
    if (activity.solvedByDayEpoch.isNotEmpty()) {
        item(key = "activity-heatmap") {
            OutlinedSurfaceCard {
                Column {
                    SmallSectionHeader("SUBMISSIONS")
                    Spacer(Modifier.height(Spacing.sm))
                    ActivityHeatmap(solvedByDayEpoch = activity.solvedByDayEpoch)
                }
            }
        }
    }

    // 4. Streak row
    item(key = "activity-streak") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            ActivityStat(
                label = "STREAK",
                value = activity.currentStreakDays.toString(),
                delta = "days",
                modifier = Modifier.weight(1f),
            )
            ActivityStat(
                label = "LONGEST",
                value = activity.longestStreakDays.toString(),
                delta = "days",
                modifier = Modifier.weight(1f),
            )
            ActivityStat(
                label = "THIS YEAR",
                value = activity.solvedThisYear.toString(),
                delta = "solved",
                modifier = Modifier.weight(1f),
            )
        }
    }

    // 5. Rating timeline + selected contest inline
    if (activity.ratingHistory.isNotEmpty()) {
        item(key = "activity-rating-timeline") {
            val selected = remember { mutableStateOf<ContestResult?>(null) }
            Column {
                SmallSectionHeader("RATING HISTORY")
                Spacer(Modifier.height(Spacing.sm))
                FullRatingTimeline(
                    contests = activity.contestHistory,
                    onTap = { selected.value = it },
                )
                val sel = selected.value
                if (sel != null) {
                    Spacer(Modifier.height(Spacing.sm))
                    OutlinedSurfaceCard {
                        Column {
                            Text(
                                text = sel.name,
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Spacer(Modifier.height(2.dp))
                            val delta = sel.newRating - sel.oldRating
                            val deltaText = if (delta >= 0) "+$delta" else "$delta"
                            val date = java.time.Instant.ofEpochSecond(sel.timeSeconds)
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate()
                                .toString()
                            Text(
                                text = "rank ${sel.rank} · $deltaText · $date",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.appExtras.textSecondary,
                            )
                        }
                    }
                }
            }
        }
    }

    // 6. Recent contests (last 10)
    if (activity.contestHistory.isNotEmpty()) {
        item(key = "activity-recent-contests") {
            val all = activity.contestHistory.sortedByDescending { it.timeSeconds }
            val shown = all.take(10)
            Column {
                SmallSectionHeader("RECENT CONTESTS")
                if (all.size > 10) {
                    Text(
                        text = "showing 10 of ${all.size}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.appExtras.textTertiary,
                    )
                }
                Spacer(Modifier.height(Spacing.xs))
                OutlinedSurfaceCard {
                    Column {
                        shown.forEachIndexed { idx, c ->
                            ContestRow(contest = c)
                            if (idx != shown.lastIndex) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(0.5.dp)
                                        .background(MaterialTheme.appExtras.borderSubtle),
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // 7. Division deltas
    if (activity.divisionDeltas.isNotEmpty()) {
        item(key = "activity-division-deltas") {
            Column {
                SmallSectionHeader("BY DIVISION")
                DivisionDeltasCard(divisions = activity.divisionDeltas)
            }
        }
    }

    // 8. Tier progression
    if (activity.tierProgression.isNotEmpty()) {
        item(key = "activity-tier-progression") {
            Column {
                SmallSectionHeader("PROBLEMS SOLVED OVER TIME")
                OutlinedSurfaceCard {
                    TierStackedArea(points = activity.tierProgression)
                }
            }
        }
    }

    // 9. Tag radar
    if (activity.tagRadar.isNotEmpty() && activity.tagRadar.any { it.count > 0 }) {
        item(key = "activity-tag-radar") {
            Column {
                SmallSectionHeader("TAG COVERAGE")
                OutlinedSurfaceCard {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        TagRadar(scores = activity.tagRadar)
                    }
                }
            }
        }
    }

    // 10. First-attempt AC rate
    if (activity.firstAttemptAcRate != null) {
        item(key = "activity-first-ac") {
            OutlinedSurfaceCard {
                Column {
                    Text(
                        text = "first-attempt AC rate",
                        style = MaterialTheme.typography.labelSmall,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.appExtras.textTertiary,
                    )
                    Spacer(Modifier.height(Spacing.xs))
                    val pct = (activity.firstAttemptAcRate * 100).toInt()
                    Text(
                        text = "$pct%",
                        style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.appExtras.accentVioletSoft,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "% of problems you nailed on your first submit.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.appExtras.textSecondary,
                    )
                }
            }
        }
    }

    // 11. Verdict breakdown
    if (activity.verdictBreakdown.isNotEmpty()) {
        item(key = "activity-verdicts") {
            Column {
                SmallSectionHeader("VERDICTS (90D)")
                OutlinedSurfaceCard {
                    VerdictBar(counts = activity.verdictBreakdown)
                }
            }
        }
    }

    // 12. Failed queue
    if (activity.failedQueue.isNotEmpty()) {
        item(key = "activity-failed-queue") {
            val context = LocalContext.current
            Column {
                SmallSectionHeader("RECENTLY FAILED")
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                    activity.failedQueue.take(8).forEach { item ->
                        FailedProblemRow(
                            item = item,
                            onRetry = {
                                val url = "https://codeforces.com/problemset/problem/${item.contestId}/${item.problemIndex}"
                                runCatching {
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        },
                                    )
                                }
                            },
                        )
                    }
                }
            }
        }
    }

    // 13. When you solve — day-of-week + hour-of-day
    val anyDow = activity.dayOfWeekCounts.any { it > 0 }
    val anyHour = activity.hourOfDayCounts.any { it > 0 }
    if (anyDow || anyHour) {
        item(key = "activity-when") {
            Column {
                SmallSectionHeader("WHEN YOU SOLVE")
                OutlinedSurfaceCard {
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                        DayOfWeekChart(dayCounts = activity.dayOfWeekCounts)
                        HourOfDayChart(hourCounts = activity.hourOfDayCounts)
                    }
                }
            }
        }
    }

    // 14. Language distribution
    if (activity.languageCounts.isNotEmpty()) {
        item(key = "activity-languages") {
            Column {
                SmallSectionHeader("LANGUAGES")
                OutlinedSurfaceCard {
                    LanguageBar(counts = activity.languageCounts)
                }
            }
        }
    }

    // 15. Virtual vs rated
    if (activity.virtualParticipations > 0 || activity.ratedParticipations > 0) {
        item(key = "activity-virtual-rated") {
            OutlinedSurfaceCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.lg),
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "RATED",
                            style = MaterialTheme.typography.labelSmall,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.appExtras.textTertiary,
                        )
                        Text(
                            text = activity.ratedParticipations.toString(),
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "VIRTUAL",
                            style = MaterialTheme.typography.labelSmall,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.appExtras.textTertiary,
                        )
                        Text(
                            text = activity.virtualParticipations.toString(),
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.appExtras.textSecondary,
                        )
                    }
                }
            }
        }
    }

    // 16. Milestones
    if (activity.milestones.isNotEmpty()) {
        item(key = "activity-milestones") {
            Column {
                SmallSectionHeader("MILESTONES")
                OutlinedSurfaceCard {
                    Column {
                        activity.milestones.forEach { m ->
                            AchievementRow(milestone = m)
                        }
                    }
                }
            }
        }
    }

    // Compare has moved to its own tab — no compare item in the activity tab.
}

private fun androidx.compose.foundation.lazy.LazyListScope.compareTab(
    state: ProfileState,
    onSetCompareHandle: (String) -> Unit,
) {
    item(key = "compare-input") {
        CompareInput(
            initial = state.compareHandle.orEmpty(),
            onCommit = onSetCompareHandle,
        )
    }
    val compareHandle = state.compareHandle
    if (!compareHandle.isNullOrBlank() && !state.handle.isNullOrBlank()) {
        item(key = "compare-card") {
            val context = LocalContext.current
            Column {
                ComparisonCard(
                    myHandle = state.handle,
                    myRating = state.rating,
                    myTier = state.currentTier,
                    theirHandle = compareHandle,
                    theirRating = state.compareRating,
                    theirTier = state.compareTier,
                )
                Spacer(Modifier.height(Spacing.sm))
                val subtitle = when {
                    state.compareError != null -> state.compareError
                    state.compareRating == null && state.compareTier == null -> "loading $compareHandle…"
                    else -> "open $compareHandle on codeforces →"
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (state.compareError != null)
                        MaterialTheme.appExtras.deltaNegative
                    else
                        MaterialTheme.appExtras.accentVioletSoft,
                    modifier = Modifier.clickable {
                        runCatching {
                            context.startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://codeforces.com/profile/$compareHandle"),
                                ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) },
                            )
                        }
                    },
                )
            }
        }
    } else if (compareHandle.isNullOrBlank()) {
        item(key = "compare-empty") {
            Text(
                text = "enter a codeforces handle above to compare ratings, tiers, and activity side-by-side.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.appExtras.textTertiary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Spacing.md),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun CompareInput(
    initial: String,
    onCommit: (String) -> Unit,
) {
    // Local state so typing stays responsive without waiting for DataStore
    // round-trips. Debounce writes so each keystroke doesn't trigger a fetch.
    var local by androidx.compose.runtime.saveable.rememberSaveable(initial.ifBlank { "\u0000" }) {
        androidx.compose.runtime.mutableStateOf(initial)
    }
    // If the source-of-truth changes externally (e.g. "delete all data"), sync local.
    androidx.compose.runtime.LaunchedEffect(initial) {
        if (initial != local) local = initial
    }
    // Debounce the commit.
    androidx.compose.runtime.LaunchedEffect(local) {
        kotlinx.coroutines.delay(350L)
        if (local != initial) onCommit(local)
    }
    androidx.compose.material3.OutlinedTextField(
        value = local,
        onValueChange = { local = it.filter { c -> !c.isWhitespace() } },
        placeholder = { Text("codeforces handle") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        trailingIcon = {
            if (local.isNotEmpty()) {
                androidx.compose.material3.IconButton(onClick = {
                    local = ""
                    onCommit("")
                }) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "clear",
                    )
                }
            }
        },
    )
}

@Composable
private fun SmallSectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        letterSpacing = 1.sp,
        color = MaterialTheme.appExtras.textTertiary,
    )
}

@Composable
private fun OutlinedSurfaceCard(content: @Composable () -> Unit) {
    val extras = MaterialTheme.appExtras
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(extras.surfaceElevated, AppShapes.medium)
            .border(0.5.dp, extras.borderSubtle, AppShapes.medium)
            .padding(Spacing.md),
    ) {
        content()
    }
}

@Composable
private fun ActivityStat(
    label: String,
    value: String,
    delta: String,
    modifier: Modifier = Modifier,
) {
    val extras = MaterialTheme.appExtras
    Column(
        modifier = modifier
            .background(extras.surfaceElevated, AppShapes.medium)
            .border(0.5.dp, extras.borderSubtle, AppShapes.medium)
            .padding(Spacing.md),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            letterSpacing = 1.sp,
            color = extras.textTertiary,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = delta,
            style = MaterialTheme.typography.labelSmall,
            color = extras.textTertiary,
        )
    }
}

@Composable
private fun ProfileHero(
    handle: String?,
    currentTier: Tier?,
    avatarUrl: String?,
    maxRating: Int?,
    problemsSolved: Int?,
    coveragePct: Int?,
) {
    val extras = MaterialTheme.appExtras
    val heroDescription = buildString {
        append(handle ?: "Unknown handle")
        if (currentTier != null) {
            append(", ")
            append(currentTier.label.lowercase())
        }
        if (maxRating != null) {
            append(", max rating ")
            append(maxRating)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(extras.surfaceElevated)
            .semantics(mergeDescendants = true) { contentDescription = heroDescription }
            .padding(Spacing.lg),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            HandleAvatar(
                handle = handle,
                avatarUrl = avatarUrl,
                tier = currentTier,
                size = 48.dp,
            )
            Spacer(Modifier.padding(start = Spacing.md))
            Column {
                if (handle != null && currentTier != null) {
                    HandleText(
                        handle = handle,
                        tier = currentTier,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                } else {
                    Text(
                        text = handle ?: "—",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Text(
                    text = (currentTier?.label ?: "unranked").lowercase(),
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.5.sp),
                    color = extras.textTertiary,
                )
            }
        }

        Spacer(Modifier.height(Spacing.md))

        val tierColor = currentTier?.palette()?.strong ?: MaterialTheme.colorScheme.primary
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            verticalAlignment = Alignment.Top,
        ) {
            HeroStat(
                modifier = Modifier.weight(1f),
                value = maxRating?.toString() ?: "—",
                label = "MAX RATING",
                valueColor = tierColor,
            )
            HeroStat(
                modifier = Modifier.weight(1f),
                value = problemsSolved?.toString() ?: "—",
                label = "PROBLEMS",
                valueColor = MaterialTheme.colorScheme.onSurface,
            )
            HeroStat(
                modifier = Modifier.weight(1f),
                value = coveragePct?.let { "$it%" } ?: "—",
                label = "COVERAGE",
                valueColor = extras.accentVioletSoft,
            )
        }

        Spacer(Modifier.height(Spacing.md))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(extras.borderSubtle),
        )
    }
}

@Composable
private fun HeroStat(
    value: String,
    label: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
) {
    val extras = MaterialTheme.appExtras
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-1.44).sp, // -0.06em @ 24sp
            ),
            color = valueColor,
            textAlign = TextAlign.Center,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.5.sp),
            color = extras.textTertiary,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun LadderCaption(maxRating: Int) {
    val extras = MaterialTheme.appExtras
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.sm),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "all tiers climbed · peak $maxRating",
            style = MaterialTheme.typography.labelSmall,
            color = extras.textTertiary,
        )
    }
}

@Composable
private fun CenterHint(text: String) {
    val extras = MaterialTheme.appExtras
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.xl),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = extras.textTertiary,
        )
    }
}

@Composable
private fun WeakTagCard(stat: WeakTagStat, currentTier: Tier?) {
    val extras = MaterialTheme.appExtras
    val coverage = stat.coverage.coerceIn(0f, 1f)
    val pct = (coverage * 100).toInt()
    val shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(extras.surfaceElevated, shape)
            .border(0.5.dp, extras.borderSubtle, shape)
            .padding(Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TagChip(tag = stat.tag, weak = true)
            Spacer(Modifier.weight(1f))
            Text(
                text = "$pct% covered",
                style = MaterialTheme.typography.labelSmall,
                color = extras.textSecondary,
            )
        }
        AnimatedProgressBar(
            progress = coverage,
            accentBrush = currentTier?.gradient(),
            modifier = Modifier.height(4.dp),
        )
        Text(
            text = "${100 - pct}% gap",
            style = MaterialTheme.typography.labelSmall,
            color = extras.textTertiary,
        )
    }
}

@Preview(name = "Profile - Loading")
@Composable
private fun ProfileScreenLoadingPreview() {
    ProblemBuddyTheme {
        ProfileScreen(state = ProfileState(loading = true), onIntent = {})
    }
}

private fun sampleActivity(): ActivityStats {
    val now = System.currentTimeMillis() / 1000L
    return ActivityStats.Empty.copy(
        solvedByDayEpoch = mapOf(20000L to 2, 20001L to 1, 20002L to 3),
        currentStreakDays = 5,
        longestStreakDays = 21,
        solvedThisYear = 120,
        ratingHistory = listOf(
            RatingPoint(now - 86400L * 60, 1450),
            RatingPoint(now - 86400L * 30, 1510),
            RatingPoint(now, 1540),
        ),
        contestHistory = listOf(
            ContestResult(1800, "Codeforces Round 900 (Div. 2)", 120, 1450, 1510, now - 86400L * 30, "Div 2"),
            ContestResult(1810, "Educational Codeforces Round 155", 340, 1510, 1499, now - 86400L * 20, "Edu"),
            ContestResult(1820, "Codeforces Round 910 (Div. 2)", 85, 1499, 1540, now - 86400L * 5, "Div 2"),
        ),
        tagRadar = listOf(
            TagScore("dp", 8),
            TagScore("graphs", 5),
            TagScore("math", 3),
        ),
        milestones = listOf(
            Milestone(now - 86400L * 200, "First accepted solution", "4A"),
            Milestone(now - 86400L * 30, "100th accepted solution", "Reached 100 solves"),
        ),
        verdictBreakdown = mapOf("OK" to 42, "WRONG_ANSWER" to 18, "TIME_LIMIT_EXCEEDED" to 5),
        languageCounts = mapOf("GNU C++17" to 80, "Python 3" to 12),
        dayOfWeekCounts = intArrayOf(4, 6, 8, 10, 7, 3, 2),
        hourOfDayCounts = IntArray(24) { if (it in 18..23) it - 10 else 1 },
        ratedParticipations = 3,
        virtualParticipations = 1,
    )
}

@Preview(name = "Profile - Legendary")
@Composable
private fun ProfileScreenLegendaryPreview() {
    ProblemBuddyTheme {
        ProfileScreen(
            state = ProfileState(
                loading = false,
                handle = "tourist",
                rating = 3847,
                maxRating = 3847,
                currentTier = Tier.LEGENDARY,
                problemsSolved = 1247,
                coveragePct = 92,
                weakTags = listOf(
                    WeakTagStat("data structures", 0.62f),
                    WeakTagStat("graphs", 0.48f),
                    WeakTagStat("math", 0.71f),
                ),
                activity = sampleActivity(),
            ),
            onIntent = {},
        )
    }
}

@Preview(name = "Profile - Specialist")
@Composable
private fun ProfileScreenSpecialistPreview() {
    ProblemBuddyTheme {
        ProfileScreen(
            state = ProfileState(
                loading = false,
                handle = "rakibjoy",
                rating = 1500,
                maxRating = 1540,
                currentTier = Tier.SPECIALIST,
                problemsSolved = 312,
                coveragePct = 41,
                weakTags = listOf(
                    WeakTagStat("dp", 0.22f),
                    WeakTagStat("greedy", 0.35f),
                ),
                activity = sampleActivity(),
            ),
            onIntent = {},
        )
    }
}
