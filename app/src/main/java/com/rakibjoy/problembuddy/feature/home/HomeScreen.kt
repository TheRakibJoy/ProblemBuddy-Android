package com.rakibjoy.problembuddy.feature.home

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rakibjoy.problembuddy.core.ui.components.AppTopBar
import com.rakibjoy.problembuddy.core.ui.components.DailyProblemCard
import com.rakibjoy.problembuddy.core.ui.components.GradientSurface
import com.rakibjoy.problembuddy.core.ui.components.HandleAvatar
import com.rakibjoy.problembuddy.core.ui.components.HandleText
import com.rakibjoy.problembuddy.core.ui.components.NextTierProgress
import com.rakibjoy.problembuddy.core.ui.components.RatingRail
import com.rakibjoy.problembuddy.core.ui.components.SparklineCard
import com.rakibjoy.problembuddy.core.ui.components.StatCard
import com.rakibjoy.problembuddy.core.ui.components.StreakRiskBanner
import com.rakibjoy.problembuddy.core.ui.components.TagChip
import com.rakibjoy.problembuddy.core.ui.components.UpcomingContestCard
import com.rakibjoy.problembuddy.core.ui.components.UpsolveBadge
import com.rakibjoy.problembuddy.core.ui.components.UpsolveCard
import com.rakibjoy.problembuddy.core.ui.theme.ProblemBuddyTheme
import com.rakibjoy.problembuddy.core.ui.theme.appExtras
import com.rakibjoy.problembuddy.domain.model.Problem
import com.rakibjoy.problembuddy.domain.model.ThemeMode
import com.rakibjoy.problembuddy.domain.model.Tier
import java.util.Calendar

@Composable
fun HomeScreen(
    onNavigateToRecommend: () -> Unit,
    onNavigateToTrain: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                HomeEffect.NavigateToRecommend -> onNavigateToRecommend()
                HomeEffect.NavigateToTrain -> onNavigateToTrain()
                HomeEffect.NavigateToProfile -> onNavigateToProfile()
                HomeEffect.NavigateToSettings -> onNavigateToSettings()
                is HomeEffect.ShowToast -> Toast.makeText(
                    context,
                    effect.message,
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }
    }
    HomeScreen(state = state, onIntent = viewModel::onIntent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: HomeState,
    onIntent: (HomeIntent) -> Unit,
) {
    GradientSurface {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                AppTopBar(
                    actions = {
                        IconButton(onClick = { onIntent(HomeIntent.SettingsClicked) }) {
                            Icon(
                                imageVector = Icons.Outlined.Settings,
                                contentDescription = "Settings",
                                tint = MaterialTheme.appExtras.textTertiary,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    },
                )
            },
        ) { padding ->
            val context = LocalContext.current
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                state.upcomingContest?.let { contest ->
                    val nowSeconds = System.currentTimeMillis() / 1000L
                    if (contest.startTimeSeconds - nowSeconds > 0L) {
                        item(key = "upcoming-contest") {
                            UpcomingContestCard(
                                contest = contest,
                                onRegister = {
                                    val uri = Uri.parse(
                                        "https://codeforces.com/contestRegistration/${contest.id}",
                                    )
                                    runCatching {
                                        context.startActivity(
                                            Intent(Intent.ACTION_VIEW, uri).apply {
                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            },
                                        )
                                    }
                                },
                            )
                        }
                    }
                }

                item(key = "handle") {
                    HandleRow(
                        handle = state.handle,
                        tier = Tier.forMaxRating(state.rating ?: 0),
                        avatarUrl = state.avatarUrl,
                    )
                }

                if (state.streakDays > 0 && !state.todayHasAc) {
                    item(key = "streak-risk") {
                        val now = java.time.LocalDateTime.now()
                        val midnight = now.toLocalDate().plusDays(1).atStartOfDay()
                        val totalMinutes = java.time.Duration.between(now, midnight).toMinutes()
                        val hours = (totalMinutes / 60L).toInt().coerceAtLeast(0)
                        val minutes = (totalMinutes % 60L).toInt().coerceAtLeast(0)
                        StreakRiskBanner(
                            streakDays = state.streakDays,
                            hoursUntilMidnight = hours,
                            minutesUntilMidnight = minutes,
                        )
                    }
                }

                state.todayProblem?.let { p ->
                    item(key = "today-problem") {
                        DailyProblemCard(
                            problem = p,
                            onSolve = {
                                runCatching {
                                    context.startActivity(
                                        Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse(
                                                "https://codeforces.com/problemset/problem/${p.contestId}/${p.problemIndex}",
                                            ),
                                        ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) },
                                    )
                                }
                            },
                        )
                    }
                }

                item(key = "stats") {
                    StatsRow(
                        rating = state.rating,
                        ratingDelta = state.ratingDelta,
                        solved = state.problemsSolved,
                        streak = state.streakDays,
                    )
                }

                if (state.rating != null) {
                    item(key = "next-tier") {
                        NextTierProgress(
                            currentTier = Tier.forMaxRating(state.rating),
                            nextTier = state.nextTier,
                            ratingToGo = state.ratingToNextTier,
                            progress = state.nextTierProgress,
                        )
                    }
                }

                item(key = "weekly-goal") {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        SectionHeaderRow(
                            title = "WEEKLY GOAL",
                            actionLabel = "edit in settings",
                            onActionClick = { onIntent(HomeIntent.SettingsClicked) },
                        )
                        // TODO: swap to shared GoalProgressCard once Agent Beta's
                        // component is available in core.ui.components.
                        GoalProgressCardLocal(
                            label = "problems this week",
                            current = state.weeklySolved,
                            target = state.weeklyGoal,
                        )
                    }
                }

                state.weakTagTrend?.let { trend ->
                    item(key = "weak-trend") {
                        WeakTagTrendSection(
                            trend = trend,
                            onAllTags = { onIntent(HomeIntent.ProfileClicked) },
                        )
                    }
                }

                if (state.todayPicks.isNotEmpty()) {
                    item(key = "picks-header") {
                        SectionHeaderRow(
                            title = "TODAY'S PICKS",
                            actionLabel = "see all →",
                            onActionClick = { onIntent(HomeIntent.RecommendClicked) },
                        )
                    }
                    items@ for (pick in state.todayPicks.take(2)) {
                        item(key = "pick-${pick.problem.contestId}${pick.problem.problemIndex}") {
                            ProblemCard(pick = pick)
                        }
                    }
                }

                if (state.upsolve.isNotEmpty()) {
                    item(key = "upsolve-header") {
                        SectionHeaderRow(
                            title = "UPSOLVE QUEUE",
                            actionLabel = "from last contest",
                            onActionClick = { onIntent(HomeIntent.RecommendClicked) },
                        )
                    }
                    for ((idx, up) in state.upsolve.take(3).withIndex()) {
                        item(key = "upsolve-$idx-${up.name}") {
                            UpsolveCard(name = up.name, meta = up.meta, badge = up.badge)
                        }
                    }
                }

                item(key = "cta") {
                    PrimaryCta(onClick = { onIntent(HomeIntent.RecommendClicked) })
                }

                item(key = "bottom-pad") { Spacer(Modifier.height(8.dp)) }
            }
        }
    }
}

// ──────────────── Handle row ────────────────

@Composable
private fun HandleRow(handle: String?, tier: Tier, avatarUrl: String?) {
    val extras = MaterialTheme.appExtras
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        HandleAvatar(handle = handle, avatarUrl = avatarUrl, tier = tier, size = 38.dp)
        Column {
            Text(
                text = greetingForHour(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)),
                style = MaterialTheme.typography.labelSmall,
                color = extras.textSecondary,
            )
            if (handle != null) {
                HandleText(
                    handle = handle,
                    tier = tier,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            } else {
                Text(
                    text = "friend",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

private fun greetingForHour(hour: Int): String = when (hour) {
    in 5..11 -> "good morning"
    in 12..16 -> "good afternoon"
    else -> "good evening"
}

// ──────────────── Stats row ────────────────

@Composable
private fun StatsRow(rating: Int?, ratingDelta: Int?, solved: Int?, streak: Int?) {
    val extras = MaterialTheme.appExtras
    // IntrinsicSize.Max so all three cards adopt the height of the tallest —
    // keeps the grid visually even even when only some cards have delta rows.
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val deltaText = ratingDelta
            ?.takeIf { it != 0 }
            ?.let { if (it > 0) "+$it" else "$it" }
        StatCard(
            value = rating?.toString() ?: "—",
            label = "RATING",
            accent = extras.accentVioletSoft,
            deltaText = deltaText,
            deltaIsPositive = (ratingDelta ?: 0) >= 0,
            modifier = Modifier.weight(1f).fillMaxHeight(),
        )
        StatCard(
            value = solved?.toString() ?: "—",
            label = "SOLVED",
            accent = extras.accentCyanSoft,
            modifier = Modifier.weight(1f).fillMaxHeight(),
        )
        StatCard(
            value = streak?.toString() ?: "—",
            label = "STREAK",
            accent = Color(0xFFF59E0B),
            deltaText = if (streak != null) "days" else null,
            deltaIsPositive = true,
            modifier = Modifier.weight(1f).fillMaxHeight(),
        )
    }
}

// ──────────────── Section header ────────────────

@Composable
private fun SectionHeaderRow(
    title: String,
    actionLabel: String,
    onActionClick: () -> Unit,
) {
    val extras = MaterialTheme.appExtras
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.0.sp),
            color = extras.textTertiary,
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = actionLabel,
            style = MaterialTheme.typography.labelSmall,
            color = extras.accentVioletSoft,
            modifier = Modifier.clickable(onClick = onActionClick),
        )
    }
}

// ──────────────── Weak tag trend ────────────────

@Composable
private fun WeakTagTrendSection(trend: WeakTagTrend, onAllTags: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        SectionHeaderRow(
            title = "WEAK TAG TREND",
            actionLabel = "all tags →",
            onActionClick = onAllTags,
        )
        SparklineCard(
            tag = trend.tag,
            trendLabel = trend.trendLabel,
            trendIsDecline = trend.declining,
            points = trend.points,
        )
    }
}

// ──────────────── Primary CTA ────────────────

@Composable
private fun PrimaryCta(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
        ),
        contentPadding = PaddingValues(vertical = 12.dp),
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Message,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = Color.White,
        )
        Spacer(Modifier.size(8.dp))
        Text(
            text = "get recommendations",
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.24.sp, // ~0.02em @ 12sp
            ),
        )
    }
}

// ──────────────── Problem card (local) ────────────────

@Composable
private fun ProblemCard(pick: TodayPick) {
    val extras = MaterialTheme.appExtras
    val shape = RoundedCornerShape(16.dp)
    val alpha = when {
        pick.isSolved -> 0.45f
        pick.isSkipped -> 0.30f
        else -> 1f
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(extras.surfaceElevated, shape)
            .border(0.5.dp, extras.borderSubtle, shape)
            .padding(12.dp)
            .alpha(alpha),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RatingRail(rating = pick.problem.rating ?: 0, tier = pick.tier)

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = pick.problem.name,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = (-0.12).sp, // -0.01em @ 12sp
                    textDecoration = if (pick.isSolved) TextDecoration.LineThrough else null,
                ),
                color = if (pick.isSolved) extras.textTertiary else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = problemMeta(pick.problem),
                style = MaterialTheme.typography.labelSmall,
                color = extras.textTertiary,
            )
            if (pick.problem.tags.isNotEmpty()) {
                Spacer(Modifier.height(5.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    pick.problem.tags.take(3).forEach { tag ->
                        TagChip(tag = tag, weak = tag in pick.weakTags)
                    }
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            when {
                pick.isSolved -> ActionIconButton(
                    icon = Icons.Filled.Check,
                    tint = Color(0xFF22C55E),
                    contentDescription = "Solved",
                )
                pick.isSkipped -> {
                    // Skipped variant shows nothing (or could show a muted chevron)
                }
                else -> {
                    ActionIconButton(
                        icon = Icons.AutoMirrored.Filled.ArrowForward,
                        tint = if (pick.isActive) extras.accentVioletSoft else extras.textTertiary,
                        contentDescription = "Solve",
                    )
                    ActionIconButton(
                        icon = Icons.Outlined.BookmarkBorder,
                        tint = extras.textTertiary,
                        contentDescription = "Bookmark",
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    contentDescription: String,
) {
    val extras = MaterialTheme.appExtras
    val shape = RoundedCornerShape(7.dp)
    Box(
        modifier = Modifier
            .size(26.dp)
            .clip(shape)
            .border(0.5.dp, extras.borderSubtle, shape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(13.dp),
        )
    }
}

private fun problemMeta(p: Problem): String {
    // e.g. "1854 · G" — Codeforces division isn't in the Problem model today;
    // plumbing it through would need contest.list lookups per render.
    val rating = p.rating?.toString() ?: "—"
    return "$rating · ${p.problemIndex}"
}

// ──────────────── Weekly goal (local fallback) ────────────────
// TODO: Replace with shared GoalProgressCard from core.ui.components
// once Agent Beta's component lands.
@Composable
private fun GoalProgressCardLocal(label: String, current: Int, target: Int) {
    val extras = MaterialTheme.appExtras
    val shape = RoundedCornerShape(16.dp)
    val safeTarget = target.coerceAtLeast(1)
    val progress = (current.toFloat() / safeTarget.toFloat()).coerceIn(0f, 1f)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(extras.surfaceElevated, shape)
            .border(0.5.dp, extras.borderSubtle, shape)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "$current / $target",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = extras.accentVioletSoft,
            )
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = extras.borderSubtle,
        )
    }
}

// ──────────────── Previews ────────────────

@Preview(name = "Home - no handle")
@Composable
private fun HomeScreenNoHandlePreview() {
    ProblemBuddyTheme(themeMode = ThemeMode.DARK) {
        HomeScreen(state = HomeState(), onIntent = {})
    }
}

@Preview(name = "Home - returning user (full)")
@Composable
private fun HomeScreenReturningPreview() {
    ProblemBuddyTheme(themeMode = ThemeMode.DARK) {
        HomeScreen(
            state = HomeState(
                handle = "rakibjoy",
                rating = 2150,
                ratingDelta = 42,
                problemsSolved = 1247,
                streakDays = 14,
                hasCorpus = true,
                weakTagTrend = WeakTagTrend(
                    tag = "data structures",
                    trendLabel = "↓ 38% solve rate",
                    declining = true,
                    points = listOf(28f, 24f, 30f, 20f, 26f, 14f, 22f, 10f),
                ),
                todayPicks = listOf(
                    TodayPick(
                        problem = Problem(1854, "G", "Segment Tree Beats", 2100, listOf("data structures", "segment tree")),
                        tier = Tier.EXPERT,
                        weakTags = setOf("data structures"),
                        isActive = true,
                    ),
                    TodayPick(
                        problem = Problem(1796, "E", "Painting Array", 1900, listOf("greedy", "constructive")),
                        tier = Tier.MASTER,
                        isSolved = true,
                    ),
                ),
                upsolve = listOf(
                    UpsolveProblem("Palindrome Graph", "CF 1900 · Div 1 D", UpsolveBadge.Unattempted),
                    UpsolveProblem("XOR Partition", "CF 2100 · Div 1 E", UpsolveBadge.Penalty),
                ),
            ),
            onIntent = {},
        )
    }
}

@Preview(name = "Home - legendary tourist")
@Composable
private fun HomeScreenLegendaryPreview() {
    ProblemBuddyTheme(themeMode = ThemeMode.DARK) {
        HomeScreen(
            state = HomeState(
                handle = "tourist",
                rating = 3847,
                ratingDelta = 42,
                problemsSolved = 1247,
                streakDays = 14,
                hasCorpus = true,
                weakTagTrend = WeakTagTrend(
                    tag = "data structures",
                    trendLabel = "↓ 38% solve rate",
                    declining = true,
                    points = listOf(28f, 24f, 30f, 20f, 26f, 14f, 22f, 10f),
                ),
                todayPicks = listOf(
                    TodayPick(
                        problem = Problem(1854, "G", "Segment Tree Beats", 2100, listOf("data structures", "segment tree")),
                        tier = Tier.EXPERT,
                        weakTags = setOf("data structures"),
                        isActive = true,
                    ),
                    TodayPick(
                        problem = Problem(1796, "E", "Painting Array", 1900, listOf("greedy", "constructive")),
                        tier = Tier.MASTER,
                        isSolved = true,
                    ),
                ),
                upsolve = listOf(
                    UpsolveProblem("Palindrome Graph", "CF 1900 · Div 1 D", UpsolveBadge.Unattempted),
                    UpsolveProblem("XOR Partition", "CF 2100 · Div 1 E", UpsolveBadge.Penalty),
                ),
            ),
            onIntent = {},
        )
    }
}
