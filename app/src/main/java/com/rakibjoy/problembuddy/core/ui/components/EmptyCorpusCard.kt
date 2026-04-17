package com.rakibjoy.problembuddy.core.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.rakibjoy.problembuddy.core.ui.theme.AppShapes
import com.rakibjoy.problembuddy.core.ui.theme.Elevations
import com.rakibjoy.problembuddy.core.ui.theme.ProblemBuddyTheme
import com.rakibjoy.problembuddy.core.ui.theme.Spacing

/**
 * Empty state for when the training corpus hasn't been built yet. Visually
 * consistent with [EmptyStateIllustration] but wrapped in a card so it can
 * sit inline inside a scrolling screen.
 */
@Composable
fun EmptyCorpusCard(
    onTrainClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = Elevations.hover),
    ) {
        EmptyStateIllustration(
            icon = Icons.Default.CloudOff,
            title = "No corpus yet",
            subtitle = "Run training to build your recommendation pool.",
            actionLabel = "Start training",
            onAction = onTrainClicked,
            modifier = Modifier.padding(horizontal = Spacing.sm),
        )
    }
}

@Preview(name = "EmptyCorpusCard", showBackground = true)
@Composable
private fun EmptyCorpusCardPreview() {
    ProblemBuddyTheme {
        Surface(modifier = Modifier.padding(Spacing.lg)) {
            EmptyCorpusCard(onTrainClicked = {})
        }
    }
}
