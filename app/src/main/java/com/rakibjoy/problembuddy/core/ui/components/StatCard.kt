package com.rakibjoy.problembuddy.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.IntrinsicSize
import com.rakibjoy.problembuddy.core.ui.theme.AppShapes
import com.rakibjoy.problembuddy.core.ui.theme.Elevations
import com.rakibjoy.problembuddy.core.ui.theme.ProblemBuddyTheme
import com.rakibjoy.problembuddy.core.ui.theme.Spacing

@Composable
fun StatCard(
    label: String,
    value: String,
    accent: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier,
) {
    val base = MaterialTheme.colorScheme.surface
    // Approximate a "surface + 4dp tonal elevation" stop by blending in a
    // touch of the surfaceTint. Avoids depending on the experimental
    // surfaceColorAtElevation helper and stays readable in both themes.
    val tonal = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    val gradient = Brush.linearGradient(colors = listOf(base, tonal))
    Card(
        modifier = modifier,
        shape = AppShapes.medium,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = Elevations.hover),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .background(gradient),
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .background(accent),
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.lg),
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Preview(name = "StatCard", showBackground = true)
@Composable
private fun StatCardPreview() {
    ProblemBuddyTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            StatCard(label = "Solved this week", value = "23")
        }
    }
}
