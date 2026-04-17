package com.rakibjoy.problembuddy.core.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.rakibjoy.problembuddy.core.ui.theme.AppShapes
import com.rakibjoy.problembuddy.core.ui.theme.Elevations
import com.rakibjoy.problembuddy.core.ui.theme.ProblemBuddyTheme
import com.rakibjoy.problembuddy.core.ui.theme.Spacing

/** Animates a linear-gradient shimmer across the composable's bounds. */
fun Modifier.shimmer(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmer-progress",
    )
    val base = MaterialTheme.colorScheme.surfaceVariant
    val mid = base.copy(alpha = 0.4f)
    drawWithCache {
        val width = size.width
        val sweep = width * 2f
        val startX = -sweep + progress * (width + sweep)
        val brush = Brush.linearGradient(
            colors = listOf(base, mid, base),
            start = Offset(startX, 0f),
            end = Offset(startX + sweep, 0f),
        )
        onDrawBehind { drawRect(brush) }
    }
}

@Composable
fun SkeletonLine(width: Dp, height: Dp = 12.dp) {
    Spacer(
        modifier = Modifier
            .width(width)
            .height(height)
            .background(MaterialTheme.colorScheme.surfaceVariant, AppShapes.extraSmall)
            .shimmer(),
    )
}

@Composable
fun SkeletonCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
            .testTag("skeleton-card"),
        shape = AppShapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = Elevations.card),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg),
        ) {
            SkeletonLine(width = 180.dp, height = 18.dp)
            Spacer(Modifier.height(Spacing.sm))
            SkeletonLine(width = 240.dp, height = 12.dp)
        }
    }
}

@Preview(name = "SkeletonCard", showBackground = true)
@Composable
private fun SkeletonCardPreview() {
    ProblemBuddyTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            SkeletonCard()
        }
    }
}

@Preview(name = "SkeletonLine", showBackground = true)
@Composable
private fun SkeletonLinePreview() {
    ProblemBuddyTheme {
        Surface {
            Column(Modifier.padding(16.dp)) {
                SkeletonLine(width = 160.dp)
                Spacer(Modifier.height(8.dp))
                SkeletonLine(width = 220.dp, height = 16.dp)
            }
        }
    }
}
