package com.rakibjoy.problembuddy.feature.onboarding

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rakibjoy.problembuddy.core.ui.components.GradientSurface
import com.rakibjoy.problembuddy.core.ui.components.pressScale
import com.rakibjoy.problembuddy.core.ui.theme.AppShapes
import com.rakibjoy.problembuddy.core.ui.theme.ProblemBuddyTheme
import com.rakibjoy.problembuddy.core.ui.theme.Spacing
import com.rakibjoy.problembuddy.domain.model.ThemeMode

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
    GradientSurface {
        Scaffold(
            containerColor = Color.Transparent,
        ) { padding ->
            var visible by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) { visible = true }

            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(horizontal = Spacing.xl),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(Spacing.xl))

                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(400)) +
                        slideInVertically(tween(400)) { it / 6 },
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        HeroOrb()
                        Spacer(Modifier.height(Spacing.lg))
                        Text(
                            text = "Welcome to ProblemBuddy",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                            ),
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.height(Spacing.xs))
                        Text(
                            text = "Your personal Codeforces coach.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.height(Spacing.lg))

                        OutlinedTextField(
                            value = state.handleInput,
                            onValueChange = { onIntent(OnboardingIntent.HandleChanged(it)) },
                            label = { Text("Codeforces handle") },
                            singleLine = true,
                            shape = AppShapes.medium,
                            leadingIcon = {
                                // decorative
                                Icon(Icons.Default.Person, contentDescription = null)
                            },
                            isError = state.validation is HandleValidation.Invalid,
                            supportingText = {
                                ValidationRow(state.validation)
                            },
                            modifier = Modifier.fillMaxWidth(),
                        )

                        Spacer(Modifier.height(Spacing.lg))

                        Button(
                            onClick = { onIntent(OnboardingIntent.SubmitClicked) },
                            enabled = state.canSubmit,
                            shape = AppShapes.large,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .pressScale(),
                        ) {
                            if (state.submitting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                )
                            } else {
                                Text(
                                    text = "Continue \u2192",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.SemiBold,
                                    ),
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.weight(1f))

                Text(
                    text = "No account. No backend. All local.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = Spacing.xl),
                )
            }
        }
    }
}

@Composable
private fun HeroOrb() {
    val transition = rememberInfiniteTransition(label = "orb-breathe")
    val scale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "orb-scale",
    )
    val primary = MaterialTheme.colorScheme.primary
    val brush = Brush.radialGradient(
        colors = listOf(primary.copy(alpha = 0.4f), Color.Transparent),
    )
    Box(
        modifier = Modifier
            .size(96.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(brush),
        contentAlignment = Alignment.Center,
    ) {
        // decorative
        Icon(
            imageVector = Icons.Default.Bolt,
            contentDescription = null,
            tint = primary,
            modifier = Modifier.size(48.dp),
        )
    }
}

@Composable
private fun ValidationRow(validation: HandleValidation) {
    Crossfade(targetState = validation, label = "validation") { v ->
        when (v) {
            HandleValidation.Idle -> Text(
                text = "Enter your handle to begin.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            HandleValidation.Validating -> Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                )
                Text(
                    text = "Checking with Codeforces\u2026",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            is HandleValidation.Valid -> Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                // decorative
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = "Ready to go, ${v.handle}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            is HandleValidation.Invalid -> Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                // decorative
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = v.reason,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Preview(name = "Onboarding - Idle (dark)")
@Composable
private fun OnboardingScreenIdlePreview() {
    ProblemBuddyTheme(themeMode = ThemeMode.DARK) {
        OnboardingScreen(
            state = OnboardingState(),
            onIntent = {},
        )
    }
}

@Preview(name = "Onboarding - Valid (dark)")
@Composable
private fun OnboardingScreenValidPreview() {
    ProblemBuddyTheme(themeMode = ThemeMode.DARK) {
        OnboardingScreen(
            state = OnboardingState(
                handleInput = "tourist",
                validation = HandleValidation.Valid("tourist"),
            ),
            onIntent = {},
        )
    }
}

@Preview(name = "Onboarding - Submitting (dark)")
@Composable
private fun OnboardingScreenSubmittingPreview() {
    ProblemBuddyTheme(themeMode = ThemeMode.DARK) {
        OnboardingScreen(
            state = OnboardingState(
                handleInput = "tourist",
                validation = HandleValidation.Valid("tourist"),
                submitting = true,
            ),
            onIntent = {},
        )
    }
}
