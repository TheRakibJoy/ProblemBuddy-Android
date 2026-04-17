package com.rakibjoy.problembuddy.feature.onboarding

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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

@Composable
fun OnboardingScreen(
    onNavigateToHome: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                OnboardingEffect.NavigateToHome -> onNavigateToHome()
                is OnboardingEffect.ShowToast -> Toast.makeText(
                    context,
                    effect.message,
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }
    }
    OnboardingScreen(state = state, onIntent = viewModel::onIntent)
}

@Composable
fun OnboardingScreen(
    state: OnboardingState,
    onIntent: (OnboardingIntent) -> Unit,
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Welcome to ProblemBuddy",
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(
                text = "Enter your Codeforces handle to get started.",
                style = MaterialTheme.typography.bodyLarge,
            )
            OutlinedTextField(
                value = state.handleInput,
                onValueChange = { onIntent(OnboardingIntent.HandleChanged(it)) },
                label = { Text("Codeforces handle") },
                singleLine = true,
                isError = state.validation is HandleValidation.Invalid,
                supportingText = {
                    when (val v = state.validation) {
                        HandleValidation.Idle -> {}
                        HandleValidation.Validating -> Text("Checking\u2026")
                        is HandleValidation.Valid -> Text("\u2713 Ready")
                        is HandleValidation.Invalid -> Text(
                            v.reason,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
            Button(
                onClick = { onIntent(OnboardingIntent.SubmitClicked) },
                enabled = state.canSubmit,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (state.submitting) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                        )
                    }
                } else {
                    Text("Continue")
                }
            }
        }
    }
}

@Preview
@Composable
private fun OnboardingScreenIdlePreview() {
    OnboardingScreen(
        state = OnboardingState(),
        onIntent = {},
    )
}

@Preview
@Composable
private fun OnboardingScreenValidatingPreview() {
    OnboardingScreen(
        state = OnboardingState(
            handleInput = "tou",
            validation = HandleValidation.Validating,
        ),
        onIntent = {},
    )
}

@Preview
@Composable
private fun OnboardingScreenValidPreview() {
    OnboardingScreen(
        state = OnboardingState(
            handleInput = "tourist",
            validation = HandleValidation.Valid("tourist"),
        ),
        onIntent = {},
    )
}
