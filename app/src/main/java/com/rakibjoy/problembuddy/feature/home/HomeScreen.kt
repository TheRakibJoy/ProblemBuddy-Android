package com.rakibjoy.problembuddy.feature.home

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import com.rakibjoy.problembuddy.core.ui.components.HandleAvatar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import com.rakibjoy.problembuddy.core.ui.components.GradientSurface
import com.rakibjoy.problembuddy.core.ui.components.SectionHeader
import com.rakibjoy.problembuddy.core.ui.components.StatCard
import com.rakibjoy.problembuddy.core.ui.components.pressScale
import com.rakibjoy.problembuddy.core.ui.theme.AppShapes
import com.rakibjoy.problembuddy.core.ui.theme.Elevations
import com.rakibjoy.problembuddy.core.ui.theme.ProblemBuddyTheme
import com.rakibjoy.problembuddy.core.ui.theme.Spacing
import com.rakibjoy.problembuddy.core.ui.theme.gradient
import com.rakibjoy.problembuddy.core.ui.theme.palette
import com.rakibjoy.problembuddy.domain.model.ThemeMode
import com.rakibjoy.problembuddy.domain.model.Tier
import com.rakibjoy.problembuddy.domain.model.TrainingJob
import kotlinx.coroutines.delay

private val TRAINING_TIPS = listOf(
    "Tip: Run training weekly to keep your corpus fresh.",
    "Tip: Solve one problem above your rating every day.",
    "Tip: Revisit failed attempts before moving on.",
    "Tip: Weak-tag drills beat random grinding.",
)

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
    val tier = Tier.forMaxRating(state.rating ?: 0)
    val tip = remember(state.handle) {
        TRAINING_TIPS[(state.handle?.hashCode()?.rem(TRAINING_TIPS.size)?.let {
            if (it < 0) it + TRAINING_TIPS.size else it
        } ?: 0)]
    }

    GradientSurface {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = { AppTopBar(title = "") },
        ) { padding ->
            val visibility = remember { mutableStateMapOf<Int, Boolean>() }

            val sections = buildSections(state)
            LaunchedEffect(sections.size) {
                sections.indices.forEach { idx ->
                    if (visibility[idx] != true) {
                        delay(60L)
                        visibility[idx] = true
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.lg),
            ) {
                sections.forEachIndexed { index, section ->
                    item(key = section.key) {
                        AnimatedVisibility(
                            visible = visibility[index] == true,
                            enter = fadeIn(tween(280)) +
                                slideInVertically(tween(280)) { it / 8 },
                        ) {
                            when (section) {
                                Section.Greeting -> GreetingHeader(
                                    handle = state.handle,
                                    rating = state.rating,
                                    tier = tier,
                                    avatarUrl = state.avatarUrl,
                                )
                                Section.Stats -> StatsRow(
                                    rating = state.rating ?: 0,
                                    maxRating = state.maxRating ?: state.rating ?: 0,
                                    tier = tier,
                                )
                                Section.PrimaryCta -> PrimaryCtaCard(
                                    enabled = state.handle != null,
                                    tier = tier,
                                    onClick = { onIntent(HomeIntent.RecommendClicked) },
                                )
                                Section.EmptyCorpus -> EmptyCorpusCard(
                                    onTrainClicked = { onIntent(HomeIntent.TrainClicked) },
                                )
                                Section.Training -> state.latestJob?.let { job ->
                                    Column {
                                        SectionHeader(title = "Last training")
                                        TrainingStatusCard(job = job)
                                    }
                                }
                                Section.QuickActions -> QuickActionsRow(
                                    onTrain = { onIntent(HomeIntent.TrainClicked) },
                                    onProfile = { onIntent(HomeIntent.ProfileClicked) },
                                    onSettings = { onIntent(HomeIntent.SettingsClicked) },
                                )
                                Section.Footer -> FooterTip(tip)
                            }
                        }
                    }
                }
            }
        }
    }
}

private enum class Section(val key: String) {
    Greeting("greeting"),
    Stats("stats"),
    PrimaryCta("cta"),
    EmptyCorpus("empty-corpus"),
    Training("training"),
    QuickActions("quick-actions"),
    Footer("footer"),
}

private fun buildSections(state: HomeState): List<Section> = buildList {
    add(Section.Greeting)
    if (state.rating != null) add(Section.Stats)
    add(Section.PrimaryCta)
    if (state.handle != null && !state.hasCorpus) add(Section.EmptyCorpus)
    if (state.latestJob != null) add(Section.Training)
    add(Section.QuickActions)
    add(Section.Footer)
}

@Composable
private fun GreetingHeader(handle: String?, rating: Int?, tier: Tier, avatarUrl: String?) {
    val greetingDescription = buildString {
        append("Hello, ")
        append(handle ?: "friend")
        if (rating != null) {
            append(". ")
            append(tier.label)
            append(", rating ")
            append(rating)
        }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) { contentDescription = greetingDescription },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Hello,",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = handle ?: "friend",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Bold,
                ),
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
        val palette = tier.palette()
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            HandleAvatar(
                handle = handle,
                avatarUrl = avatarUrl,
                tier = tier,
                size = 64.dp,
            )
            if (rating != null) {
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    text = tier.abbreviation(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = palette.strong,
                )
            }
        }
    }
}

private fun Tier.abbreviation(): String =
    label.split(' ').filter { it.isNotBlank() }.joinToString("") { it.first().uppercase() }

@Composable
private fun StatsRow(rating: Int, maxRating: Int, tier: Tier) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        StatCard(
            label = "RATING",
            value = "$rating",
            accent = tier.palette().strong,
            modifier = Modifier.weight(1f),
        )
        StatCard(
            label = "MAX",
            value = "$maxRating",
            accent = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun PrimaryCtaCard(
    enabled: Boolean,
    tier: Tier,
    onClick: () -> Unit,
) {
    val palette = tier.palette()
    val alpha = if (enabled) 1f else 0.45f
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 96.dp)
            .pressScale()
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier),
        shape = AppShapes.large,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = Elevations.hover),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(tier.gradient())
                .padding(Spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Get problems",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    color = palette.onColor.copy(alpha = alpha),
                )
                Text(
                    text = "Tailored to your weak tags",
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.onColor.copy(alpha = 0.85f * alpha),
                )
            }
            // decorative
            Icon(
                imageVector = Icons.AutoMirrored.Default.ArrowForward,
                contentDescription = null,
                tint = palette.onColor.copy(alpha = alpha),
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

@Composable
private fun TrainingStatusCard(job: TrainingJob) {
    val tier = job.currentTier
    val accent = tier?.palette()?.strong ?: MaterialTheme.colorScheme.primary
    val brush = tier?.gradient()
    val progress = if (job.total > 0) job.done.toFloat() / job.total else 0f
    val statusLabel = when (job.status) {
        TrainingJob.Status.QUEUED -> "Queued"
        TrainingJob.Status.RUNNING -> "Running"
        TrainingJob.Status.SUCCESS -> "Success"
        TrainingJob.Status.FAILED -> "Failed"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = Elevations.hover),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
        ) {
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                ) {
                    StatusChip(label = statusLabel, accent = accent)
                    if (job.status == TrainingJob.Status.RUNNING) {
                        PulsingDot(color = accent)
                    }
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = "${job.done}/${job.total}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                AnimatedProgressBar(
                    progress = progress,
                    accentBrush = brush,
                )
            }
        }
    }
}

@Composable
private fun StatusChip(label: String, accent: Color) {
    Box(
        modifier = Modifier
            .clip(AppShapes.small)
            .background(accent.copy(alpha = 0.18f))
            .padding(horizontal = Spacing.md, vertical = Spacing.xs),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
            ),
            color = accent,
        )
    }
}

@Composable
private fun PulsingDot(color: Color) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val alpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse-alpha",
    )
    Box(
        modifier = Modifier
            .size(10.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = alpha)),
    )
}

@Composable
private fun QuickActionsRow(
    onTrain: () -> Unit,
    onProfile: () -> Unit,
    onSettings: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        QuickActionCard(
            label = "Train",
            icon = Icons.Default.Refresh,
            onClick = onTrain,
            modifier = Modifier.weight(1f),
        )
        QuickActionCard(
            label = "Profile",
            icon = Icons.Default.Person,
            onClick = onProfile,
            modifier = Modifier.weight(1f),
        )
        QuickActionCard(
            label = "Settings",
            icon = Icons.Default.Settings,
            onClick = onSettings,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun QuickActionCard(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedCard(
        modifier = modifier
            .height(96.dp)
            .pressScale()
            .clickable(onClick = onClick),
        shape = AppShapes.medium,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // decorative
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(Spacing.sm))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun FooterTip(tip: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = Elevations.card),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            // decorative
            Icon(
                imageVector = Icons.Default.Lightbulb,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.secondary,
            )
            Text(
                text = tip,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Start,
            )
        }
    }
}

@Preview(name = "Home - no handle (dark)")
@Composable
private fun HomeScreenNoHandlePreview() {
    ProblemBuddyTheme(themeMode = ThemeMode.DARK) {
        HomeScreen(
            state = HomeState(),
            onIntent = {},
        )
    }
}

@Preview(name = "Home - with handle + corpus (dark)")
@Composable
private fun HomeScreenWithHandlePreview() {
    ProblemBuddyTheme(themeMode = ThemeMode.DARK) {
        HomeScreen(
            state = HomeState(
                handle = "tourist",
                greeting = "Welcome back, tourist",
                rating = 3800,
                maxRating = 3979,
                hasCorpus = true,
            ),
            onIntent = {},
        )
    }
}

@Preview(name = "Home - training running (dark)")
@Composable
private fun HomeScreenTrainingPreview() {
    ProblemBuddyTheme(themeMode = ThemeMode.DARK) {
        HomeScreen(
            state = HomeState(
                handle = "tourist",
                greeting = "Welcome back, tourist",
                rating = 1800,
                maxRating = 2100,
                hasCorpus = true,
                latestJob = TrainingJob(
                    id = 1,
                    handle = "tourist",
                    status = TrainingJob.Status.RUNNING,
                    currentTier = Tier.EXPERT,
                    done = 65,
                    total = 120,
                    error = null,
                    updatedAt = 0,
                ),
            ),
            onIntent = {},
        )
    }
}
