package com.rakibjoy.problembuddy.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rakibjoy.problembuddy.core.ui.theme.AppShapes
import com.rakibjoy.problembuddy.core.ui.theme.ProblemBuddyTheme
import com.rakibjoy.problembuddy.core.ui.theme.appExtras
import com.rakibjoy.problembuddy.core.ui.theme.palette
import com.rakibjoy.problembuddy.domain.model.Tier
import com.rakibjoy.problembuddy.feature.profile.TierProjection
import kotlin.math.roundToInt

@Composable
fun ProjectionCard(
    projection: TierProjection,
    modifier: Modifier = Modifier,
) {
    val extras = MaterialTheme.appExtras
    val shape = AppShapes.small
    val tierColor = projection.nextTier.palette().strong

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(extras.surfaceElevated, shape)
            .border(0.5.dp, extras.borderSubtle, shape)
            .padding(14.dp),
    ) {
        Text(
            text = "PROJECTION",
            style = MaterialTheme.typography.labelSmall,
            color = extras.textTertiary,
        )
        Spacer(Modifier.height(6.dp))
        if (projection.averageDeltaLast10 <= 0f) {
            Text(
                text = "Next tier: ${projection.nextTier.label}",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = tierColor,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "current trend is flat/down — keep practicing",
                style = MaterialTheme.typography.labelMedium,
                color = extras.textSecondary,
            )
        } else {
            val avg = projection.averageDeltaLast10.roundToInt()
            Text(
                text = "Next tier: ${projection.nextTier.label} · ${projection.ratingNeeded} to go · avg Δ $avg/round",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = tierColor,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${projection.estimatedContestsToReach ?: "—"} rounds estimated",
                style = MaterialTheme.typography.labelMedium,
                color = extras.textSecondary,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProjectionCardPreview() {
    ProblemBuddyTheme(themeMode = com.rakibjoy.problembuddy.domain.model.ThemeMode.DARK) {
        Surface(modifier = Modifier.padding(16.dp)) {
            ProjectionCard(
                projection = TierProjection(
                    nextTier = Tier.EXPERT,
                    ratingNeeded = 78,
                    currentRating = 1522,
                    averageDeltaLast10 = 14.2f,
                    estimatedContestsToReach = 6,
                ),
            )
        }
    }
}
