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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
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
import com.rakibjoy.problembuddy.core.ui.components.AppTopBar
import com.rakibjoy.problembuddy.core.ui.components.GradientSurface
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
    onNavigateToTrain: (() -> Unit)? = null,
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val tabs = remember { listOf("tier ladder", "weak tags", "activity") }

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
                2 -> activityTab()
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

private fun androidx.compose.foundation.lazy.LazyListScope.activityTab() {
    item(key = "activity-placeholder") {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            val extras = MaterialTheme.appExtras
            Text(
                text = "activity coming soon",
                style = MaterialTheme.typography.labelMedium,
                color = extras.textSecondary,
            )
            Text(
                text = "heatmap, streaks, and rating history will land here",
                style = MaterialTheme.typography.labelSmall,
                color = extras.textTertiary,
                textAlign = TextAlign.Center,
            )
        }
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
            ),
            onIntent = {},
        )
    }
}
