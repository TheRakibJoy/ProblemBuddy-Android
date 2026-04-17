package com.rakibjoy.problembuddy.core.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun AnimatedProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    accentBrush: Brush? = null,
) {
    if (progress < 0f) {
        LinearProgressIndicator(
            modifier = modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = trackColor,
        )
        return
    }

    val clamped = progress.coerceIn(0f, 1f)
    val animated by animateFloatAsState(
        targetValue = clamped,
        animationSpec = tween(durationMillis = 600),
        label = "progress-bar",
    )
    val brush = accentBrush ?: Brush.horizontalGradient(
        colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary),
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(trackColor),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animated)
                .fillMaxHeight()
                .background(brush),
        )
    }
}

@Preview(name = "AnimatedProgressBar", showBackground = true)
@Composable
private fun AnimatedProgressBarPreview() {
    ProblemBuddyThemePreview {
        AnimatedProgressBar(progress = 0.65f, modifier = Modifier.padding(16.dp))
    }
}

@Preview(name = "AnimatedProgressBar - Indeterminate", showBackground = true)
@Composable
private fun AnimatedProgressBarIndeterminatePreview() {
    ProblemBuddyThemePreview {
        AnimatedProgressBar(progress = -1f, modifier = Modifier.padding(16.dp))
    }
}

@Composable
private fun ProblemBuddyThemePreview(content: @Composable () -> Unit) {
    com.rakibjoy.problembuddy.core.ui.theme.ProblemBuddyTheme {
        Surface { content() }
    }
}
