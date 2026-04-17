package com.rakibjoy.problembuddy.feature.train

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rakibjoy.problembuddy.domain.model.Tier
import com.rakibjoy.problembuddy.domain.model.TrainingJob

@Composable
fun TrainScreen(viewModel: TrainViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is TrainEffect.ShowToast -> Toast.makeText(
                    context,
                    effect.message,
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }
    }
    TrainScreen(state = state, onIntent = viewModel::onIntent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainScreen(
    state: TrainState,
    onIntent: (TrainIntent) -> Unit,
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Train") }) },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            HandleField(
                value = state.handleInput,
                validation = state.handleValidation,
                onValueChange = { onIntent(TrainIntent.HandleChanged(it)) },
            )
            Button(
                onClick = { onIntent(TrainIntent.StartClicked) },
                enabled = state.startEnabled,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Start") }

            state.activeJob?.let { job -> JobCard(job = job, onCancel = { onIntent(TrainIntent.CancelClicked) }) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HandleField(
    value: String,
    validation: HandleValidation,
    onValueChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Codeforces handle") },
        singleLine = true,
        isError = validation is HandleValidation.Invalid,
        supportingText = {
            when (validation) {
                HandleValidation.Idle -> {}
                HandleValidation.Validating -> Text("Checking\u2026")
                HandleValidation.Valid -> Text("\u2713 Handle OK")
                is HandleValidation.Invalid -> Text(
                    validation.reason,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun JobCard(job: TrainingJob, onCancel: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            StatusBadge(job.status)
            Text("Handle: ${job.handle}", style = MaterialTheme.typography.bodyMedium)
            job.currentTier?.let { tier ->
                Text("Tier: ${tier.label}", style = MaterialTheme.typography.bodyMedium)
            }

            if (job.total > 0) {
                val progress = (job.done.toFloat() / job.total.toFloat()).coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                )
            } else if (job.status == TrainingJob.Status.RUNNING || job.status == TrainingJob.Status.QUEUED) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            Text("${job.done}/${job.total}", style = MaterialTheme.typography.bodySmall)

            if (job.status == TrainingJob.Status.FAILED && !job.error.isNullOrBlank()) {
                Text(
                    job.error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            if (job.status == TrainingJob.Status.RUNNING || job.status == TrainingJob.Status.QUEUED) {
                TextButton(onClick = onCancel) { Text("Cancel") }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: TrainingJob.Status) {
    val (label, bg) = when (status) {
        TrainingJob.Status.QUEUED -> "Queued" to Color(0xFF9E9E9E)
        TrainingJob.Status.RUNNING -> "Running" to Color(0xFF1976D2)
        TrainingJob.Status.SUCCESS -> "Success" to Color(0xFF2E7D32)
        TrainingJob.Status.FAILED -> "Failed" to Color(0xFFC62828)
    }
    Text(
        text = label,
        color = Color.White,
        fontWeight = FontWeight.SemiBold,
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier
            .background(bg, RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 4.dp),
    )
    Spacer(Modifier.height(0.dp))
}

@Preview
@Composable
private fun TrainScreenIdlePreview() {
    TrainScreen(
        state = TrainState(),
        onIntent = {},
    )
}

@Preview
@Composable
private fun TrainScreenValidatingPreview() {
    TrainScreen(
        state = TrainState(
            handleInput = "tou",
            handleValidation = HandleValidation.Validating,
        ),
        onIntent = {},
    )
}

@Preview
@Composable
private fun TrainScreenValidRunningPreview() {
    TrainScreen(
        state = TrainState(
            handleInput = "tourist",
            handleValidation = HandleValidation.Valid,
            activeJob = TrainingJob(
                id = 1,
                handle = "tourist",
                status = TrainingJob.Status.RUNNING,
                currentTier = Tier.EXPERT,
                done = 40,
                total = 120,
                error = null,
                updatedAt = 0,
            ),
            startEnabled = false,
        ),
        onIntent = {},
    )
}

@Preview
@Composable
private fun TrainScreenSuccessPreview() {
    TrainScreen(
        state = TrainState(
            handleInput = "tourist",
            handleValidation = HandleValidation.Valid,
            activeJob = TrainingJob(
                id = 1,
                handle = "tourist",
                status = TrainingJob.Status.SUCCESS,
                currentTier = null,
                done = 120,
                total = 120,
                error = null,
                updatedAt = 0,
            ),
            startEnabled = true,
        ),
        onIntent = {},
    )
}

@Suppress("unused")
private val PreviewPadding = PaddingValues(0.dp)
