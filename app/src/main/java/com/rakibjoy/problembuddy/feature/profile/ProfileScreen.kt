package com.rakibjoy.problembuddy.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rakibjoy.problembuddy.domain.model.Tier

@Composable
fun ProfileScreen(viewModel: ProfileViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ProfileScreen(state = state, onIntent = viewModel::onIntent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    state: ProfileState,
    onIntent: (ProfileIntent) -> Unit,
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Profile") }) },
    ) { padding ->
        when {
            state.loading -> LoadingContent(padding)
            state.error != null -> ErrorContent(
                padding = padding,
                message = state.error,
                onRetry = { onIntent(ProfileIntent.Refresh) },
            )
            else -> ProfileContent(padding = padding, state = state)
        }
    }
}

@Composable
private fun LoadingContent(padding: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
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
            .padding(padding)
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(message, style = MaterialTheme.typography.bodyLarge)
            Button(onClick = onRetry) { Text("Retry") }
        }
    }
}

@Composable
private fun ProfileContent(padding: PaddingValues, state: ProfileState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Header(
            handle = state.handle,
            rating = state.rating,
            maxRating = state.maxRating,
        )
        TierLadder(currentTier = state.currentTier)
        WeakTagsSection(weakTags = state.weakTags)
    }
}

@Composable
private fun Header(handle: String?, rating: Int?, maxRating: Int?) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = handle ?: "—",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
        )
        val ratingText = buildString {
            append("Rating: ")
            append(rating?.toString() ?: "—")
            if (maxRating != null) {
                append("  (max ")
                append(maxRating)
                append(")")
            }
        }
        Text(ratingText, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun TierLadder(currentTier: Tier?) {
    val tiers = Tier.entries
    val currentIndex = currentTier?.let { tiers.indexOf(it) } ?: -1
    val listState = rememberLazyListState()

    LaunchedEffect(currentIndex) {
        if (currentIndex >= 0) {
            listState.scrollToItem(currentIndex)
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "Tier ladder",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        LazyRow(
            state = listState,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(tiers) { tier ->
                TierPill(tier = tier, currentTier = currentTier)
            }
        }
    }
}

@Composable
private fun TierPill(tier: Tier, currentTier: Tier?) {
    val role: PillRole = when {
        currentTier == null -> PillRole.Muted
        tier.floor < currentTier.floor -> PillRole.Past
        tier.floor == currentTier.floor -> PillRole.Current
        else -> PillRole.Future
    }
    val colors = MaterialTheme.colorScheme
    val (bg, fg) = when (role) {
        PillRole.Past -> colors.surfaceVariant to colors.onSurfaceVariant
        PillRole.Current -> colors.primary to colors.onPrimary
        PillRole.Future -> Color.Transparent to colors.onSurface
        PillRole.Muted -> colors.surfaceVariant to colors.onSurfaceVariant
    }
    val shape = RoundedCornerShape(50)
    val base = Modifier
        .background(bg, shape)
    val modifier = if (role == PillRole.Future) {
        base.border(1.dp, colors.outline, shape)
    } else {
        base
    }
    Text(
        text = tier.label,
        color = fg,
        fontWeight = if (role == PillRole.Current) FontWeight.Bold else FontWeight.Normal,
        style = MaterialTheme.typography.labelLarge,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 12.dp),
    )
}

private enum class PillRole { Past, Current, Future, Muted }

@Composable
private fun WeakTagsSection(weakTags: List<WeakTagStat>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            "Weak tags",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        if (weakTags.isEmpty()) {
            Text(
                "Run training first to see weak tags.",
                style = MaterialTheme.typography.bodyMedium,
            )
        } else {
            weakTags.forEach { stat -> WeakTagRow(stat) }
        }
    }
}

@Composable
private fun WeakTagRow(stat: WeakTagStat) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row3(
            start = {
                Text(
                    stat.tag,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
            },
            end = {
                Text(
                    "${(stat.coverage * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                )
            },
        )
        LinearProgressIndicator(
            progress = { stat.coverage.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun Row3(start: @Composable () -> Unit, end: @Composable () -> Unit) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        start()
        end()
    }
}

@Preview
@Composable
private fun ProfileScreenLoadingPreview() {
    ProfileScreen(state = ProfileState(loading = true), onIntent = {})
}

@Preview
@Composable
private fun ProfileScreenDataPreview() {
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
                WeakTagStat("greedy", 0.60f),
            ),
        ),
        onIntent = {},
    )
}

@Preview
@Composable
private fun ProfileScreenNoHandlePreview() {
    ProfileScreen(
        state = ProfileState(loading = false, error = "No handle"),
        onIntent = {},
    )
}
