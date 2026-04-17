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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rakibjoy.problembuddy.core.ui.components.AnimatedProgressBar
import com.rakibjoy.problembuddy.core.ui.components.AppTopBar
import com.rakibjoy.problembuddy.core.ui.components.EmptyCorpusCard
import com.rakibjoy.problembuddy.core.ui.components.EmptyStateIllustration
import com.rakibjoy.problembuddy.core.ui.components.GradientSurface
import com.rakibjoy.problembuddy.core.ui.components.HandleAvatar
import com.rakibjoy.problembuddy.core.ui.components.SectionHeader
import com.rakibjoy.problembuddy.core.ui.components.SkeletonCard
import com.rakibjoy.problembuddy.core.ui.components.SkeletonLine
import com.rakibjoy.problembuddy.core.ui.components.StaleDataBanner
import com.rakibjoy.problembuddy.core.ui.components.StatCard
import com.rakibjoy.problembuddy.core.ui.components.TagChip
import com.rakibjoy.problembuddy.core.ui.components.TierBadge
import com.rakibjoy.problembuddy.core.ui.theme.AppShapes
import com.rakibjoy.problembuddy.core.ui.theme.Elevations
import com.rakibjoy.problembuddy.core.ui.theme.ProblemBuddyTheme
import com.rakibjoy.problembuddy.core.ui.theme.Spacing
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    state: ProfileState,
    onIntent: (ProfileIntent) -> Unit,
    onNavigateToTrain: (() -> Unit)? = null,
) {
    GradientSurface {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = { AppTopBar(title = "Profile") },
        ) { padding ->
            when {
                state.loading -> LoadingContent(padding)
                state.error != null -> ErrorContent(
                    padding = padding,
                    message = state.error,
                    onRetry = { onIntent(ProfileIntent.Refresh) },
                )
                else -> ProfileContent(
                    padding = padding,
                    state = state,
                    onNavigateToTrain = onNavigateToTrain,
                )
            }
        }
    }
}

@Composable
private fun LoadingContent(padding: PaddingValues) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentPadding = PaddingValues(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.xl),
    ) {
        item { SkeletonCard(modifier = Modifier.height(140.dp)) }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                SkeletonLine(width = 160.dp, height = 22.dp)
                SkeletonLine(width = 220.dp, height = 14.dp)
                SkeletonLine(width = 180.dp, height = 14.dp)
                SkeletonLine(width = 200.dp, height = 14.dp)
                SkeletonLine(width = 140.dp, height = 14.dp)
            }
        }
    }
}

@Composable
private fun ErrorContent(
    padding: PaddingValues,
    message: String,
    onRetry: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
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
    padding: PaddingValues,
    state: ProfileState,
    onNavigateToTrain: (() -> Unit)? = null,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentPadding = PaddingValues(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.xl),
    ) {
        if (state.stale) {
            item { StaleDataBanner(fetchedAtMillis = state.fetchedAtMillis) }
        }
        item {
            HeroCard(
                handle = state.handle,
                rating = state.rating,
                maxRating = state.maxRating,
                currentTier = state.currentTier,
                avatarUrl = state.avatarUrl,
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                StatCard(
                    label = "RATING",
                    value = state.rating?.toString() ?: "—",
                    accent = state.currentTier?.palette()?.strong
                        ?: MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    label = "MAX",
                    value = state.maxRating?.toString() ?: "—",
                    accent = state.currentTier?.palette()?.strong
                        ?: MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                SectionHeader(title = "Tier ladder")
                TierLadder(currentTier = state.currentTier)
            }
        }
        item {
            SectionHeader(
                title = "Weak tags",
                actionLabel = "How it works?",
                onAction = { /* placeholder */ },
            )
        }
        if (state.weakTags.isEmpty()) {
            item {
                EmptyCorpusCard(onTrainClicked = { onNavigateToTrain?.invoke() })
            }
        } else {
            itemsIndexed(state.weakTags, key = { _, item -> item.tag }) { _, stat ->
                WeakTagRow(stat = stat, currentTier = state.currentTier)
            }
        }
    }
}

@Composable
private fun HeroCard(
    handle: String?,
    rating: Int?,
    maxRating: Int?,
    currentTier: Tier?,
    avatarUrl: String?,
) {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val brush: Brush = currentTier?.gradient()
        ?: Brush.verticalGradient(listOf(primary, secondary))
    val onColor = currentTier?.palette()?.onColor ?: Color.White
    val heroDescription = buildString {
        append(handle ?: "Unknown handle")
        if (currentTier != null) {
            append(", ")
            append(currentTier.label)
        }
        if (rating != null) {
            append(", rating ")
            append(rating)
        }
        if (maxRating != null && maxRating != rating) {
            append(", max ")
            append(maxRating)
        }
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) { contentDescription = heroDescription },
        shape = AppShapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = Elevations.hover),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush)
                .padding(Spacing.xl),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HandleAvatar(
                handle = handle,
                avatarUrl = avatarUrl,
                tier = currentTier,
                size = 80.dp,
            )
            Spacer(Modifier.width(Spacing.lg))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs),
            ) {
                Text(
                    text = handle ?: "—",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = onColor,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (currentTier != null) {
                        TierBadge(tier = currentTier, compact = false)
                        Spacer(Modifier.width(Spacing.sm))
                    }
                    Text(
                        text = rating?.toString() ?: "—",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = onColor,
                    )
                }
                if (maxRating != null && maxRating != rating) {
                    Text(
                        text = "Max $maxRating",
                        style = MaterialTheme.typography.bodySmall,
                        color = onColor.copy(alpha = 0.8f),
                    )
                }
            }
        }
    }
}

@Composable
private fun TierLadder(currentTier: Tier?) {
    val tiers = Tier.entries
    val currentIndex = currentTier?.let { tiers.indexOf(it) } ?: -1
    val listState = rememberLazyListState()

    LaunchedEffect(currentIndex) {
        if (currentIndex >= 0) {
            listState.animateScrollToItem(
                index = currentIndex.coerceAtLeast(0),
            )
        }
    }

    LazyRow(
        state = listState,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        itemsIndexed(tiers) { _, tier ->
            TierPill(tier = tier, currentTier = currentTier)
        }
    }
}

@Composable
private fun TierPill(tier: Tier, currentTier: Tier?) {
    val palette = tier.palette()
    val isCurrent = currentTier != null && tier == currentTier
    val isPast = currentTier != null && tier.floor < currentTier.floor
    val bg: Color = when {
        isCurrent -> palette.strong
        isPast -> palette.soft.copy(alpha = 0.35f)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val fg: Color = when {
        isCurrent -> palette.onColor
        isPast -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val scale = if (isCurrent) 1.12f else 1f
    val elevation = if (isCurrent) Elevations.hover else 0.dp
    val primary = MaterialTheme.colorScheme.primary
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        val cardModifier = Modifier
            .width(96.dp)
            .height(64.dp)
            .scale(scale)
            .then(
                if (isCurrent) {
                    Modifier.border(
                        width = 1.5.dp,
                        color = primary.copy(alpha = 0.5f),
                        shape = AppShapes.medium,
                    )
                } else {
                    Modifier
                },
            )
        Card(
            modifier = cardModifier,
            shape = AppShapes.medium,
            colors = CardDefaults.cardColors(containerColor = bg, contentColor = fg),
            elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Spacing.xs),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = tier.initials(),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = fg,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = "${tier.floor}+",
                    style = MaterialTheme.typography.bodySmall,
                    color = fg,
                    textAlign = TextAlign.Center,
                )
            }
        }
        if (isCurrent) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = null,
                tint = primary,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}

private fun Tier.initials(): String =
    label.split(' ').filter { it.isNotBlank() }.joinToString("") { it.first().uppercase() }

@Composable
private fun WeakTagRow(stat: WeakTagStat, currentTier: Tier?) {
    val coverage = stat.coverage.coerceIn(0f, 1f)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = Elevations.card),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TagChip(tag = stat.tag)
                Spacer(Modifier.weight(1f))
                Text(
                    text = "${(coverage * 100).toInt()}% covered",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            AnimatedProgressBar(
                progress = coverage,
                accentBrush = currentTier?.gradient(),
            )
            Text(
                text = "${((1f - coverage) * 100).toInt()}% gap",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(name = "Profile - Loading")
@Composable
private fun ProfileScreenLoadingPreview() {
    ProblemBuddyTheme {
        ProfileScreen(state = ProfileState(loading = true), onIntent = {})
    }
}

@Preview(name = "Profile - Success")
@Composable
private fun ProfileScreenDataPreview() {
    ProblemBuddyTheme {
        ProfileScreen(
            state = ProfileState(
                loading = false,
                handle = "tourist",
                rating = 3800,
                maxRating = 3979,
                currentTier = Tier.LEGENDARY,
                weakTags = listOf(
                    WeakTagStat("dp", 0.15f),
                    WeakTagStat("graphs", 0.32f),
                    WeakTagStat("math", 0.48f),
                ),
            ),
            onIntent = {},
        )
    }
}

@Preview(name = "Profile - No handle")
@Composable
private fun ProfileScreenNoHandlePreview() {
    ProblemBuddyTheme {
        ProfileScreen(
            state = ProfileState(loading = false, error = "No handle"),
            onIntent = {},
        )
    }
}
