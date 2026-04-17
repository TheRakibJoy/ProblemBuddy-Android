package com.rakibjoy.problembuddy.feature.recommend

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rakibjoy.problembuddy.domain.model.Filters
import com.rakibjoy.problembuddy.domain.model.Problem
import com.rakibjoy.problembuddy.domain.model.Tier

@Composable
fun RecommendScreen(viewModel: RecommendViewModel = hiltViewModel()) {
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
    RecommendScreen(state = state, onIntent = viewModel::onIntent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendScreen(
    state: RecommendState,
    onIntent: (RecommendIntent) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recommendations") },
                actions = {
                    IconButton(onClick = { onIntent(RecommendIntent.OpenFilters) }) {
                        Icon(Icons.Filled.FilterList, contentDescription = "Filters")
                    }
                },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            when {
                state.loading -> SkeletonList()
                state.error != null -> ErrorView(
                    message = state.error,
                    onRetry = { onIntent(RecommendIntent.Refresh) },
                )
                state.problems.isEmpty() -> EmptyView()
                else -> ProblemList(
                    problems = state.problems,
                    onIntent = onIntent,
                )
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

@Composable
private fun SkeletonList() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(5) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .background(Color.Gray.copy(alpha = 0.2f)),
                )
            }
        }
    }
}

@Composable
private fun ErrorView(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(message, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(12.dp))
        Button(onClick = onRetry) { Text("Retry") }
    }
}

@Composable
private fun EmptyView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            "No recommendations. Try adjusting filters.",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(24.dp),
        )
    }
}

@Composable
private fun ProblemList(
    problems: List<Problem>,
    onIntent: (RecommendIntent) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(problems, key = { "${it.contestId}-${it.problemIndex}" }) { problem ->
            ProblemCard(
                problem = problem,
                onSolve = { onIntent(RecommendIntent.OpenUrl(problem)) },
                onMarkSolved = { onIntent(RecommendIntent.MarkSolved(problem)) },
                onSkip = { onIntent(RecommendIntent.Skip(problem)) },
            )
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
    val brush = remember(problem.tags) { gradientForTags(problem.tags) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush)
                .padding(16.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "${problem.contestId}${problem.problemIndex} \u2014 ${problem.name.ifBlank { problem.problemIndex }}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Rating: ${problem.rating ?: "-"}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = Tier.forMaxRating(problem.rating ?: 0).label,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                if (problem.tags.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        problem.tags.take(6).forEach { tag ->
                            SuggestionChip(
                                onClick = {},
                                label = { Text(tag, style = MaterialTheme.typography.labelSmall) },
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Button(onClick = onSolve) { Text("Solve") }
                    OutlinedButton(onClick = onMarkSolved) { Text("Mark Solved") }
                    TextButton(onClick = onSkip) { Text("Skip") }
                }
            }
        }
    }
}

private fun gradientForTags(tags: List<String>): Brush {
    val first = tags.firstOrNull()
    if (first == null) {
        return Brush.linearGradient(
            listOf(
                Color(0xFFECEFF1).copy(alpha = 0.8f),
                Color(0xFFCFD8DC).copy(alpha = 0.8f),
            ),
        )
    }
    val hue = ((first.hashCode() % 360) + 360) % 360
    val c1 = hslToColor(hue.toFloat(), 0.55f, 0.75f).copy(alpha = 0.55f)
    val c2 = hslToColor(((hue + 40) % 360).toFloat(), 0.55f, 0.65f).copy(alpha = 0.55f)
    return Brush.linearGradient(listOf(c1, c2))
}

private fun hslToColor(h: Float, s: Float, l: Float): Color {
    val c = (1f - kotlin.math.abs(2f * l - 1f)) * s
    val hp = h / 60f
    val x = c * (1f - kotlin.math.abs(hp % 2f - 1f))
    val (r1, g1, b1) = when {
        hp < 1f -> Triple(c, x, 0f)
        hp < 2f -> Triple(x, c, 0f)
        hp < 3f -> Triple(0f, c, x)
        hp < 4f -> Triple(0f, x, c)
        hp < 5f -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }
    val m = l - c / 2f
    return Color(
        red = (r1 + m).coerceIn(0f, 1f),
        green = (g1 + m).coerceIn(0f, 1f),
        blue = (b1 + m).coerceIn(0f, 1f),
    )
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Filters", style = MaterialTheme.typography.titleMedium)
            Text("Count: ${count.toInt()}", style = MaterialTheme.typography.bodyMedium)
            Slider(
                value = count,
                onValueChange = { count = it },
                valueRange = 1f..30f,
                steps = 28,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = minRating,
                    onValueChange = { v -> minRating = v.filter { it.isDigit() } },
                    label = { Text("Min rating") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = maxRating,
                    onValueChange = { v -> maxRating = v.filter { it.isDigit() } },
                    label = { Text("Max rating") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
            }
            OutlinedTextField(
                value = includeTags,
                onValueChange = { includeTags = it },
                label = { Text("Include tags (comma-separated)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            OutlinedTextField(
                value = excludeTags,
                onValueChange = { excludeTags = it },
                label = { Text("Exclude tags (comma-separated)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Weak tags only", modifier = Modifier.weight(1f))
                Switch(checked = weakOnly, onCheckedChange = { weakOnly = it })
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = onCancel) { Text("Cancel") }
                Spacer(Modifier.width(8.dp))
                Button(
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

private fun String.splitTags(): Set<String> =
    split(',').map { it.trim() }.filter { it.isNotEmpty() }.toSet()

@Suppress("unused")
private val UnusedClip = Modifier.clip(RoundedCornerShape(0.dp))

@Preview
@Composable
private fun RecommendScreenLoadingPreview() {
    RecommendScreen(state = RecommendState(loading = true), onIntent = {})
}

@Preview
@Composable
private fun RecommendScreenCardsPreview() {
    RecommendScreen(
        state = RecommendState(
            loading = false,
            problems = listOf(
                Problem(1520, "A", "Do Not Be Distracted", 800, listOf("implementation", "strings")),
                Problem(1530, "B", "Putting Plates", 1100, listOf("constructive algorithms", "greedy")),
                Problem(1540, "C", "Tree XOR", 1600, listOf("dp", "trees", "bitmasks")),
            ),
        ),
        onIntent = {},
    )
}

@Preview
@Composable
private fun RecommendScreenEmptyPreview() {
    RecommendScreen(
        state = RecommendState(loading = false, problems = emptyList()),
        onIntent = {},
    )
}
