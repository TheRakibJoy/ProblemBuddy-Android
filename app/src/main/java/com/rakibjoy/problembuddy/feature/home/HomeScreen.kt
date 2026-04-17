package com.rakibjoy.problembuddy.feature.home

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rakibjoy.problembuddy.core.ui.components.EmptyCorpusCard
import com.rakibjoy.problembuddy.domain.model.Tier
import com.rakibjoy.problembuddy.domain.model.TrainingJob

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
    Scaffold(
        topBar = { TopAppBar(title = { Text("ProblemBuddy") }) },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            HeaderCard(
                greeting = state.greeting,
                rating = state.rating,
                maxRating = state.maxRating,
                onProfileClick = { onIntent(HomeIntent.ProfileClicked) },
            )

            if (state.handle != null && !state.hasCorpus) {
                EmptyCorpusCard(
                    onTrainClicked = { onIntent(HomeIntent.TrainClicked) },
                )
            }

            Button(
                onClick = { onIntent(HomeIntent.RecommendClicked) },
                enabled = state.handle != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            ) { Text("Get problems") }

            OutlinedButton(
                onClick = { onIntent(HomeIntent.TrainClicked) },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Train corpus") }

            state.latestJob?.let { job -> LatestJobCard(job) }

            Spacer(modifier = Modifier.weight(1f))

            TextButton(
                onClick = { onIntent(HomeIntent.SettingsClicked) },
                modifier = Modifier.align(Alignment.CenterHorizontally),
            ) { Text("Settings") }
        }
    }
}

@Composable
private fun HeaderCard(
    greeting: String,
    rating: Int?,
    maxRating: Int?,
    onProfileClick: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(greeting, style = MaterialTheme.typography.headlineSmall)
            if (rating != null) {
                val maxText = maxRating?.let { " (max $it)" } ?: ""
                Text(
                    "Rating: $rating$maxText",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            TextButton(onClick = onProfileClick) { Text("View Profile") }
        }
    }
}

@Composable
private fun LatestJobCard(job: TrainingJob) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            val statusLabel = when (job.status) {
                TrainingJob.Status.QUEUED -> "Queued"
                TrainingJob.Status.RUNNING -> "Running"
                TrainingJob.Status.SUCCESS -> "Success"
                TrainingJob.Status.FAILED -> "Failed"
            }
            val tierLabel = job.currentTier?.label ?: "-"
            Text(
                "Training: $statusLabel",
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                "Tier: $tierLabel  ${job.done}/${job.total}",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Preview
@Composable
private fun HomeScreenNoHandlePreview() {
    HomeScreen(
        state = HomeState(greeting = "Welcome"),
        onIntent = {},
    )
}

@Preview
@Composable
private fun HomeScreenHandleNoCorpusPreview() {
    HomeScreen(
        state = HomeState(
            handle = "tourist",
            greeting = "Welcome back, tourist",
            rating = 3800,
            maxRating = 3979,
            hasCorpus = false,
        ),
        onIntent = {},
    )
}

@Preview
@Composable
private fun HomeScreenWithTrainingSuccessPreview() {
    HomeScreen(
        state = HomeState(
            handle = "tourist",
            greeting = "Welcome back, tourist",
            rating = 3800,
            maxRating = 3979,
            hasCorpus = true,
            latestJob = TrainingJob(
                id = 1,
                handle = "tourist",
                status = TrainingJob.Status.SUCCESS,
                currentTier = Tier.EXPERT,
                done = 120,
                total = 120,
                error = null,
                updatedAt = 0,
            ),
        ),
        onIntent = {},
    )
}
